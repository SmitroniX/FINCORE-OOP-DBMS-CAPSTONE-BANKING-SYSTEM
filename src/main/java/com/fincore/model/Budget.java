package com.fincore.model;

/**
 * Domain entity representing a category spending budget and progress tracking.
 */
public class Budget {

    private Long id;
    private String category;
    private double monthlyLimit;
    private String periodMonth;
    private double spentAmount;

    public Budget() {
    }

    public Budget(Long id, String category, double monthlyLimit, String periodMonth) {
        this.id = id;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.periodMonth = periodMonth;
        this.spentAmount = 0.0;
    }

    public Budget(Long id, String category, double monthlyLimit, String periodMonth, double spentAmount) {
        this.id = id;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.periodMonth = periodMonth;
        this.spentAmount = spentAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public String getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(String periodMonth) {
        this.periodMonth = periodMonth;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return monthlyLimit - spentAmount;
    }

    public double getPercentageUsed() {
        if (monthlyLimit <= 0) return 0.0;
        return Math.min(100.0, (spentAmount / monthlyLimit) * 100.0);
    }

    public boolean isExceeded() {
        return spentAmount > monthlyLimit;
    }

    @Override
    public String toString() {
        return String.format("%s: Limit $%,.2f | Spent: $%,.2f | Remaining: $%,.2f (%.1f%%)",
                category, monthlyLimit, spentAmount, getRemainingAmount(), getPercentageUsed());
    }
}
