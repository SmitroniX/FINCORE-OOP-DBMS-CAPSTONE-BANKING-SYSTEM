package com.fincore.model;

/**
 * Data Transfer Object (DTO) capturing analytical aggregation queries across
 * Customers, Accounts, and Transactions (demonstrating DBMS JOINs and GROUP BY).
 */
public class CustomerSummaryDTO {

    private Long customerId;
    private String customerCode;
    private String customerName;
    private String email;
    private int totalAccounts;
    private double totalBalance;
    private int totalTransactions;

    public CustomerSummaryDTO() {
    }

    public CustomerSummaryDTO(Long customerId, String customerCode, String customerName, String email,
                              int totalAccounts, double totalBalance, int totalTransactions) {
        this.customerId = customerId;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.email = email;
        this.totalAccounts = totalAccounts;
        this.totalBalance = totalBalance;
        this.totalTransactions = totalTransactions;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    @Override
    public String toString() {
        return String.format("%-4d | %-10s | %-20s | %-25s | Accounts: %2d | Balance: $%,12.2f | Txs: %3d",
                customerId, customerCode, customerName, email, totalAccounts, totalBalance, totalTransactions);
    }
}
