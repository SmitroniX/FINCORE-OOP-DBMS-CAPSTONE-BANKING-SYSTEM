package com.fincore.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automates DBMS schema generation and initial data seeding on application startup.
 */
public class MigrationRunner {

    private final DatabaseManager dbManager;

    public MigrationRunner(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void runMigrations() {
        String schemaFile = dbManager.getConfig().getSchemaFile();
        String seedFile = dbManager.getConfig().getSeedFile();
        executeSqlScript(schemaFile, "Schema Migration");
        executeSqlScript(seedFile, "Seed Data");
    }

    private void executeSqlScript(String resourcePath, String scriptName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.out.println("[Migration] Optional script not found: " + resourcePath);
                return;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Ignore comment lines
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.startsWith("//")) {
                        continue;
                    }
                    sb.append(line).append("\n");
                }
            }

            String[] statements = sb.toString().split(";");
            try (Connection conn = dbManager.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.execute(trimmed);
                        } catch (SQLException ex) {
                            // If table or record already exists, proceed gracefully
                            if (!ex.getMessage().toLowerCase().contains("already exists") &&
                                !ex.getMessage().toLowerCase().contains("duplicate")) {
                                System.err.println("[Migration] Notice on statement: " + ex.getMessage());
                            }
                        }
                    }
                }
                System.out.println("[Migration] Successfully executed: " + scriptName + " (" + resourcePath + ")");
            }
        } catch (Exception e) {
            System.err.println("[Migration] Error running " + scriptName + ": " + e.getMessage());
        }
    }
}
