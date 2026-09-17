package com.fincore.repository.impl;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Customer;
import com.fincore.model.CustomerSummaryDTO;
import com.fincore.repository.CustomerRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of CustomerRepository.
 * Uses PreparedStatements to guarantee protection against SQL Injection attacks.
 */
public class JdbcCustomerRepository implements CustomerRepository {

    private final DatabaseManager dbManager;

    public JdbcCustomerRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (customer_code, name, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getUserCode());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setString(5, "CUSTOMER");
            ps.setString(6, customer.getStatus() != null ? customer.getStatus() : "ACTIVE");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setId(rs.getLong(1));
                }
            }
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        String sql = "SELECT id, customer_code, name, email, phone, role, status, created_at FROM customers WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByCustomerCode(String customerCode) {
        String sql = "SELECT id, customer_code, name, email, phone, role, status, created_at FROM customers WHERE customer_code = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT id, customer_code, name, email, phone, role, status, created_at FROM customers WHERE email = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, customer_code, name, email, phone, role, status, created_at FROM customers ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all customers: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getStatus());
            ps.setLong(5, customer.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating customer: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting customer: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CustomerSummaryDTO> getCustomerPortfolioSummaries() {
        List<CustomerSummaryDTO> summaries = new ArrayList<>();
        String sql = """
            SELECT c.id AS cust_id,
                   c.customer_code,
                   c.name,
                   c.email,
                   COALESCE(acc.total_accounts, 0) AS total_accounts,
                   COALESCE(acc.total_balance, 0.0) AS total_balance,
                   COALESCE(tx.total_transactions, 0) AS total_transactions
            FROM customers c
            LEFT JOIN (
                SELECT customer_id,
                       COUNT(account_number) AS total_accounts,
                       SUM(balance) AS total_balance
                FROM accounts
                GROUP BY customer_id
            ) acc ON c.id = acc.customer_id
            LEFT JOIN (
                SELECT a.customer_id,
                       COUNT(t.id) AS total_transactions
                FROM accounts a
                JOIN transactions t ON a.account_number = t.account_number
                GROUP BY a.customer_id
            ) tx ON c.id = tx.customer_id
            ORDER BY total_balance DESC
        """;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                summaries.add(new CustomerSummaryDTO(
                    rs.getLong("cust_id"),
                    rs.getString("customer_code"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getInt("total_accounts"),
                    rs.getDouble("total_balance"),
                    rs.getInt("total_transactions")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching customer portfolio summaries: " + e.getMessage(), e);
        }
        return summaries;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new Customer(
            rs.getLong("id"),
            rs.getString("customer_code"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("status"),
            created
        );
    }
}
