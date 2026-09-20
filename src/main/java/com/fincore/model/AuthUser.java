package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Entity representing an authenticated user in the database.
 * Used for database-backed authentication in Oracle 10g XE / SQLite.
 */
public class AuthUser {

    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public AuthUser() {
        this.status = "ACTIVE";
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
    }

    public AuthUser(Long id, String username, String password, String fullName, String role, String status, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role != null ? role : "USER";
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public AuthUser(String username, String password, String fullName, String role, String status) {
        this(null, username, password, fullName, role, status, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", fullName, username, role);
    }
}
