package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Encapsulated ledger transaction entity representing historical account movements.
 */
public class Transaction {

    private Long id;
    private String transactionId;
    private String accountNumber;
    private TransactionType type;
    private double amount;
    private double balanceAfter;
    private String targetAccount;
    private String description;
    private LocalDateTime createdAt;

    public Transaction() {
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(Long id, String transactionId, String accountNumber, TransactionType type,
                       double amount, double balanceAfter, String targetAccount, String description,
                       LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.targetAccount = targetAccount;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Transaction(String transactionId, String accountNumber, TransactionType type,
                       double amount, double balanceAfter, String targetAccount, String description) {
        this(null, transactionId, accountNumber, type, amount, balanceAfter, targetAccount, description, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(String targetAccount) {
        this.targetAccount = targetAccount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | $%,.2f | Balance: $%,.2f | %s",
                createdAt != null ? createdAt.toString().replace('T', ' ').substring(0, 19) : "",
                transactionId, type, amount, balanceAfter, description);
    }
}
