package com.fincore;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Account;
import com.fincore.model.CheckingAccount;
import com.fincore.model.Customer;
import com.fincore.model.SavingsAccount;
import com.fincore.repository.AccountRepository;
import com.fincore.repository.AuditLogRepository;
import com.fincore.repository.CustomerRepository;
import com.fincore.repository.TransactionRepository;
import com.fincore.repository.impl.JdbcAccountRepository;
import com.fincore.repository.impl.JdbcAuditLogRepository;
import com.fincore.repository.impl.JdbcCustomerRepository;
import com.fincore.repository.impl.JdbcTransactionRepository;
import com.fincore.service.BankingService;
import com.fincore.service.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests verifying DBMS ACID Transactions, Atomicity, and Rollbacks.
 */
public class BankingServiceTest {

    private static DatabaseManager dbManager;
    private static AccountRepository accountRepo;
    private static TransactionRepository txRepo;
    private static AuditLogRepository auditRepo;
    private static CustomerRepository customerRepo;
    private static BankingService bankingService;

    @BeforeAll
    static void setUpAll() {
        dbManager = DatabaseManager.getInstance();
        dbManager.initializeDatabase();

        customerRepo = new JdbcCustomerRepository(dbManager);
        accountRepo = new JdbcAccountRepository(dbManager);
        txRepo = new JdbcTransactionRepository(dbManager);
        auditRepo = new JdbcAuditLogRepository(dbManager);

        bankingService = new BankingService(dbManager, accountRepo, txRepo, auditRepo);
    }

    private String createTestAccount(double initialBalance, boolean isSavings) {
        String code = "T-" + UUID.randomUUID().toString().substring(0, 6);
        Customer c = customerRepo.save(new Customer(code, "Test User", code + "@test.org", "555-0000"));

        String accNum = (isSavings ? "SAV-" : "CHK-") + UUID.randomUUID().toString().substring(0, 6);
        Account acc;
        if (isSavings) {
            acc = new SavingsAccount(accNum, c.getId(), initialBalance, 4.0, "ACTIVE", null);
        } else {
            acc = new CheckingAccount(accNum, c.getId(), initialBalance, 500.0, "ACTIVE", null);
        }
        accountRepo.save(acc);
        return accNum;
    }

    @Test
    @DisplayName("ACID Transfer succeeds atomically and updates both accounts")
    void testTransferSuccessCommit() {
        String fromAcc = createTestAccount(1000.0, true);
        String toAcc = createTestAccount(200.0, false);

        bankingService.transferFunds(fromAcc, toAcc, 300.0, "Test Payment");

        Account updatedFrom = accountRepo.findByAccountNumber(fromAcc).orElseThrow();
        Account updatedTo = accountRepo.findByAccountNumber(toAcc).orElseThrow();

        assertEquals(700.0, updatedFrom.getBalance(), 0.001, "Source account must decrease by 300");
        assertEquals(500.0, updatedTo.getBalance(), 0.001, "Target account must increase by 300");
    }

    @Test
    @DisplayName("ACID Transfer rollback preserves balances when insufficient funds occur")
    void testTransferRollbackOnInsufficientFunds() {
        String fromAcc = createTestAccount(100.0, true); // Savings balance = 100
        String toAcc = createTestAccount(50.0, false);

        // Try transferring 500 (exceeds balance + min balance)
        assertThrows(InsufficientFundsException.class, () ->
            bankingService.transferFunds(fromAcc, toAcc, 500.0, "Excessive Transfer")
        );

        // Verify DBMS rollback ensured no balances changed
        Account fromAfter = accountRepo.findByAccountNumber(fromAcc).orElseThrow();
        Account toAfter = accountRepo.findByAccountNumber(toAcc).orElseThrow();

        assertEquals(100.0, fromAfter.getBalance(), 0.001, "Source balance must remain unchanged after rollback");
        assertEquals(50.0, toAfter.getBalance(), 0.001, "Target balance must remain unchanged after rollback");
    }
}
