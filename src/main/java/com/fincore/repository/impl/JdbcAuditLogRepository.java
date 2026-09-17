package com.fincore.repository.impl;

import com.fincore.db.DatabaseManager;
import com.fincore.model.AuditLog;
import com.fincore.repository.AuditLogRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of AuditLogRepository tracking security actions.
 */
public class JdbcAuditLogRepository implements AuditLogRepository {

    private final DatabaseManager dbManager;

    public JdbcAuditLogRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void log(String action, String entityType, String entityId, String performedBy, String details) {
        try (Connection conn = dbManager.getConnection()) {
            log(conn, action, entityType, entityId, performedBy, details);
        } catch (SQLException e) {
            System.err.println("[AuditLog] Failed to record audit log: " + e.getMessage());
        }
    }

    @Override
    public void log(Connection conn, String action, String entityType, String entityId, String performedBy, String details) {
        String sql = "INSERT INTO audit_logs (action, entity_type, entity_id, performed_by, details) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, entityType);
            ps.setString(3, entityId);
            ps.setString(4, performedBy);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuditLog] Failed to insert audit log in connection: " + e.getMessage());
        }
    }

    @Override
    public AuditLog save(AuditLog entity) {
        log(entity.getAction(), entity.getEntityType(), entity.getEntityId(), entity.getPerformedBy(), entity.getDetails());
        return entity;
    }

    @Override
    public Optional<AuditLog> findById(Long id) {
        String sql = "SELECT id, action, entity_type, entity_id, performed_by, details, timestamp FROM audit_logs WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding audit log by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<AuditLog> findRecentLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT id, action, entity_type, entity_id, performed_by, details, timestamp FROM audit_logs ORDER BY timestamp DESC, id DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding recent audit logs: " + e.getMessage(), e);
        }
        return logs;
    }

    @Override
    public List<AuditLog> findAll() {
        return findRecentLogs(100);
    }

    @Override
    public boolean update(AuditLog entity) {
        throw new UnsupportedOperationException("Audit logs are append-only and immutable.");
    }

    @Override
    public boolean deleteById(Long id) {
        throw new UnsupportedOperationException("Audit logs cannot be deleted.");
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("timestamp");
        LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new AuditLog(
            rs.getLong("id"),
            rs.getString("action"),
            rs.getString("entity_type"),
            rs.getString("entity_id"),
            rs.getString("performed_by"),
            rs.getString("details"),
            timestamp
        );
    }
}
