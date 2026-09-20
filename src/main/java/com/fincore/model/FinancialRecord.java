package com.fincore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing an Income or Expense financial record.
 * Supports complete CRUD (Create, Read, Update, Delete) module operations.
 */
public class FinancialRecord {

    public enum Type {
        INCOME,
        EXPENSE
    }

    private Long id;
    private Type recordType;
    private String category;
    private double amount;
    private String accountNumber;
    private String description;
    private LocalDate recordDate;
    private LocalDateTime createdAt;

    public FinancialRecord() {
        this.recordType = Type.EXPENSE;
        this.recordDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    public FinancialRecord(Long id, Type recordType, String category, double amount,
                           String accountNumber, String description, LocalDate recordDate,
                           LocalDateTime createdAt) {
        this.id = id;
        this.recordType = recordType;
        this.category = category;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.description = description;
        this.recordDate = recordDate != null ? recordDate : LocalDate.now();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public FinancialRecord(Type recordType, String category, double amount, String accountNumber, String description) {
        this(null, recordType, category, amount, accountNumber, description, LocalDate.now(), LocalDateTime.now());
    }

    public FinancialRecord(Type recordType, String category, double amount, String accountNumber, String description, LocalDate recordDate) {
        this(null, recordType, category, amount, accountNumber, description, recordDate, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getRecordType() {
        return recordType;
    }

    public void setRecordType(Type recordType) {
        this.recordType = recordType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be strictly greater than zero.");
        }
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isIncome() {
        return recordType == Type.INCOME;
    }

    public boolean isExpense() {
        return recordType == Type.EXPENSE;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | $%,.2f | Acc: %s | %s | %s",
                id, recordDate, recordType, amount,
                accountNumber != null ? accountNumber : "N/A",
                category, description);
    }
}
