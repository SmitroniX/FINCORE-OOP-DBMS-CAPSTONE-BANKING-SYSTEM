package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Abstract Base Class demonstrating OOP Abstraction and Encapsulation.
 */
public abstract class User {

    private Long id;
    private String userCode;
    private String name;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String userCode, String name, String email, String phone, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userCode = userCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Abstract method demonstrating polymorphism in derived subclasses
    public abstract String getRoleDescription();

    // Getters and Setters with Encapsulation
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
        return String.format("[%s] %s (%s) - %s", userCode, name, email, getRoleDescription());
    }
}
