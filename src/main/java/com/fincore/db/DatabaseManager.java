package com.fincore.db;

import com.fincore.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages JDBC connections, connection lifecycles, and transaction boundaries.
 * Defaults to Oracle 10g XE with enterprise connection handling and fallback capability.
 */
public class DatabaseManager {

    public interface SqlListener {
        void onSqlExecuted(String operation, String sql, long executionTimeMs, int affectedRows);
    }

    private static DatabaseManager instance;
    private final DatabaseConfig config;
    private final List<SqlListener> sqlListeners = new CopyOnWriteArrayList<>();

    private DatabaseManager() {
        this.config = DatabaseConfig.getInstance();
        initDrivers();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initDrivers() {
        try {
            String type = config.getDbType();
            if ("oracle".equals(type)) {
                Class.forName("oracle.jdbc.OracleDriver");
            } else if ("mysql".equals(type)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                Class.forName("org.sqlite.JDBC");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] JDBC Driver not found for " + config.getDbType() + ": " + e.getMessage());
        }
    }

    /**
     * Obtains a new JDBC connection based on the active configuration.
     * Defaults to Oracle 10g XE.
     */
    public Connection getConnection() throws SQLException {
        Connection conn;
        String type = config.getDbType();
        if ("oracle".equals(type)) {
            try {
                conn = DriverManager.getConnection(config.getJdbcUrl(), config.getDbUser(), config.getDbPassword());
                return conn;
            } catch (SQLException e1) {
                // Attempt classic Oracle XE port 1521
                try {
                    conn = DriverManager.getConnection(config.getOracleAltUrl(), config.getOracleAltUser(), config.getOracleAltPassword());
                    return conn;
                } catch (SQLException e2) {
                    if ("true".equalsIgnoreCase(config.getProperty("oracle.fallback.sqlite", "true"))) {
                        System.out.println("[DatabaseManager] Primary DBMS Engine: Oracle Database in Docker (" + config.getJdbcUrl() + ").");
                        System.out.println("[DatabaseManager] Notice: Oracle container port 1521 not currently ready (" + e1.getMessage() + ").");
                        System.out.println("[DatabaseManager] Activating automated embedded SQLite engine to maintain 100% operational availability.");
                        config.setDbType("sqlite");
                        initDrivers();
                        return getConnection();
                    }
                    throw e1;
                }
            }
        } else if ("mysql".equals(type)) {
            conn = DriverManager.getConnection(config.getJdbcUrl(), config.getDbUser(), config.getDbPassword());
            return conn;
        } else {
            conn = DriverManager.getConnection(config.getJdbcUrl());
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        }
    }

    public boolean isOracle() {
        return "oracle".equalsIgnoreCase(config.getDbType());
    }

    public String getActiveEngineDescription() {
        if (isOracle()) {
            return "Oracle Database (Docker / XE 1521)";
        } else if ("mysql".equalsIgnoreCase(config.getDbType())) {
            return "MySQL Server (Port 3306)";
        } else {
            return "SQLite Embedded (Zero-Config Engine)";
        }
    }

    /**
     * Initializes the database schema and default seed data.
     */
    public void initializeDatabase() {
        try {
            // Probe connection to trigger driver load and fallback if Oracle is offline
            try (Connection testConn = getConnection()) {
                // Connection successfully verified
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Connection test notice: " + e.getMessage());
        }

        MigrationRunner runner = new MigrationRunner(this);
        runner.runMigrations();
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public void addSqlListener(SqlListener listener) {
        sqlListeners.add(listener);
    }

    public void removeSqlListener(SqlListener listener) {
        sqlListeners.remove(listener);
    }

    public void notifySqlExecuted(String operation, String sql, long executionTimeMs, int affectedRows) {
        for (SqlListener listener : sqlListeners) {
            try {
                listener.onSqlExecuted(operation, sql, executionTimeMs, affectedRows);
            } catch (Exception ignored) {
            }
        }
    }
}
