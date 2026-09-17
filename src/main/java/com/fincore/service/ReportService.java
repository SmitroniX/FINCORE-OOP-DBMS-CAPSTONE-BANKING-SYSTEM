package com.fincore.service;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Account;
import com.fincore.model.AuditLog;
import com.fincore.model.CustomerSummaryDTO;
import com.fincore.model.Transaction;
import com.fincore.repository.AccountRepository;
import com.fincore.repository.AuditLogRepository;
import com.fincore.repository.CustomerRepository;
import com.fincore.repository.TransactionRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Service showcasing advanced DBMS Relational Analytics, Aggregations, and Analytical DTOs.
 */
public class ReportService {

    private final DatabaseManager dbManager;
    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final AuditLogRepository auditRepo;

    public ReportService(DatabaseManager dbManager,
                         CustomerRepository customerRepo,
                         AccountRepository accountRepo,
                         TransactionRepository txRepo,
                         AuditLogRepository auditRepo) {
        this.dbManager = dbManager;
        this.customerRepo = customerRepo;
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Executes multi-table relational JOIN and GROUP BY query for all customer portfolios.
     */
    public List<CustomerSummaryDTO> getCustomerPortfolioReport() {
        return customerRepo.getCustomerPortfolioSummaries();
    }

    /**
     * Aggregates total bank deposits and breakdown by Account Type.
     */
    public Map<String, Object> getBankLiquidityMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        String sql = """
            SELECT 
                COUNT(account_number) AS total_accounts,
                COALESCE(SUM(balance), 0.0) AS total_deposits,
                COALESCE(AVG(balance), 0.0) AS avg_balance,
                COALESCE(SUM(CASE WHEN account_type = 'SAVINGS' THEN balance ELSE 0 END), 0.0) AS savings_deposits,
                COALESCE(SUM(CASE WHEN account_type = 'CHECKING' THEN balance ELSE 0 END), 0.0) AS checking_deposits
            FROM accounts
        """;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                metrics.put("total_accounts", rs.getInt("total_accounts"));
                metrics.put("total_deposits", rs.getDouble("total_deposits"));
                metrics.put("avg_balance", rs.getDouble("avg_balance"));
                metrics.put("savings_deposits", rs.getDouble("savings_deposits"));
                metrics.put("checking_deposits", rs.getDouble("checking_deposits"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error computing bank liquidity metrics: " + e.getMessage(), e);
        }
        return metrics;
    }

    /**
     * Retrieves account statement transactions for a given account.
     */
    public List<Transaction> getAccountStatement(String accountNumber) {
        return txRepo.findByAccountNumber(accountNumber);
    }

    /**
     * Retrieves recent audit compliance logs.
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditRepo.findRecentLogs(limit);
    }
}
