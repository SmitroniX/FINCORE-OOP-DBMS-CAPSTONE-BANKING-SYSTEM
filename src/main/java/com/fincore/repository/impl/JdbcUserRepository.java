package com.fincore.repository.impl;

import com.fincore.db.DatabaseManager;
import com.fincore.model.AuthUser;
import com.fincore.repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of UserRepository.
 * Handles Database-backed authentication compatible with Oracle 10g XE, SQLite, and MySQL.
 */
public class JdbcUserRepository implements UserRepository {

    private final DatabaseManager dbManager;

    public JdbcUserRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Optional<AuthUser> authenticate(String username, String password) {
        long start = System.currentTimeMillis();
        String sql = "SELECT id, username, password, full_name, role, status, created_at FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE'";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                long duration = System.currentTimeMillis() - start;
                int affected = 0;
                Optional<AuthUser> result = Optional.empty();
                if (rs.next()) {
                    affected = 1;
                    result = Optional.of(mapResultSetToUser(rs));
                }
                dbManager.notifySqlExecuted("SELECT (Authenticate)", sql + " [params: " + username + ", ********]", duration, affected);
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error during authentication: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        long start = System.currentTimeMillis();
        String sql = "SELECT id, username, password, full_name, role, status, created_at FROM users WHERE username = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                long duration = System.currentTimeMillis() - start;
                Optional<AuthUser> result = Optional.empty();
                int affected = 0;
                if (rs.next()) {
                    affected = 1;
                    result = Optional.of(mapResultSetToUser(rs));
                }
                dbManager.notifySqlExecuted("SELECT", sql + " [params: " + username + "]", duration, affected);
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthUser save(AuthUser user) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO users (username, password, full_name, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            int rows = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    try {
                        user.setId(rs.getLong(1));
                    } catch (Exception ex) {
                        if (dbManager.isOracle()) {
                            try (Statement s2 = conn.createStatement();
                                 ResultSet rs2 = s2.executeQuery("SELECT seq_users.CURRVAL FROM dual")) {
                                if (rs2.next()) {
                                    user.setId(rs2.getLong(1));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("INSERT", sql + " [username: " + user.getUsername() + "]", duration, rows);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AuthUser> findById(Long id) {
        long start = System.currentTimeMillis();
        String sql = "SELECT id, username, password, full_name, role, status, created_at FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                long duration = System.currentTimeMillis() - start;
                Optional<AuthUser> result = Optional.empty();
                int rows = 0;
                if (rs.next()) {
                    rows = 1;
                    result = Optional.of(mapResultSetToUser(rs));
                }
                dbManager.notifySqlExecuted("SELECT", sql + " [id: " + id + "]", duration, rows);
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AuthUser> findAll() {
        long start = System.currentTimeMillis();
        List<AuthUser> list = new ArrayList<>();
        String sql = "SELECT id, username, password, full_name, role, status, created_at FROM users ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("SELECT", sql, duration, list.size());
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(AuthUser user) {
        long start = System.currentTimeMillis();
        String sql = "UPDATE users SET full_name = ?, role = ?, status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getStatus());
            ps.setLong(4, user.getId());

            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("UPDATE", sql + " [id: " + user.getId() + "]", duration, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        long start = System.currentTimeMillis();
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("DELETE", sql + " [id: " + id + "]", duration, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    private AuthUser mapResultSetToUser(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new AuthUser(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("status"),
                created
        );
    }
}
