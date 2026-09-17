package com.fincore.model;

import com.fincore.service.exception.InsufficientFundsException;
import com.fincore.service.exception.InvalidTransactionException;

import java.time.LocalDateTime;

/**
 * Abstract Base Class representing a financial bank account.
 * Demonstrates OOP Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class Account {

    private String accountNumber;
    private Long customerId;
    private double balance;
    private String status;
    private LocalDateTime createdAt;

    public Account() {
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public Account(String accountNumber, Long customerId, double balance, String status, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = balance;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // =========================================================================
    // Abstract Polymorphic Methods (Implemented differently by each subclass)
    // =========================================================================

    /**
     * Checks whether a withdrawal of the given amount is permissible under account rules.
     */
    public abstract boolean canWithdraw(double amount);

    /**
     * Calculates monthly financial adjustments (interest credit or maintenance fee debit).
     */
    public abstract double calculateMonthlyInterestOrFee();

    /**
     * Returns the specific account type.
     */
    public abstract AccountType getAccountType();

    /**
     * Returns a human-readable formatted summary of account details.
     */
    public abstract String getAccountSummary();

    // =========================================================================
    // Core Encapsulated Business Logic
    // =========================================================================

    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new InvalidTransactionException("Deposit amount must be strictly greater than zero.");
        }
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            throw new InvalidTransactionException("Cannot deposit to non-active account: " + accountNumber);
        }
        this.balance += amount;
    }

    public synchronized void withdraw(double amount) {
        if (amount <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be strictly greater than zero.");
        }
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            throw new InvalidTransactionException("Cannot withdraw from non-active account: " + accountNumber);
        }
        if (!canWithdraw(amount)) {
            throw new InsufficientFundsException(
                String.format("Insufficient funds/overdraft in account %s. Requested: $%.2f, Balance: $%.2f",
                        accountNumber, amount, balance)
            );
        }
        this.balance -= amount;
    }

    // =========================================================================
    // Encapsulated Getters and Setters
    // =========================================================================

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - Balance: $%.2f (%s)", getAccountType(), accountNumber, balance, status);
    }
}
