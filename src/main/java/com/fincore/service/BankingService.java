package com.fincore.service;

import com.fincore.db.DatabaseManager;
import com.fincore.model.*;
import com.fincore.repository.AccountRepository;
import com.fincore.repository.AuditLogRepository;
import com.fincore.repository.TransactionRepository;
import com.fincore.service.exception.AccountNotFoundException;
import com.fincore.service.exception.BankingException;
import com.fincore.service.exception.InsufficientFundsException;
import com.fincore.service.exception.InvalidTransactionException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * Core Banking Service managing financial operations with strict ACID Transaction Guarantees.
 * Demonstrates OOP business logic encapsulation and DBMS atomicity (commit & rollback).
 */
public class BankingService {

    private final DatabaseManager dbManager;
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final AuditLogRepository auditRepo;

    public BankingService(DatabaseManager dbManager,
                          AccountRepository accountRepo,
                          TransactionRepository txRepo,
                          AuditLogRepository auditRepo) {
        this.dbManager = dbManager;
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Deposits funds into an account and records ledger and audit records.
     */
    public Transaction deposit(String accountNumber, double amount, String description) {
        if (amount <= 0) {
            throw new InvalidTransactionException("Deposit amount must be strictly greater than zero.");
        }

        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        account.deposit(amount);
        accountRepo.update(account);

        String txId = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction tx = new Transaction(
                txId,
                accountNumber,
                TransactionType.DEPOSIT,
                amount,
                account.getBalance(),
                null,
                description != null ? description : "Cash/Cheque Deposit"
        );
        txRepo.save(tx);

        auditRepo.log("DEPOSIT", "ACCOUNT", accountNumber, "SYSTEM",
                String.format("Deposited $%.2f, New Balance: $%.2f", amount, account.getBalance()));

        return tx;
    }

    /**
     * Withdraws funds using OOP polymorphic account rules.
     */
    public Transaction withdraw(String accountNumber, double amount, String description) {
        if (amount <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be strictly greater than zero.");
        }

        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        // Account polymorphism enforces rules (minimum balance for savings, overdraft limit for checking)
        account.withdraw(amount);
        accountRepo.update(account);

        String txId = "WTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction tx = new Transaction(
                txId,
                accountNumber,
                TransactionType.WITHDRAWAL,
                amount,
                account.getBalance(),
                null,
                description != null ? description : "Cash Withdrawal"
        );
        txRepo.save(tx);

        auditRepo.log("WITHDRAWAL", "ACCOUNT", accountNumber, "SYSTEM",
                String.format("Withdrew $%.2f, New Balance: $%.2f", amount, account.getBalance()));

        return tx;
    }

