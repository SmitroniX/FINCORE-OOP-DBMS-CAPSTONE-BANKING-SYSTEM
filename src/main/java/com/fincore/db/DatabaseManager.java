package com.fincore.db;

import com.fincore.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages JDBC connections, connection lifecycles, and transaction boundaries.
 * Demonstrates Object-Oriented singleton pattern and DBMS connection encapsulation.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private final DatabaseConfig config;

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
            if ("mysql".equals(config.getDbType())) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                Class.forName("org.sqlite.JDBC");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Obtains a new JDBC connection based on the active configuration.
     * For SQLite, enforces foreign key constraints.
     */
    public Connection getConnection() throws SQLException {
        Connection conn;
        if ("mysql".equals(config.getDbType())) {
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
}
