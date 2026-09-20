package com.fincore.service;

import com.fincore.model.Account;
import com.fincore.model.Budget;
import com.fincore.model.FinancialRecord;
import com.fincore.model.Transaction;
import com.fincore.repository.*;
import com.fincore.service.exception.BankingException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * High-level business service orchestrating the Finance Dashboard:
 * Income & Expenses CRUD, Accounts, Budgets, Transactions, and Financial Reports.
 */
public class FinanceService {

    private final FinancialRecordRepository finRecordRepo;
    private final BudgetRepository budgetRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final AuditLogRepository auditRepo;

    public FinanceService(FinancialRecordRepository finRecordRepo,
                          BudgetRepository budgetRepo,
                          AccountRepository accountRepo,
                          TransactionRepository txRepo,
                          AuditLogRepository auditRepo) {
        this.finRecordRepo = finRecordRepo;
        this.budgetRepo = budgetRepo;
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
        this.auditRepo = auditRepo;
    }

    // =========================================================================
    // Complete CRUD Module: Income & Expenses
    // =========================================================================

    /**
     * INSERT: Adds a new Income or Expense record to the database.
     */
    public FinancialRecord addRecord(FinancialRecord.Type type, String category, double amount,
                                     String accountNumber, String description, LocalDate date) {
        if (amount <= 0) {
            throw new BankingException("Amount must be greater than zero.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new BankingException("Category cannot be empty.");
        }

        FinancialRecord record = new FinancialRecord(null, type, category.trim(), amount,
                accountNumber, description != null ? description.trim() : "", date, null);

        FinancialRecord saved = finRecordRepo.save(record);
        auditRepo.log("INSERT_FINANCIAL_RECORD", "FINANCIAL_RECORD", String.valueOf(saved.getId()),
                "FINANCE_APP", String.format("Added %s: $%.2f [%s]", type, amount, category));
        return saved;
    }

    /**
     * SELECT: Retrieves all financial records.
     */
    public List<FinancialRecord> getAllRecords() {
        return finRecordRepo.findAll();
    }

    /**
     * SELECT: Retrieves records filtered by type (INCOME or EXPENSE).
     */
    public List<FinancialRecord> getRecordsByType(FinancialRecord.Type type) {
        return finRecordRepo.findByType(type);
    }

    /**
     * SELECT: Retrieves a single financial record by ID.
     */
    public Optional<FinancialRecord> getRecordById(Long id) {
        return finRecordRepo.findById(id);
    }

    /**
     * UPDATE: Updates an existing financial record.
     */
    public boolean updateRecord(FinancialRecord record) {
        if (record.getId() == null) {
            throw new BankingException("Cannot update record without an ID.");
        }
        if (record.getAmount() <= 0) {
            throw new BankingException("Amount must be greater than zero.");
        }

        boolean updated = finRecordRepo.update(record);
        if (updated) {
            auditRepo.log("UPDATE_FINANCIAL_RECORD", "FINANCIAL_RECORD", String.valueOf(record.getId()),
                    "FINANCE_APP", String.format("Updated record #%d: $%.2f [%s]", record.getId(), record.getAmount(), record.getCategory()));
        }
        return updated;
    }

    /**
     * DELETE: Deletes a financial record from the database.
     */
    public boolean deleteRecord(Long id) {
        Optional<FinancialRecord> existing = finRecordRepo.findById(id);
        if (existing.isEmpty()) {
            throw new BankingException("Financial record #" + id + " not found.");
        }

        boolean deleted = finRecordRepo.deleteById(id);
        if (deleted) {
            auditRepo.log("DELETE_FINANCIAL_RECORD", "FINANCIAL_RECORD", String.valueOf(id),
                    "FINANCE_APP", "Deleted record #" + id + " (" + existing.get().getCategory() + ")");
        }
        return deleted;
    }

    // =========================================================================
    // Dashboard Financial Metrics
    // =========================================================================

    public double getTotalIncome() {
        return finRecordRepo.getTotalIncome();
    }

    public double getTotalExpenses() {
        return finRecordRepo.getTotalExpenses();
    }

    public double getNetSavings() {
        return getTotalIncome() - getTotalExpenses();
    }

    public double getTotalAccountBalances() {
        return accountRepo.findAll().stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    public List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    public List<Budget> getAllBudgetsWithSpending() {
        return budgetRepo.getBudgetsWithSpending();
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return txRepo.findRecentTransactions(limit);
    }

    public Map<String, Double> getCategoryBreakdown(FinancialRecord.Type type) {
        return finRecordRepo.getCategoryBreakdown(type);
    }
}