    /**
     * Executes an ACID-compliant money transfer between two accounts.
     * Guarantees Atomicity: Either BOTH debit and credit succeed, or BOTH are rolled back.
     */
    public void transferFunds(String fromAccountNum, String toAccountNum, double amount, String remarks) {
        if (fromAccountNum.equalsIgnoreCase(toAccountNum)) {
            throw new InvalidTransactionException("Cannot transfer funds to the same account.");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("Transfer amount must be strictly greater than zero.");
        }

        // 1. If connected to Oracle DB, execute production PL/SQL Stored Package Procedure
        if (dbManager.isOracle()) {
            try (Connection conn = dbManager.getConnection();
                 CallableStatement cstmt = conn.prepareCall("{CALL PKG_BANKING_OPERATIONS.TRANSFER_FUNDS(?, ?, ?, ?, ?, ?)}")) {
                long start = System.currentTimeMillis();
                cstmt.setString(1, fromAccountNum);
                cstmt.setString(2, toAccountNum);
                cstmt.setDouble(3, amount);
                cstmt.setString(4, remarks != null ? remarks : "Fund Transfer");
                cstmt.registerOutParameter(5, Types.VARCHAR);
                cstmt.registerOutParameter(6, Types.VARCHAR);
                cstmt.execute();
                long duration = System.currentTimeMillis() - start;

                String status = cstmt.getString(5);
                String msg = cstmt.getString(6);
                dbManager.notifySqlExecuted("PL/SQL CALL", 
                        "CALL PKG_BANKING_OPERATIONS.TRANSFER_FUNDS('" + fromAccountNum + "', '" + toAccountNum + "', " + amount + ")", 
                        duration, 2);

                if ("SUCCESS".equalsIgnoreCase(status)) {
                    return;
                } else {
                    throw new InvalidTransactionException(msg != null ? msg : "Transfer failed in PL/SQL package.");
                }
            } catch (SQLException e) {
                System.out.println("[BankingService] Notice: PL/SQL package procedure fallback to JDBC ACID transaction: " + e.getMessage());
                // Fall through to standard JDBC ACID transaction below
            }
        }

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            // 2. Begin DBMS Transaction (Disable auto-commit)
            conn.setAutoCommit(false);

            Account source = accountRepo.findByAccountNumber(conn, fromAccountNum)
                    .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + fromAccountNum));

            Account target = accountRepo.findByAccountNumber(conn, toAccountNum)
                    .orElseThrow(() -> new AccountNotFoundException("Target account not found: " + toAccountNum));

            if (!"ACTIVE".equalsIgnoreCase(source.getStatus())) {
                throw new InvalidTransactionException("Source account is not active: " + fromAccountNum);
            }
            if (!"ACTIVE".equalsIgnoreCase(target.getStatus())) {
                throw new InvalidTransactionException("Target account is not active: " + toAccountNum);
            }

            // 2. OOP Polymorphic withdrawal check
            if (!source.canWithdraw(amount)) {
                throw new InsufficientFundsException(
                        String.format("Transfer failed: Account %s has insufficient funds/overdraft for transfer of $%.2f (Current balance: $%.2f)",
                                fromAccountNum, amount, source.getBalance())
                );
            }

            // 3. Mutate balances
            source.withdraw(amount);
            target.deposit(amount);

            // 4. Update database balances inside transaction
            accountRepo.updateBalance(conn, source.getAccountNumber(), source.getBalance());
            accountRepo.updateBalance(conn, target.getAccountNumber(), target.getBalance());

            // 5. Record dual ledger entries (Debit & Credit)
            String baseTxId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Transaction debitTx = new Transaction(
                    "TRF-OUT-" + baseTxId,
                    fromAccountNum,
                    TransactionType.TRANSFER_OUT,
                    amount,
                    source.getBalance(),
                    toAccountNum,
                    "Transfer to " + toAccountNum + (remarks != null ? ": " + remarks : "")
            );
            txRepo.save(conn, debitTx);

            Transaction creditTx = new Transaction(
                    "TRF-IN-" + baseTxId,
                    toAccountNum,
                    TransactionType.TRANSFER_IN,
                    amount,
                    target.getBalance(),
                    fromAccountNum,
                    "Transfer from " + fromAccountNum + (remarks != null ? ": " + remarks : "")
            );
            txRepo.save(conn, creditTx);

            // 6. Record Audit Log inside the transaction
            auditRepo.log(conn, "TRANSFER", "TRANSACTION", baseTxId, "SYSTEM",
                    String.format("Transferred $%.2f from %s to %s", amount, fromAccountNum, toAccountNum));

            // 7. Commit Transaction atomically!
            conn.commit();

        } catch (Exception e) {
            // Rollback on any failure to preserve DBMS consistency
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[ACID Transaction] Transaction rolled back due to: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("[ACID Transaction] Rollback failure: " + rollbackEx.getMessage());
                }
            }
            if (e instanceof BankingException) {
                throw (BankingException) e;
            }
            throw new BankingException("Transfer failed and transaction was safely rolled back: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("[ACID Transaction] Error restoring auto-commit: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Polymorphic month-end batch processing:
     * - Savings Accounts earn interest credit.
     * - Checking Accounts pay monthly maintenance fee.
     */
    public void processMonthlyAdjustments() {
        for (Account account : accountRepo.findAll()) {
            double adjustment = account.calculateMonthlyInterestOrFee();
            if (adjustment > 0.001) {
                // Interest earned
                deposit(account.getAccountNumber(), adjustment,
                        String.format("Monthly Interest Credit (APR adjustment)"));
            } else if (adjustment < -0.001) {
                // Maintenance fee
                double fee = Math.abs(adjustment);
                try {
                    withdraw(account.getAccountNumber(), fee, "Monthly Account Maintenance Fee");
                } catch (Exception ex) {
                    System.err.println("[Batch] Failed to deduct fee from " + account.getAccountNumber() + ": " + ex.getMessage());
                }
            }
        }
    }
}
