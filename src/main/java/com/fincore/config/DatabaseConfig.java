package com.fincore.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager for database properties.
 * Reads configurations from db.properties and provides database connection metadata.
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
                System.err.println("[DatabaseConfig] db.properties not found on classpath, using SQLite defaults.");
                properties.setProperty("db.type", "sqlite");
                properties.setProperty("sqlite.url", "jdbc:sqlite:fincore_banking.db");
            }
        } catch (IOException e) {
            System.err.println("[DatabaseConfig] Failed to load db.properties: " + e.getMessage());
        }
    }

    public String getDbType() {
        return properties.getProperty("db.type", "sqlite").trim().toLowerCase();
    }

    public String getJdbcUrl() {
        String type = getDbType();
        if ("mysql".equals(type)) {
            return properties.getProperty("mysql.url", "jdbc:mysql://localhost:3306/fincore_db");
        }
        return properties.getProperty("sqlite.url", "jdbc:sqlite:fincore_banking.db");
    }

    public String getDbUser() {
        return properties.getProperty("mysql.user", "root");
    }

    public String getDbPassword() {
        return properties.getProperty("mysql.password", "");
    }

    public String getSchemaFile() {
        return "mysql".equals(getDbType()) ? "schema-mysql.sql" : "schema-sqlite.sql";
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
