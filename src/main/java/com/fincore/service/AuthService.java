package com.fincore.service;

import com.fincore.model.AuthUser;
import com.fincore.repository.AuditLogRepository;
import com.fincore.repository.UserRepository;
import com.fincore.service.exception.BankingException;

import java.util.Optional;

/**
 * Service managing user authentication from database (Oracle 10g XE / SQLite).
 */
public class AuthService {

    private final UserRepository userRepo;
    private final AuditLogRepository auditRepo;
    private AuthUser currentUser;

    public AuthService(UserRepository userRepo, AuditLogRepository auditRepo) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Authenticates a user against the relational database.
     */
    public AuthUser login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BankingException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BankingException("Password cannot be empty.");
        }

        Optional<AuthUser> userOpt = userRepo.authenticate(username.trim(), password.trim());
        if (userOpt.isEmpty()) {
            auditRepo.log("AUTH_FAILURE", "USER", username, "SYSTEM", "Failed login attempt for username: " + username);
            throw new BankingException("Invalid username or password. Please check your credentials.");
        }

        this.currentUser = userOpt.get();
        auditRepo.log("AUTH_SUCCESS", "USER", username, currentUser.getFullName(), "User successfully authenticated from database.");
        return this.currentUser;
    }

    public void logout() {
        if (currentUser != null) {
            auditRepo.log("LOGOUT", "USER", currentUser.getUsername(), currentUser.getFullName(), "User logged out.");
            currentUser = null;
        }
    }

    public AuthUser getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
