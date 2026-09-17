package com.fincore.repository.impl;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Account;
import com.fincore.model.AccountType;
import com.fincore.model.CheckingAccount;
import com.fincore.model.SavingsAccount;
import com.fincore.repository.AccountRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of AccountRepository demonstrating Polymorphic Object-Relational Mapping (ORM).
 */
public class JdbcAccountRepository implements AccountRepository {

    private final DatabaseManager dbManager;

    public JdbcAccountRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Account save(Account account) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getAccountNumber());
            ps.setLong(2, account.getCustomerId());
            ps.setString(3, account.getAccountType().name());
            ps.setDouble(4, account.getBalance());

            if (account instanceof SavingsAccount sa) {
                ps.setDouble(5, sa.getInterestRate());
                ps.setDouble(6, 0.0);
            } else if (account instanceof CheckingAccount ca) {
                ps.setDouble(5, 0.0);
                ps.setDouble(6, ca.getOverdraftLimit());
            } else {
                ps.setDouble(5, 0.0);
                ps.setDouble(6, 0.0);
            }

            ps.setString(7, account.getStatus() != null ? account.getStatus() : "ACTIVE");
            ps.executeUpdate();
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving account: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Account> findById(String accountNumber) {
        return findByAccountNumber(accountNumber);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status, created_at " +
                     "FROM accounts WHERE account_number = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account by number: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByAccountNumber(Connection conn, String accountNumber) {
        String sql = "SELECT account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status, created_at " +
                     "FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading account in transaction: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Account> findByCustomerId(Long customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status, created_at " +
                     "FROM accounts WHERE customer_id = ? ORDER BY account_number ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding accounts by customer ID: " + e.getMessage(), e);
        }
        return accounts;
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status, created_at " +
                     "FROM accounts ORDER BY account_number ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all accounts: " + e.getMessage(), e);
        }
        return accounts;
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE accounts SET balance = ?, status = ? WHERE account_number = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, account.getBalance());
            ps.setString(2, account.getStatus());
            ps.setString(3, account.getAccountNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating account: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateBalance(Connection conn, String accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating balance in transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String accountNumber) {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account: " + e.getMessage(), e);
        }
    }

    /**
     * Factory method hydrating relational rows into polymorphic OOP Account subclasses.
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String accNum = rs.getString("account_number");
        Long customerId = rs.getLong("customer_id");
        String typeStr = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        double interestRate = rs.getDouble("interest_rate");
        double overdraftLimit = rs.getDouble("overdraft_limit");
        String status = rs.getString("status");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        AccountType type = AccountType.valueOf(typeStr.toUpperCase());
        if (type == AccountType.SAVINGS) {
            return new SavingsAccount(accNum, customerId, balance, interestRate, status, created);
        } else {
            return new CheckingAccount(accNum, customerId, balance, overdraftLimit, status, created);
        }
    }
}
