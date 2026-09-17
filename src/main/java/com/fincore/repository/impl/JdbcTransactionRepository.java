package com.fincore.repository.impl;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Transaction;
import com.fincore.model.TransactionType;
import com.fincore.repository.TransactionRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of TransactionRepository managing ledger records.
 */
public class JdbcTransactionRepository implements TransactionRepository {

    private final DatabaseManager dbManager;

    public JdbcTransactionRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Transaction save(Transaction tx) {
        try (Connection conn = dbManager.getConnection()) {
            return save(conn, tx);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction save(Connection conn, Transaction tx) {
        String sql = "INSERT INTO transactions (transaction_id, account_number, type, amount, balance_after, target_account, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getTransactionId());
            ps.setString(2, tx.getAccountNumber());
            ps.setString(3, tx.getType().name());
            ps.setDouble(4, tx.getAmount());
            ps.setDouble(5, tx.getBalanceAfter());
            ps.setString(6, tx.getTargetAccount());
            ps.setString(7, tx.getDescription());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setId(rs.getLong(1));
                }
            }
            return tx;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving transaction in connection: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT id, transaction_id, account_number, type, amount, balance_after, target_account, description, created_at " +
                     "FROM transactions WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transaction by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Transaction> findByTransactionId(String transactionId) {
        String sql = "SELECT id, transaction_id, account_number, type, amount, balance_after, target_account, description, created_at " +
                     "FROM transactions WHERE transaction_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transaction by UUID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, transaction_id, account_number, type, amount, balance_after, target_account, description, created_at " +
                     "FROM transactions WHERE account_number = ? ORDER BY created_at DESC, id DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transactions by account: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Transaction> findRecentTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, transaction_id, account_number, type, amount, balance_after, target_account, description, created_at " +
                     "FROM transactions ORDER BY created_at DESC, id DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recent transactions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Transaction> findAll() {
        return findRecentTransactions(500);
    }

    @Override
    public boolean update(Transaction entity) {
        throw new UnsupportedOperationException("Financial ledger transactions are immutable and cannot be updated.");
    }

    @Override
    public boolean deleteById(Long id) {
        throw new UnsupportedOperationException("Financial ledger transactions are immutable and cannot be deleted.");
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new Transaction(
            rs.getLong("id"),
            rs.getString("transaction_id"),
            rs.getString("account_number"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getDouble("amount"),
            rs.getDouble("balance_after"),
            rs.getString("target_account"),
            rs.getString("description"),
            created
        );
    }
}
