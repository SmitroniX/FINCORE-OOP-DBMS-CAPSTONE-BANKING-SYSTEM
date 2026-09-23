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
            String primaryUrl = config.getJdbcUrl();
            String user = config.getDbUser();
            String pass = config.getDbPassword();

            conn = tryConnectOracle(primaryUrl, user, pass);
            if (conn != null) {
                return conn;
            }

            // Attempt secondary/alternative Oracle URLs (e.g. CDB service /FREE or classic :xe)
            String[] altUrls = new String[] {
                "jdbc:oracle:thin:@localhost:1521/FREE",
                "jdbc:oracle:thin:@localhost:1521:FREE",
                "jdbc:oracle:thin:@localhost:1521/XEPDB1",
                config.getOracleAltUrl()
            };
            for (String altUrl : altUrls) {
                try {
                    conn = DriverManager.getConnection(altUrl, user, pass);
                    System.out.println("[DatabaseManager] Connected to Oracle Database via target: " + altUrl);
                    return conn;
                } catch (SQLException ignored) {
                }
            }

            if ("true".equalsIgnoreCase(config.getProperty("oracle.fallback.sqlite", "false"))) {
                System.out.println("[DatabaseManager] Primary DBMS Engine: Oracle Database in Docker (" + primaryUrl + ").");
                System.out.println("[DatabaseManager] Notice: Oracle service is not currently available on port 1521.");
                System.out.println("[DatabaseManager] Activating automated embedded SQLite engine to maintain 100% operational availability.");
                config.setDbType("sqlite");
                initDrivers();
                return getConnection();
            }
            throw new SQLException("Cannot establish connection to Oracle Database at " + primaryUrl + " (" + user + "). Ensure Oracle container 'fincore-oracle-db' is running!");
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

    private boolean initialized = false;

    /**
     * Initializes the database schema and default seed data.
     */
    public synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }
        try {
            // Probe connection to trigger driver load and verify readiness
            try (Connection testConn = getConnection()) {
                // Connection successfully verified
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Connection test notice: " + e.getMessage());
        }

        MigrationRunner runner = new MigrationRunner(this);
        runner.runMigrations();
        initialized = true;
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

    private Connection tryConnectOracle(String url, String user, String pass) {
        int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Connection conn = DriverManager.getConnection(url, user, pass);
                if (attempt > 1) {
                    System.out.println("[DatabaseManager] Successfully established connection to Oracle Database (" + url + ") on attempt #" + attempt + "!");
                }
                return conn;
            } catch (SQLException e) {
                boolean isStartingUp = e.getErrorCode() == 12514
                        || (e.getMessage() != null && e.getMessage().contains("ORA-12514"))
                        || (e.getMessage() != null && e.getMessage().contains("ORA-12505"))
                        || (e.getMessage() != null && e.getMessage().contains("ORA-01033"));

                if (isStartingUp && attempt < maxAttempts) {
                    System.out.println("[DatabaseManager] Oracle listener is active on port 1521, but service FREEPDB1 is still initializing (attempt "
                            + attempt + "/" + maxAttempts + ")... waiting 3s for Oracle to finish opening");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    if (attempt == 1 && !isStartingUp) {
                        System.err.println("[DatabaseManager] Oracle connection notice: " + e.getMessage());
                    }
                    break;
                }
            }
        }
        return null;
    }
}
