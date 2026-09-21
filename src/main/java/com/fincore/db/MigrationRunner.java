package com.fincore.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Automates DBMS schema generation and initial data seeding on application startup.
 * Supports Oracle 10g XE (including PL/SQL Triggers and Sequences), SQLite, and MySQL.
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

            boolean isOracle = "oracle".equalsIgnoreCase(dbManager.getConfig().getDbType());
            List<String> statements = parseStatements(in, isOracle);

            try (Connection conn = dbManager.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.execute(trimmed);
                        } catch (SQLException ex) {
                            String msg = ex.getMessage().toLowerCase();
                            // If table, sequence, or record already exists, proceed gracefully
                            if (!msg.contains("already exists") &&
                                !msg.contains("name is already used") &&
                                !msg.contains("duplicate") &&
                                !msg.contains("unique constraint")) {
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

    private List<String> parseStatements(InputStream in, boolean isOracle) throws Exception {
        List<String> statements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder currentStmt = new StringBuilder();
            boolean insidePlSql = false;
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.startsWith("//")) {
                    continue;
                }
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (isOracle) {
                    String upper = trimmed.toUpperCase();
                    if (upper.startsWith("CREATE OR REPLACE TRIGGER") || upper.startsWith("CREATE TRIGGER")
                        || upper.startsWith("CREATE OR REPLACE PACKAGE") || upper.startsWith("CREATE PACKAGE")
                        || upper.startsWith("CREATE OR REPLACE PROCEDURE") || upper.startsWith("CREATE PROCEDURE")
                        || upper.startsWith("CREATE OR REPLACE FUNCTION") || upper.startsWith("CREATE FUNCTION")) {
                        insidePlSql = true;
                    }

                    if (insidePlSql) {
                        if (trimmed.equals("/")) {
                            statements.add(currentStmt.toString().trim());
                            currentStmt.setLength(0);
                            insidePlSql = false;
                            continue;
                        } else {
                            currentStmt.append(line).append("\n");
                            continue;
                        }
                    }
                }

                if (trimmed.endsWith(";")) {
                    currentStmt.append(line.substring(0, line.lastIndexOf(';')));
                    statements.add(currentStmt.toString().trim());
                    currentStmt.setLength(0);
                } else if (trimmed.equals("/")) {
                    if (currentStmt.length() > 0) {
                        statements.add(currentStmt.toString().trim());
                        currentStmt.setLength(0);
                    }
                } else {
                    currentStmt.append(line).append("\n");
                }
            }

            if (currentStmt.length() > 0 && !currentStmt.toString().trim().isEmpty()) {
                statements.add(currentStmt.toString().trim());
            }
        }
        return statements;
    }
}
