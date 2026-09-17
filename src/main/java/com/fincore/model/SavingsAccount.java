package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Concrete implementation of Savings Account demonstrating OOP Inheritance and Polymorphism.
 * Implements interest calculations and enforces minimum balance invariants.
 */
public class SavingsAccount extends Account {

    public static final double DEFAULT_MINIMUM_BALANCE = 50.00;
    private double interestRate; // Annual percentage e.g. 4.25%
    private double minimumBalance = DEFAULT_MINIMUM_BALANCE;

    public SavingsAccount() {
        super();
        this.interestRate = 3.50;
    }

    public SavingsAccount(String accountNumber, Long customerId, double balance, double interestRate, String status, LocalDateTime createdAt) {
        super(accountNumber, customerId, balance, status, createdAt);
        this.interestRate = interestRate;
    }

    @Override
    public boolean canWithdraw(double amount) {
        return (getBalance() - amount) >= minimumBalance;
    }

    @Override
    public double calculateMonthlyInterestOrFee() {
        // Positive return represents interest earned for the customer
        return getBalance() * (interestRate / 100.0) / 12.0;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public String getAccountSummary() {
        return String.format("[SAVINGS] Acc# %s | Balance: $%,.2f | APR: %.2f%% | Min Balance: $%.2f | Status: %s",
                getAccountNumber(), getBalance(), interestRate, minimumBalance, getStatus());
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        this.minimumBalance = minimumBalance;
    }
}
