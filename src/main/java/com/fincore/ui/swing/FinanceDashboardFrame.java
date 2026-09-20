package com.fincore.ui.swing;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Account;
import com.fincore.model.AuthUser;
import com.fincore.model.Budget;
import com.fincore.model.FinancialRecord;
import com.fincore.model.Transaction;
import com.fincore.service.AuthService;
import com.fincore.service.BankingService;
import com.fincore.service.CustomerService;
import com.fincore.service.FinanceService;
import com.fincore.service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Main Java Swing Finance Dashboard showcasing:
 * - Income, Expenses, Accounts, Budget, Transactions, Reports
 * - Complete CRUD Module (INSERT, SELECT, UPDATE, DELETE)
 * - Real-time Live SQL / DML Execution Inspector
 */
public class FinanceDashboardFrame extends JFrame implements DatabaseManager.SqlListener {

    private final AuthService authService;
    private final FinanceService financeService;
    private final BankingService bankingService;
    private final CustomerService customerService;
    private final ReportService reportService;
    private final Runnable onLogoutCallback;

    // Overview metric labels
    private JLabel totalBalanceLabel;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel netSavingsLabel;

    // CRUD Table & Model
    private JTable crudTable;
    private DefaultTableModel crudTableModel;
    private JComboBox<String> filterTypeCombo;

    // Accounts & Budgets & Transactions tables
    private JTable accountsTable;
    private DefaultTableModel accountsModel;

    private JTable budgetTable;
    private DefaultTableModel budgetModel;

    private JTable txTable;
    private DefaultTableModel txModel;

    // Reports table
    private JTable reportTable;
    private DefaultTableModel reportModel;

    // Live SQL Console
    private JTextArea sqlLogArea;

