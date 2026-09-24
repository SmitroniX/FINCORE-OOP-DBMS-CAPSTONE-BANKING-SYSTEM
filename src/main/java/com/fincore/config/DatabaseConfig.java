package com.fincore.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager for database properties.
 * Reads configurations from db.properties and provides database connection metadata.
 * Defaults to Oracle 10g XE with full JDBC support.
 */
public class DatabaseConfig {

    private static DatabaseConfig instance;
    private final Properties properties = new Properties();

    private DatabaseConfig() {
        loadProperties();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                System.err.println("[DatabaseConfig] db.properties not found on classpath, defaulting to Oracle 10g XE.");
                properties.setProperty("db.type", "oracle");
                properties.setProperty("oracle.url", "jdbc:oracle:thin:@localhost:1521:xe");
                properties.setProperty("oracle.user", "system");
                properties.setProperty("oracle.password", "oracle");
                properties.setProperty("oracle.fallback.sqlite", "false");
            }
        } catch (IOException e) {
            System.err.println("[DatabaseConfig] Failed to load db.properties: " + e.getMessage());
        }
    }

    public String resolveValue(String sysProp, String envVar, String propKey, String fallback) {
        String val = System.getProperty(sysProp);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = System.getenv(envVar);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = properties.getProperty(propKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        return fallback;
    }

    public String getDbType() {
        return resolveValue("db.type", "DB_TYPE", "db.type", "oracle").trim().toLowerCase();
    }

    public void setDbType(String dbType) {
        properties.setProperty("db.type", dbType != null ? dbType.trim().toLowerCase() : "oracle");
    }

    public String getJdbcUrl() {
        String directUrl = resolveValue("db.url", "DB_URL", "db.url", null);
        if (directUrl != null) {
            return directUrl;
        }
        String type = getDbType();
        if ("oracle".equals(type)) {
            return resolveValue("oracle.url", "ORACLE_URL", "oracle.url", "jdbc:oracle:thin:@localhost:1521/FREEPDB1");
        } else if ("mysql".equals(type)) {
            return resolveValue("mysql.url", "MYSQL_URL", "mysql.url", "jdbc:mysql://localhost:3306/fincore_db");
        }
        return resolveValue("sqlite.url", "SQLITE_URL", "sqlite.url", "jdbc:sqlite:fincore_banking.db");
    }

    public String getDbUrl() {
        return getJdbcUrl();
    }

    public String getDriverClass() {
        String type = getDbType();
        if ("oracle".equals(type)) {
            return "oracle.jdbc.OracleDriver";
        } else if ("mysql".equals(type)) {
            return "com.mysql.cj.jdbc.Driver";
        }
        return "org.sqlite.JDBC";
    }

    public String getDbUser() {
        String directUser = resolveValue("db.user", "DB_USER", "db.user", null);
        if (directUser != null) {
            return directUser;
        }
        String type = getDbType();
        if ("oracle".equals(type)) {
            return resolveValue("oracle.user", "ORACLE_USER", "oracle.user", "fincore_user");
        } else if ("mysql".equals(type)) {
            return resolveValue("mysql.user", "MYSQL_USER", "mysql.user", "root");
        }
        return "";
    }

    public String getDbPassword() {
        String directPass = resolveValue("db.password", "DB_PASSWORD", "db.password", null);
        if (directPass != null) {
            return directPass;
        }
        String type = getDbType();
        if ("oracle".equals(type)) {
            return resolveValue("oracle.password", "ORACLE_PASSWORD", "oracle.password", "fincore_pass");
        } else if ("mysql".equals(type)) {
            return resolveValue("mysql.password", "MYSQL_PASSWORD", "mysql.password", "");
        }
        return "";
    }

    public boolean isMigrationEnabled() {
        String val = resolveValue("db.migrate", "DB_MIGRATE", "db.migrate", "true");
        return !"false".equalsIgnoreCase(val);
    }

    public String getOracleAltUrl() {
        return properties.getProperty("oracle.alt.url", "jdbc:oracle:thin:@localhost:1521:xe");
    }

    public String getOracleAltUser() {
        return properties.getProperty("oracle.alt.user", "system");
    }

    public String getOracleAltPassword() {
        return properties.getProperty("oracle.alt.password", "oracle");
    }

    public String getSchemaFile() {
        String type = getDbType();
        if ("oracle".equals(type)) {
            return "schema-oracle.sql";
        }
        return "schema-sqlite.sql";
    }

    public String getSeedFile() {
        String type = getDbType();
        if ("oracle".equals(type)) {
            return "seed-oracle.sql";
        }
        return "seed.sql";
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
