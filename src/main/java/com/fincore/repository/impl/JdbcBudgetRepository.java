package com.fincore.repository.impl;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Budget;
import com.fincore.repository.BudgetRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of BudgetRepository.
 * Manages category budgets and calculates actual real-time expenditure against limits.
 */
public class JdbcBudgetRepository implements BudgetRepository {

    private final DatabaseManager dbManager;

    public JdbcBudgetRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Budget save(Budget budget) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO budgets (category, monthly_limit, period_month) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, budget.getCategory());
            ps.setDouble(2, budget.getMonthlyLimit());
            ps.setString(3, budget.getPeriodMonth() != null ? budget.getPeriodMonth() : "2026-09");

            int rows = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    try {
                        budget.setId(rs.getLong(1));
                    } catch (Exception ex) {
                        if (dbManager.isOracle()) {
                            try (Statement s2 = conn.createStatement();
                                 ResultSet rs2 = s2.executeQuery("SELECT seq_budgets.CURRVAL FROM dual")) {
                                if (rs2.next()) {
                                    budget.setId(rs2.getLong(1));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("INSERT", sql + " [category=" + budget.getCategory() + ", limit=$" + budget.getMonthlyLimit() + "]", duration, rows);
            return budget;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving budget: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Budget> findById(Long id) {
        long start = System.currentTimeMillis();
        String sql = "SELECT id, category, monthly_limit, period_month FROM budgets WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                long duration = System.currentTimeMillis() - start;
                Optional<Budget> result = Optional.empty();
                int rows = 0;
                if (rs.next()) {
                    rows = 1;
                    result = Optional.of(mapResultSetToBudget(rs));
                }
                dbManager.notifySqlExecuted("SELECT", sql + " [id=" + id + "]", duration, rows);
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding budget by id: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Budget> findByCategory(String category) {
        long start = System.currentTimeMillis();
        String sql = "SELECT id, category, monthly_limit, period_month FROM budgets WHERE category = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                long duration = System.currentTimeMillis() - start;
                Optional<Budget> result = Optional.empty();
                int rows = 0;
                if (rs.next()) {
                    rows = 1;
                    result = Optional.of(mapResultSetToBudget(rs));
                }
                dbManager.notifySqlExecuted("SELECT", sql + " [category=" + category + "]", duration, rows);
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding budget by category: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Budget> findAll() {
        return getBudgetsWithSpending();
    }

    @Override
    public List<Budget> getBudgetsWithSpending() {
        long start = System.currentTimeMillis();
        List<Budget> list = new ArrayList<>();
        // Relational query joining budgets with financial_records (EXPENSE) to compute real-time expenditure
        String sql = """
            SELECT b.id, b.category, b.monthly_limit, b.period_month,
                   COALESCE(SUM(fr.amount), 0.0) AS spent_total
            FROM budgets b
            LEFT JOIN financial_records fr 
                   ON LOWER(b.category) = LOWER(fr.category) 
                  AND fr.record_type = 'EXPENSE'
            GROUP BY b.id, b.category, b.monthly_limit, b.period_month
            ORDER BY b.monthly_limit DESC
        """;
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Budget b = new Budget(
                        rs.getLong("id"),
                        rs.getString("category"),
                        rs.getDouble("monthly_limit"),
                        rs.getString("period_month"),
                        rs.getDouble("spent_total")
                );
                list.add(b);
            }
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("SELECT (Budgets + Expenditure JOIN)", sql, duration, list.size());
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching budgets with spending: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Budget budget) {
        long start = System.currentTimeMillis();
        String sql = "UPDATE budgets SET monthly_limit = ?, period_month = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, budget.getMonthlyLimit());
            ps.setString(2, budget.getPeriodMonth());
            ps.setLong(3, budget.getId());

            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("UPDATE", sql + " [id=" + budget.getId() + "]", duration, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating budget: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        long start = System.currentTimeMillis();
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            long duration = System.currentTimeMillis() - start;
            dbManager.notifySqlExecuted("DELETE", sql + " [id=" + id + "]", duration, rows);
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting budget: " + e.getMessage(), e);
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        return new Budget(
                rs.getLong("id"),
                rs.getString("category"),
                rs.getDouble("monthly_limit"),
                rs.getString("period_month"),
                0.0
        );
    }
}
