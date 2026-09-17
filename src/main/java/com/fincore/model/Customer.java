package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Customer class extending User, demonstrating OOP Inheritance and Polymorphism.
 */
public class Customer extends User {

    private String customerTier = "STANDARD";

    public Customer() {
        super();
    }

    public Customer(Long id, String customerCode, String name, String email, String phone, String status, LocalDateTime createdAt) {
        super(id, customerCode, name, email, phone, status, createdAt);
    }

    public Customer(String customerCode, String name, String email, String phone) {
        super(null, customerCode, name, email, phone, "ACTIVE", LocalDateTime.now());
    }

    @Override
    public String getRoleDescription() {
        return "Bank Retail Customer [" + customerTier + " Tier]";
    }

    public String getCustomerTier() {
        return customerTier;
    }

    public void setCustomerTier(String customerTier) {
        this.customerTier = customerTier;
    }
}
