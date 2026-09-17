package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Concrete implementation of Checking Account demonstrating OOP Inheritance and Polymorphism.
 * Implements overdraft credit protection and recurring maintenance fee logic.
 */
public class CheckingAccount extends Account {

    public static final double DEFAULT_OVERDRAFT_LIMIT = 1000.00;
    public static final double DEFAULT_MAINTENANCE_FEE = 12.00;

    private double overdraftLimit;
    private double monthlyMaintenanceFee = DEFAULT_MAINTENANCE_FEE;

    public CheckingAccount() {
        super();
        this.overdraftLimit = DEFAULT_OVERDRAFT_LIMIT;
    }

    public CheckingAccount(String accountNumber, Long customerId, double balance, double overdraftLimit, String status, LocalDateTime createdAt) {
        super(accountNumber, customerId, balance, status, createdAt);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public boolean canWithdraw(double amount) {
        // Allows overdraft up to the approved limit
        return (getBalance() + overdraftLimit) >= amount;
    }

    @Override
    public double calculateMonthlyInterestOrFee() {
        // Negative return indicates a fee deducted from account
        return -monthlyMaintenanceFee;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CHECKING;
    }

    @Override
    public String getAccountSummary() {
        return String.format("[CHECKING] Acc# %s | Balance: $%,.2f | Overdraft Limit: $%,.2f | Fee: $%.2f/mo | Status: %s",
                getAccountNumber(), getBalance(), overdraftLimit, monthlyMaintenanceFee, getStatus());
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public double getMonthlyMaintenanceFee() {
        return monthlyMaintenanceFee;
    }

    public void setMonthlyMaintenanceFee(double monthlyMaintenanceFee) {
        this.monthlyMaintenanceFee = monthlyMaintenanceFee;
    }
}
