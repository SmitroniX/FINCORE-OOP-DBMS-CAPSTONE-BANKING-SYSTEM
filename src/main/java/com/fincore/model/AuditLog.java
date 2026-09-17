package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Audit log entity tracking system operations for regulatory and compliance logging.
 */
public class AuditLog {

    private Long id;
    private String action;
    private String entityType;
    private String entityId;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(Long id, String action, String entityType, String entityId, String performedBy, String details, LocalDateTime timestamp) {
        this.id = id;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public AuditLog(String action, String entityType, String entityId, String performedBy, String details) {
        this(null, action, entityType, entityId, performedBy, details, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s on %s (%s) by %s: %s",
                timestamp, action, entityType, entityId, performedBy, details);
    }
}
