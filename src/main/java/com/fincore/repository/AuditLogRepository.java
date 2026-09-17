package com.fincore.repository;

import com.fincore.model.AuditLog;

import java.sql.Connection;
import java.util.List;

/**
 * Data access contract for security audit logs.
 */
public interface AuditLogRepository extends CrudRepository<AuditLog, Long> {

    void log(String action, String entityType, String entityId, String performedBy, String details);

    void log(Connection conn, String action, String entityType, String entityId, String performedBy, String details);

    List<AuditLog> findRecentLogs(int limit);
}