    public FinanceDashboardFrame(AuthService authService,
                                 FinanceService financeService,
                                 BankingService bankingService,
                                 CustomerService customerService,
                                 ReportService reportService,
                                 Runnable onLogoutCallback) {
        super("FinCore - Enterprise Finance Dashboard (OOP & DBMS Capstone)");
        this.authService = authService;
        this.financeService = financeService;
        this.bankingService = bankingService;
        this.customerService = customerService;
        this.reportService = reportService;
        this.onLogoutCallback = onLogoutCallback;

        setSize(1180, 780);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();

        // Register SQL listener to display real-time DML/SQL statements
        DatabaseManager.getInstance().addSqlListener(this);

        // Load initial data into all tabs
        refreshAllData();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));

        // 1. Top Header Banner
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Tabs (Dashboard, CRUD Income/Expenses, Accounts, Budget, Transactions, Reports)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("📊 Dashboard Overview", createOverviewPanel());
        tabbedPane.addTab("💰 Income & Expenses (CRUD)", createCrudPanel());
        tabbedPane.addTab("🏦 Accounts", createAccountsPanel());
        tabbedPane.addTab("🎯 Budget Tracker", createBudgetPanel());
        tabbedPane.addTab("📜 Transactions Ledger", createTransactionsPanel());
        tabbedPane.addTab("📈 Financial Reports", createReportsPanel());

        // 3. Bottom Live SQL/DML Inspector Panel
        JPanel bottomPanel = createSqlInspectorPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, bottomPanel);
        splitPane.setResizeWeight(0.72);
        splitPane.setDividerSize(6);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBackground(new Color(24, 43, 73));
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Title and Subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("FinCore Financial Management System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        String dbType = DatabaseManager.getInstance().getConfig().getDbType().toUpperCase();
        JLabel dbTag = new JLabel("DBMS: Connected to " + dbType + " via JDBC | Oracle 10g XE Ready");
        dbTag.setForeground(new Color(176, 196, 222));
        dbTag.setFont(new Font("SansSerif", Font.PLAIN, 12));

        titlePanel.add(titleLabel);
        titlePanel.add(dbTag);

        // User info and Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        userPanel.setOpaque(false);

        AuthUser user = authService.getCurrentUser();
        String userInfo = user != null ? user.getFullName() + " (" + user.getRole() + ")" : "Authenticated User";
        JLabel userLabel = new JLabel("👤 " + userInfo);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(211, 47, 47));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            authService.logout();
            DatabaseManager.getInstance().removeSqlListener(this);
            dispose();
            if (onLogoutCallback != null) {
                onLogoutCallback.run();
            }
        });

        userPanel.add(userLabel);
        userPanel.add(logoutBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    // Tab 1: Dashboard Overview
    // =========================================================================
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top 4 Metric Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 10));
        totalBalanceLabel = new JLabel("$0.00", SwingConstants.CENTER);
        totalIncomeLabel = new JLabel("$0.00", SwingConstants.CENTER);
        totalExpenseLabel = new JLabel("$0.00", SwingConstants.CENTER);
        netSavingsLabel = new JLabel("$0.00", SwingConstants.CENTER);

        cardsGrid.add(createMetricCard("TOTAL ACCOUNT LIQUIDITY", totalBalanceLabel, new Color(33, 150, 243)));
        cardsGrid.add(createMetricCard("TOTAL RECORDED INCOME", totalIncomeLabel, new Color(46, 125, 50)));
        cardsGrid.add(createMetricCard("TOTAL EXPENSES", totalExpenseLabel, new Color(198, 40, 40)));
        cardsGrid.add(createMetricCard("NET CASH FLOW / SAVINGS", netSavingsLabel, new Color(123, 31, 162)));

        panel.add(cardsGrid, BorderLayout.NORTH);

        // Center split: Quick recent activity summary
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 10));

        // Quick action helper box
        JPanel quickBox = new JPanel(new BorderLayout(10, 10));
        quickBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), "Capstone Quick Actions & Demonstration Guide"));
        JTextArea guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        guideArea.setText(
                "===========================================================\n" +
                " CAPSTONE CRUD & DATABASE DEMONSTRATION WORKFLOW\n" +
                "===========================================================\n\n" +
                "1. Click 'Income & Expenses (CRUD)' Tab:\n" +
                "   - Click '+ Add Record' to execute SQL INSERT.\n" +
                "   - View records populated in JTable (SQL SELECT).\n" +
                "   - Select a row & click 'Edit Record' to execute SQL UPDATE.\n" +
                "   - Click 'Delete Record' to execute SQL DELETE.\n\n" +
                "2. Observe the Live SQL / DML Inspector Panel at the bottom:\n" +
                "   - Every JDBC query is logged with execution duration (ms).\n" +
                "   - Shows exact SQL syntax: INSERT, SELECT, UPDATE, DELETE.\n\n" +
                "3. Relational DBMS Architecture:\n" +
                "   - Foreign key integrity cascading between Accounts & Records.\n" +
                "   - Multi-table JOINs for Budget expenditure and Portfolios.\n" +
                "   - Compatible with Oracle 10g XE (schema-oracle10g.sql)\n" +
                "     and zero-configuration embedded SQLite."
        );
        quickBox.add(new JScrollPane(guideArea), BorderLayout.CENTER);
        centerPanel.add(quickBox);

        // Quick summary table
        JPanel summaryBox = new JPanel(new BorderLayout(5, 5));
        summaryBox.setBorder(BorderFactory.createTitledBorder("System Highlights"));
        JTextArea highlights = new JTextArea();
        highlights.setEditable(false);
        highlights.setFont(new Font("SansSerif", Font.PLAIN, 13));
        highlights.setText(
                "\n  • Database Engine: " + DatabaseManager.getInstance().getConfig().getDbType().toUpperCase() + "\n" +
                "  • Driver Class: " + (DatabaseManager.getInstance().getConfig().getDbType().equals("oracle") ? "oracle.jdbc.OracleDriver" : "org.sqlite.JDBC") + "\n" +
                "  • ACID Transaction Support: ENABLED (Commit & Rollback)\n" +
                "  • PreparedStatements SQL Injection Protection: 100% Parameterized\n" +
                "  • Interactive Swing GUI with live event binding\n" +
                "  • Enterprise double-entry ledger & compliance logging\n"
        );
        summaryBox.add(highlights, BorderLayout.CENTER);
        centerPanel.add(summaryBox);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLbl.setForeground(Color.DARK_GRAY);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // Tab 2: Income & Expenses (The Complete CRUD Module)
    // =========================================================================
    private JPanel createCrudPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Toolbar: INSERT, UPDATE, DELETE, SELECT (Refresh)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton addBtn = new JButton("➕ Add Record (INSERT)");
        addBtn.setBackground(new Color(46, 125, 50));
        addBtn.setForeground(Color.BLACK);
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton editBtn = new JButton("✏️ Edit Record (UPDATE)");
        editBtn.setBackground(new Color(25, 118, 210));
        editBtn.setForeground(Color.BLACK);
        editBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton deleteBtn = new JButton("🗑️ Delete Record (DELETE)");
        deleteBtn.setBackground(new Color(198, 40, 40));
        deleteBtn.setForeground(Color.BLACK);
        deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton refreshBtn = new JButton("🔄 Refresh (SELECT)");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));

        filterTypeCombo = new JComboBox<>(new String[]{"All Records", "INCOME Only", "EXPENSE Only"});

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(new JLabel("Filter:"));
        toolbar.add(filterTypeCombo);
        toolbar.add(refreshBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        // CRUD JTable
        String[] columns = {"ID", "Date", "Type", "Category", "Amount ($)", "Account", "Description"};
        crudTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        crudTable = new JTable(crudTableModel);
        crudTable.setRowHeight(24);
        crudTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        crudTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Align numbers right
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        crudTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(crudTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Event Handlers
        addBtn.addActionListener(e -> {
            RecordDialog dialog = new RecordDialog(this, financeService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshCrudTable();
                refreshMetrics();
                refreshBudgetTable();
            }
        });

        editBtn.addActionListener(e -> {
            int selectedRow = crudTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a record from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Long id = (Long) crudTableModel.getValueAt(selectedRow, 0);
            financeService.getRecordById(id).ifPresent(record -> {
                RecordDialog dialog = new RecordDialog(this, financeService, record);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    refreshCrudTable();
                    refreshMetrics();
                    refreshBudgetTable();
                }
            });
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = crudTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a record from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Long id = (Long) crudTableModel.getValueAt(selectedRow, 0);
            String desc = (String) crudTableModel.getValueAt(selectedRow, 6);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to DELETE record #" + id + " (" + desc + ")?\nThis will execute a DBMS DELETE operation.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                financeService.deleteRecord(id);
                refreshCrudTable();
                refreshMetrics();
                refreshBudgetTable();
                JOptionPane.showMessageDialog(this, "Record #" + id + " deleted successfully.", "DELETE Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> {
            refreshCrudTable();
            refreshMetrics();
        });

        filterTypeCombo.addActionListener(e -> refreshCrudTable());

        return panel;
    }

    // =========================================================================
    // Tab 3: Accounts
    // =========================================================================
    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"Account Number", "Customer ID", "Type", "Balance ($)", "Rate / Overdraft", "Status"};
        accountsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        accountsTable = new JTable(accountsModel);
        accountsTable.setRowHeight(24);
        panel.add(new JScrollPane(accountsTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshAccBtn = new JButton("Refresh Accounts");
        refreshAccBtn.addActionListener(e -> refreshAccountsTable());
        btnPanel.add(refreshAccBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // Tab 4: Budget Tracker
    // =========================================================================
    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"Category", "Monthly Limit ($)", "Spent ($)", "Remaining ($)", "Usage %", "Status"};
        budgetModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        budgetTable = new JTable(budgetModel);
        budgetTable.setRowHeight(24);
        panel.add(new JScrollPane(budgetTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBudgetBtn = new JButton("Recalculate Budgets");
        refreshBudgetBtn.addActionListener(e -> refreshBudgetTable());
        bottom.add(refreshBudgetBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // Tab 5: Transactions Ledger
    // =========================================================================
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"Transaction ID", "Account", "Type", "Amount ($)", "Balance After ($)", "Target", "Description", "Date"};
        txModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        txTable = new JTable(txModel);
        txTable.setRowHeight(24);
        panel.add(new JScrollPane(txTable), BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // Tab 6: Financial Reports
    // =========================================================================
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"Category", "Total Spent ($)", "Share of Expenses (%)"};
        reportModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reportTable = new JTable(reportModel);
        reportTable.setRowHeight(24);

        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        JButton refreshRptBtn = new JButton("Generate Report");
        refreshRptBtn.addActionListener(e -> refreshReportsTable());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(refreshRptBtn);
        panel.add(btnP, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // Live SQL / DML Execution Inspector Panel
    // =========================================================================
    private JPanel createSqlInspectorPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)), "🖥️ Live JDBC SQL & DML Execution Inspector (Oracle 10g XE / SQLite)"));

        sqlLogArea = new JTextArea(6, 80);
        sqlLogArea.setEditable(false);
        sqlLogArea.setBackground(new Color(20, 24, 33));
        sqlLogArea.setForeground(new Color(78, 201, 176));
        sqlLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(sqlLogArea);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        JButton clearBtn = new JButton("Clear SQL Log");
        clearBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        clearBtn.addActionListener(e -> sqlLogArea.setText(""));
        btnBar.add(clearBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // Data Refresh Methods
    // =========================================================================
    public void refreshAllData() {
        refreshMetrics();
        refreshCrudTable();
        refreshAccountsTable();
        refreshBudgetTable();
        refreshTransactionsTable();
        refreshReportsTable();
    }

    private void refreshMetrics() {
        double balance = financeService.getTotalAccountBalances();
        double income = financeService.getTotalIncome();
        double expense = financeService.getTotalExpenses();
        double savings = income - expense;

        totalBalanceLabel.setText(String.format("$%,.2f", balance));
        totalIncomeLabel.setText(String.format("$%,.2f", income));
        totalExpenseLabel.setText(String.format("$%,.2f", expense));
        netSavingsLabel.setText(String.format("$%,.2f", savings));
    }

    private void refreshCrudTable() {
        crudTableModel.setRowCount(0);
        String filter = filterTypeCombo != null ? (String) filterTypeCombo.getSelectedItem() : "All Records";

        List<FinancialRecord> records;
        if ("INCOME Only".equals(filter)) {
            records = financeService.getRecordsByType(FinancialRecord.Type.INCOME);
        } else if ("EXPENSE Only".equals(filter)) {
            records = financeService.getRecordsByType(FinancialRecord.Type.EXPENSE);
        } else {
            records = financeService.getAllRecords();
        }

        for (FinancialRecord r : records) {
            crudTableModel.addRow(new Object[]{
                    r.getId(),
                    r.getRecordDate(),
                    r.getRecordType(),
                    r.getCategory(),
                    String.format("$%,.2f", r.getAmount()),
                    r.getAccountNumber(),
                    r.getDescription()
            });
        }
    }

    private void refreshAccountsTable() {
        accountsModel.setRowCount(0);
        List<Account> accounts = financeService.getAllAccounts();
        for (Account a : accounts) {
            String rateOrLimit = a instanceof com.fincore.model.SavingsAccount sa ?
                    String.format("APR: %.2f%%", sa.getInterestRate()) :
                    String.format("Overdraft: $%,.2f", ((com.fincore.model.CheckingAccount) a).getOverdraftLimit());

            accountsModel.addRow(new Object[]{
                    a.getAccountNumber(),
                    a.getCustomerId(),
                    a.getAccountType(),
                    String.format("$%,.2f", a.getBalance()),
                    rateOrLimit,
                    a.getStatus()
            });
        }
    }

    private void refreshBudgetTable() {
        budgetModel.setRowCount(0);
        List<Budget> budgets = financeService.getAllBudgetsWithSpending();
        for (Budget b : budgets) {
            String status = b.isExceeded() ? "⚠️ EXCEEDED" : (b.getPercentageUsed() > 80 ? "⚡ WARNING" : "✅ ON TRACK");
            budgetModel.addRow(new Object[]{
                    b.getCategory(),
                    String.format("$%,.2f", b.getMonthlyLimit()),
                    String.format("$%,.2f", b.getSpentAmount()),
                    String.format("$%,.2f", b.getRemainingAmount()),
                    String.format("%.1f%%", b.getPercentageUsed()),
                    status
            });
        }
    }

    private void refreshTransactionsTable() {
        txModel.setRowCount(0);
        List<Transaction> txs = financeService.getRecentTransactions(100);
        for (Transaction t : txs) {
            txModel.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getAccountNumber(),
                    t.getType(),
                    String.format("$%,.2f", t.getAmount()),
                    String.format("$%,.2f", t.getBalanceAfter()),
                    t.getTargetAccount() != null ? t.getTargetAccount() : "-",
                    t.getDescription(),
                    t.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            });
        }
    }

    private void refreshReportsTable() {
        reportModel.setRowCount(0);
        Map<String, Double> breakdown = financeService.getCategoryBreakdown(FinancialRecord.Type.EXPENSE);
        double totalExpense = financeService.getTotalExpenses();

        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            double share = totalExpense > 0 ? (entry.getValue() / totalExpense) * 100.0 : 0.0;
            reportModel.addRow(new Object[]{
                    entry.getKey(),
                    String.format("$%,.2f", entry.getValue()),
                    String.format("%.1f%%", share)
            });
        }
    }

    // =========================================================================
    // DatabaseManager.SqlListener Implementation
    // =========================================================================
    @Override
    public void onSqlExecuted(String operation, String sql, long executionTimeMs, int affectedRows) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            String logLine = String.format("[%s] [%s] (%d ms, rows=%d) %s\n",
                    timestamp, operation, executionTimeMs, affectedRows, sql);
            sqlLogArea.append(logLine);
            sqlLogArea.setCaretPosition(sqlLogArea.getDocument().getLength());
        });
    }
}
