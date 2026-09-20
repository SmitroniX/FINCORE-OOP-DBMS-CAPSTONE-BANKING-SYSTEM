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
 * Demonstrates Object-Oriented singleton pattern and DBMS connection encapsulation.
 * Supports Oracle 10g XE, SQLite, and MySQL.
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
     * Supports Oracle 10g XE, SQLite, and MySQL.
     */
    public Connection getConnection() throws SQLException {
        Connection conn;
        String type = config.getDbType();
        if ("oracle".equals(type) || "mysql".equals(type)) {
            conn = DriverManager.getConnection(config.getJdbcUrl(), config.getDbUser(), config.getDbPassword());
        } else {
            conn = DriverManager.getConnection(config.getJdbcUrl());
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return conn;
    }

    /**
     * Initializes the database schema and default seed data.
     */
    public void initializeDatabase() {
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
