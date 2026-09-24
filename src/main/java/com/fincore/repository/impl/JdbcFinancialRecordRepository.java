package com.fincore.repository.impl;

import com.fincore.db.DatabaseManager;
import com.fincore.model.FinancialRecord;
import com.fincore.repository.FinancialRecordRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JDBC Implementation of FinancialRecordRepository providing full CRUD operations:
 * INSERT (Create), SELECT (Read), UPDATE (Update), and DELETE (Delete).
 * Fully compatible with Oracle 10g XE, SQLite, and MySQL.
 */
public class JdbcFinancialRecordRepository implements FinancialRecordRepository {

    private final DatabaseManager dbManager;

    public JdbcFinancialRecordRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public FinancialRecord save(FinancialRecord record) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO financial_records (record_type, category, amount, account_number, description, record_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, record.getRecordType().name());
            ps.setString(2, record.getCategory());
            ps.setDouble(3, record.getAmount());
            ps.setString(4, record.getAccountNumber());
            ps.setString(5, record.getDescription());
            if (record.getRecordDate() != null) {
                ps.setDate(6, java.sql.Date.valueOf(record.getRecordDate()));
            } else {
                ps.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
            }

            int rows = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    try {
                        record.setId(rs.getLong(1));
                    } catch (Exception ex) {
                        if (dbManager.isOracle()) {
                            try (Statement s2 = conn.createStatement();
                                 ResultSet rs2 = s2.executeQuery("SELECT seq_fin_records.CURRVAL FROM dual")) {
                                if (rs2.next()) {
                                    record.setId(rs2.getLong(1));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("INSERT", sql + " [type=" + record.getRecordType() + ", amount=$" + record.getAmount() + ", category=" + record.getCategory() + "]", duration, rows);
            return record;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting financial record: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<FinancialRecord> findById(Long id) {
        long start = System.currentTimeMillis();
        String sql = "SELECT id, record_type, category, amount, account_number, description, record_date, created_at " +
                     "FROM financial_records WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                long duration = System.currentTimeMillis() - start;
                Optional<FinancialRecord> result = Optional.empty();
                int rows = 0;
                if (rs.next()) {
                    rows = 1;
                    result = Optional.of(mapResultSetToRecord(rs));
                }
                dbManager.notifySqlExecuted("SELECT", sql + " [id=" + id + "]", duration, rows);
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error selecting financial record by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FinancialRecord> findAll() {
        long start = System.currentTimeMillis();
        List<FinancialRecord> list = new ArrayList<>();
        String sql = "SELECT id, record_type, category, amount, account_number, description, record_date, created_at " +
                     "FROM financial_records ORDER BY record_date DESC, id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("SELECT (All Records)", sql, duration, list.size());
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all financial records: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FinancialRecord> findByType(FinancialRecord.Type type) {
        long start = System.currentTimeMillis();
        List<FinancialRecord> list = new ArrayList<>();
        String sql = "SELECT id, record_type, category, amount, account_number, description, record_date, created_at " +
                     "FROM financial_records WHERE record_type = ? ORDER BY record_date DESC, id DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("SELECT (By Type)", sql + " [type=" + type + "]", duration, list.size());
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching financial records by type: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FinancialRecord> findByCategory(String category) {
        long start = System.currentTimeMillis();
        List<FinancialRecord> list = new ArrayList<>();
        String sql = "SELECT id, record_type, category, amount, account_number, description, record_date, created_at " +
                     "FROM financial_records WHERE category = ? ORDER BY record_date DESC, id DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("SELECT (By Category)", sql + " [category=" + category + "]", duration, list.size());
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching financial records by category: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(FinancialRecord record) {
        long start = System.currentTimeMillis();
        String sql = "UPDATE financial_records SET record_type = ?, category = ?, amount = ?, account_number = ?, description = ?, record_date = ? " +
                     "WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, record.getRecordType().name());
            ps.setString(2, record.getCategory());
            ps.setDouble(3, record.getAmount());
            ps.setString(4, record.getAccountNumber());
            ps.setString(5, record.getDescription());
            if (record.getRecordDate() != null) {
                ps.setDate(6, java.sql.Date.valueOf(record.getRecordDate()));
            } else {
                ps.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
            }
            ps.setLong(7, record.getId());

            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("UPDATE", sql + " [id=" + record.getId() + ", amount=$" + record.getAmount() + "]", duration, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating financial record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        long start = System.currentTimeMillis();
        String sql = "DELETE FROM financial_records WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("DELETE", sql + " [id=" + id + "]", duration, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting financial record: " + e.getMessage(), e);
        }
    }

    @Override
    public double getTotalIncome() {
        return getAggregateSum("SELECT COALESCE(SUM(amount), 0.0) FROM financial_records WHERE record_type = 'INCOME'", "SELECT SUM(Income)");
    }

    @Override
    public double getTotalExpenses() {
        return getAggregateSum("SELECT COALESCE(SUM(amount), 0.0) FROM financial_records WHERE record_type = 'EXPENSE'", "SELECT SUM(Expense)");
    }

    private double getAggregateSum(String sql, String opLabel) {
        long start = System.currentTimeMillis();
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            double sum = 0.0;
            if (rs.next()) {
                sum = rs.getDouble(1);
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted(opLabel, sql, duration, 1);
            return sum;
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating aggregate sum: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Double> getCategoryBreakdown(FinancialRecord.Type type) {
        long start = System.currentTimeMillis();
        Map<String, Double> breakdown = new HashMap<>();
        String sql = "SELECT category, COALESCE(SUM(amount), 0.0) AS cat_total " +
                     "FROM financial_records WHERE record_type = ? GROUP BY category ORDER BY cat_total DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    breakdown.put(rs.getString("category"), rs.getDouble("cat_total"));
                }
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("SELECT (Category Breakdown)", sql + " [type=" + type + "]", duration, breakdown.size());
            return breakdown;
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating category breakdown: " + e.getMessage(), e);
        }
    }

    private FinancialRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String typeStr = rs.getString("record_type");
        String category = rs.getString("category");
        double amount = rs.getDouble("amount");
        String accountNumber = rs.getString("account_number");
        String description = rs.getString("description");
        LocalDate date = LocalDate.now();
        try {
            java.sql.Date d = rs.getDate("record_date");
            if (d != null) {
                date = d.toLocalDate();
            } else {
                String dateStr = rs.getString("record_date");
                if (dateStr != null && !dateStr.isEmpty()) {
                    date = LocalDate.parse(dateStr.substring(0, 10));
                }
            }
        } catch (Exception ignored) {
        }
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        FinancialRecord.Type type = FinancialRecord.Type.valueOf(typeStr.toUpperCase());
        return new FinancialRecord(id, type, category, amount, accountNumber, description, date, created);
    }
}
