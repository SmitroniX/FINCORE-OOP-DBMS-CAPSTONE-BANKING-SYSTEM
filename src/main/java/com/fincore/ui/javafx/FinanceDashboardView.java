package com.fincore.ui.javafx;

import com.fincore.db.DatabaseManager;
import com.fincore.model.*;
import com.fincore.service.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main JavaFX 21 Enterprise Finance Dashboard.
 * Includes:
 * - Income, Expenses, Accounts, Budgets, Transactions, and DBMS Reports
 * - Complete CRUD Module (INSERT, SELECT, UPDATE, DELETE)
 * - Visual Charts (PieChart, BarChart)
 * - Live SQL / PL/SQL Statement Execution Console
 */
public class FinanceDashboardView extends BorderPane implements DatabaseManager.SqlListener {

    private final AuthService authService;
    private final FinanceService financeService;
    private final BankingService bankingService;
    private final CustomerService customerService;
    private final ReportService reportService;
    private final Runnable onLogout;

    // Overview KPIs
    private Label totalIncomeLabel;
    private Label totalExpenseLabel;
    private Label netBalanceLabel;
    private Label totalAccountsLabel;
    private PieChart expensePieChart;
    private BarChart<String, Number> incomeExpenseBarChart;

    // Financial Records CRUD Table
    private TableView<FinancialRecord> crudTable;
    private ObservableList<FinancialRecord> masterRecords = FXCollections.observableArrayList();
    private FilteredList<FinancialRecord> filteredRecords;
    private ComboBox<String> filterTypeCombo;
    private TextField searchField;
    private Label crudStatusLabel;

    // Accounts Table
    private TableView<Account> accountsTable;
    private ObservableList<Account> accountsData = FXCollections.observableArrayList();

    // Budgets Table
    private TableView<Budget> budgetTable;
    private ObservableList<Budget> budgetData = FXCollections.observableArrayList();

    // Transactions Table
    private TableView<Transaction> txTable;
    private ObservableList<Transaction> txData = FXCollections.observableArrayList();

    // Reports View Table
    private TableView<Map<String, Object>> reportTableView;
    private ComboBox<String> reportSelector;

    // Live SQL / PL/SQL Console
    private TextArea sqlLogArea;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public FinanceDashboardView(AuthService authService,
                                FinanceService financeService,
                                BankingService bankingService,
                                CustomerService customerService,
                                ReportService reportService,
                                Runnable onLogout) {
        this.authService = authService;
        this.financeService = financeService;
        this.bankingService = bankingService;
        this.customerService = customerService;
        this.reportService = reportService;
        this.onLogout = onLogout;

        getStyleClass().add("main-container");
        initUI();

        // Register for Live SQL/PLSQL execution notifications
        DatabaseManager.getInstance().addSqlListener(this);

        // Load initial records into all views
        refreshAllData();
    }

    private void initUI() {
        // 1. Top Header Banner
        setTop(createHeader());

        // 2. Main Tabbed Workspace
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                new Tab("📊 Overview", createOverviewTab()),
                new Tab("💰 Financial Records (CRUD)", createCrudTab()),
                new Tab("🏦 Bank Accounts", createAccountsTab()),
                new Tab("🎯 Budget Tracker", createBudgetsTab()),
                new Tab("📜 Transactions Ledger", createTransactionsTab()),
                new Tab("📑 DBMS Relational Views", createReportsTab()),
                new Tab("⚡ Live SQL / PL/SQL Console", createSqlConsoleTab())
        );

        setCenter(tabPane);
    }

    // =========================================================================
    // 1. Header Bar
    // =========================================================================
    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-bar");

        Label brandIcon = new Label("🏦");
        brandIcon.setStyle("-fx-font-size: 26px;");

        VBox brandBox = new VBox(2);
        Label title = new Label("FinCore Banking System");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subtitle = new Label("Java 21 | JavaFX 21 | Oracle Database in Docker | PL/SQL");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        brandBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Active DBMS Badge
        boolean isOracle = DatabaseManager.getInstance().isOracle();
        HBox dbBadge = new HBox(6);
        dbBadge.setAlignment(Pos.CENTER);
        Circle dot = new Circle(4, isOracle ? Color.web("#4ade80") : Color.web("#38bdf8"));
        Label dbLbl = new Label(isOracle ? "Oracle DB (FREEPDB1:1521)" : "Oracle Database Engine");
        dbLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (isOracle ? "#4ade80" : "#38bdf8") + ";");
        dbBadge.getChildren().addAll(dot, dbLbl);
        dbBadge.getStyleClass().add("badge-db-oracle");

        // User info
        AuthUser user = authService.getCurrentUser();
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER);
        Label userIcon = new Label("👤");
        Label userName = new Label(user != null ? user.getFullName() : "Admin");
        userName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");
        Label roleBadge = new Label(user != null ? user.getRole() : "ADMIN");
        roleBadge.getStyleClass().add("badge-role");
        userBox.getChildren().addAll(userIcon, userName, roleBadge);

        // Actions
        Button refreshBtn = new Button("⟳ Refresh");
        refreshBtn.getStyleClass().addAll("button", "btn-secondary");
        refreshBtn.setOnAction(e -> refreshAllData());

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().addAll("button", "btn-danger");
        logoutBtn.setOnAction(e -> {
            authService.logout();
            onLogout.run();
        });

        header.getChildren().addAll(brandIcon, brandBox, spacer, dbBadge, userBox, refreshBtn, logoutBtn);
        return header;
    }

    // =========================================================================
    // 2. Overview Tab
    // =========================================================================
    private Node createOverviewTab() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0b0f19; -fx-background-color: transparent;");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        // KPI Cards Row
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(16);
        kpiGrid.setVgap(16);

        totalIncomeLabel = new Label("$0.00");
        totalIncomeLabel.getStyleClass().add("kpi-value");
        VBox cardIncome = createKpiCard("TOTAL INFLOW (INCOME)", totalIncomeLabel, "Cumulative credits & earnings", "kpi-card-income");

        totalExpenseLabel = new Label("$0.00");
        totalExpenseLabel.getStyleClass().add("kpi-value");
        VBox cardExpense = createKpiCard("TOTAL OUTFLOW (EXPENSES)", totalExpenseLabel, "Cumulative debits & expenditures", "kpi-card-expense");

        netBalanceLabel = new Label("$0.00");
        netBalanceLabel.getStyleClass().add("kpi-value");
        VBox cardBalance = createKpiCard("NET CASH FLOW", netBalanceLabel, "Total Inflow minus Total Outflow", "kpi-card-balance");

        totalAccountsLabel = new Label("0 Accounts");
        totalAccountsLabel.getStyleClass().add("kpi-value");
        VBox cardAccounts = createKpiCard("BANK PORTFOLIO", totalAccountsLabel, "Active savings & checking accounts", "kpi-card");

        kpiGrid.add(cardIncome, 0, 0);
        kpiGrid.add(cardExpense, 1, 0);
        kpiGrid.add(cardBalance, 2, 0);
        kpiGrid.add(cardAccounts, 3, 0);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            kpiGrid.getColumnConstraints().add(cc);
        }

        // Charts Row
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);

        // Expense Breakdown PieChart
        expensePieChart = new PieChart();
        expensePieChart.setTitle("Expense Distribution by Category");
        expensePieChart.getStyleClass().add("kpi-card");
        expensePieChart.setPrefHeight(340);
        HBox.setHgrow(expensePieChart, Priority.ALWAYS);

        // Bar Chart (Income vs Expense)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Metric");
        yAxis.setLabel("Amount ($ USD)");

        incomeExpenseBarChart = new BarChart<>(xAxis, yAxis);
        incomeExpenseBarChart.setTitle("Income vs Expense Comparison");
        incomeExpenseBarChart.getStyleClass().add("kpi-card");
        incomeExpenseBarChart.setPrefHeight(340);
        incomeExpenseBarChart.setLegendVisible(false);
        HBox.setHgrow(incomeExpenseBarChart, Priority.ALWAYS);

        chartsBox.getChildren().addAll(expensePieChart, incomeExpenseBarChart);

        root.getChildren().addAll(kpiGrid, chartsBox);
        scroll.setContent(root);
        return scroll;
    }

    private VBox createKpiCard(String title, Label valueLabel, String subtitle, String styleClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("kpi-card", styleClass);
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("kpi-title");
        Label subLbl = new Label(subtitle);
        subLbl.getStyleClass().add("kpi-subtitle");
        card.getChildren().addAll(titleLbl, valueLabel, subLbl);
        return card;
    }

    // =========================================================================
    // 3. Complete CRUD Module: Financial Records
    // =========================================================================
    private Node createCrudTab() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(18));

        // Control / Filter / Action Bar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label filterLbl = new Label("Filter Type:");
        filterLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        filterTypeCombo = new ComboBox<>(FXCollections.observableArrayList("ALL", "INCOME", "EXPENSE"));
        filterTypeCombo.setValue("ALL");
        filterTypeCombo.setOnAction(e -> applyFilters());

        searchField = new TextField();
        searchField.setPromptText("Search category or description...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Record (INSERT)");
        addBtn.getStyleClass().addAll("button", "btn-success");
        addBtn.setOnAction(e -> handleAddRecord());

        Button editBtn = new Button("✏ Edit Record (UPDATE)");
        editBtn.getStyleClass().addAll("button", "btn-primary");
        editBtn.setOnAction(e -> handleEditRecord());

        Button deleteBtn = new Button("🗑 Delete Record (DELETE)");
        deleteBtn.getStyleClass().addAll("button", "btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteRecord());

        Button reloadBtn = new Button("⟳ Reload");
        reloadBtn.getStyleClass().addAll("button", "btn-secondary");
        reloadBtn.setOnAction(e -> refreshCrudRecords());

        toolbar.getChildren().addAll(filterLbl, filterTypeCombo, searchField, spacer, addBtn, editBtn, deleteBtn, reloadBtn);

        // Table View
        crudTable = new TableView<>();
        filteredRecords = new FilteredList<>(masterRecords, p -> true);
        crudTable.setItems(filteredRecords);
        VBox.setVgrow(crudTable, Priority.ALWAYS);

        TableColumn<FinancialRecord, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<FinancialRecord, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getRecordType().name()));
        colType.setPrefWidth(100);
        colType.setCellFactory(column -> new TableCell<FinancialRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("INCOME".equalsIgnoreCase(item) ? "badge-income" : "badge-expense");
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<FinancialRecord, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCat.setPrefWidth(140);

        TableColumn<FinancialRecord, String> colAmt = new TableColumn<>("Amount");
        colAmt.setCellValueFactory(r -> new SimpleStringProperty(String.format("$%,.2f", r.getValue().getAmount())));
        colAmt.setPrefWidth(120);
        colAmt.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        TableColumn<FinancialRecord, String> colAcc = new TableColumn<>("Account");
        colAcc.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colAcc.setPrefWidth(130);

        TableColumn<FinancialRecord, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(260);

        TableColumn<FinancialRecord, String> colDate = new TableColumn<>("Record Date");
        colDate.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getRecordDate() != null ? r.getValue().getRecordDate().toString() : ""));
        colDate.setPrefWidth(110);
        colDate.setStyle("-fx-alignment: CENTER;");

        crudTable.getColumns().addAll(colId, colType, colCat, colAmt, colAcc, colDesc, colDate);

        // Status bar
        crudStatusLabel = new Label("Ready");
        crudStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        root.getChildren().addAll(toolbar, crudTable, crudStatusLabel);
        return root;
    }

    private void applyFilters() {
        String typeFilter = filterTypeCombo.getValue();
        String searchText = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        filteredRecords.setPredicate(record -> {
            if (!"ALL".equalsIgnoreCase(typeFilter)) {
                if (!record.getRecordType().name().equalsIgnoreCase(typeFilter)) {
                    return false;
                }
            }
            if (!searchText.isEmpty()) {
                String cat = record.getCategory() != null ? record.getCategory().toLowerCase() : "";
                String desc = record.getDescription() != null ? record.getDescription().toLowerCase() : "";
                return cat.contains(searchText) || desc.contains(searchText);
            }
            return true;
        });
        crudStatusLabel.setText("Showing " + filteredRecords.size() + " of " + masterRecords.size() + " records");
    }

    private void handleAddRecord() {
        List<String> accs = accountsData.stream().map(Account::getAccountNumber).collect(Collectors.toList());
        RecordDialog dialog = new RecordDialog(null, accs);
        Optional<FinancialRecord> result = dialog.showAndWait();
        result.ifPresent(record -> {
            try {
                FinancialRecord saved = financeService.addRecord(
                        record.getRecordType(),
                        record.getCategory(),
                        record.getAmount(),
                        record.getAccountNumber(),
                        record.getDescription(),
                        record.getRecordDate()
                );
                crudStatusLabel.setText("Successfully inserted record #" + saved.getId() + " into Oracle DB.");
                refreshAllData();
            } catch (Exception ex) {
                showError("Insert Error", ex.getMessage());
            }
        });
    }

    private void handleEditRecord() {
        FinancialRecord selected = crudTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selection Required", "Please select a record from the table to edit.");
            return;
        }

        List<String> accs = accountsData.stream().map(Account::getAccountNumber).collect(Collectors.toList());
        RecordDialog dialog = new RecordDialog(selected, accs);
        Optional<FinancialRecord> result = dialog.showAndWait();
        result.ifPresent(record -> {
            try {
                boolean ok = financeService.updateRecord(record);
                if (ok) {
                    crudStatusLabel.setText("Successfully updated record #" + record.getId() + " in Oracle DB.");
                    refreshAllData();
                } else {
                    showError("Update Error", "Record #" + record.getId() + " could not be updated.");
                }
            } catch (Exception ex) {
                showError("Update Error", ex.getMessage());
            }
        });
    }

    private void handleDeleteRecord() {
        FinancialRecord selected = crudTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selection Required", "Please select a record from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion (SQL DELETE)");
        confirm.setHeaderText("Permanently delete record #" + selected.getId() + "?");
        confirm.setContentText(String.format("Delete %s of $%.2f (%s)?", selected.getRecordType(), selected.getAmount(), selected.getCategory()));

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                boolean ok = financeService.deleteRecord(selected.getId());
                if (ok) {
                    crudStatusLabel.setText("Successfully executed DELETE on record #" + selected.getId());
                    refreshAllData();
                } else {
                    showError("Delete Error", "Record #" + selected.getId() + " could not be deleted.");
                }
            } catch (Exception ex) {
                showError("Delete Error", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // 4. Accounts Tab
    // =========================================================================
    private Node createAccountsTab() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(18));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Bank Accounts Portfolio");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button transferBtn = new Button("💸 Transfer Funds (PL/SQL)");
        transferBtn.getStyleClass().addAll("button", "btn-primary");
        transferBtn.setOnAction(e -> handleTransferFunds());

        Button reloadBtn = new Button("⟳ Reload");
        reloadBtn.getStyleClass().addAll("button", "btn-secondary");
        reloadBtn.setOnAction(e -> refreshAccounts());

        toolbar.getChildren().addAll(title, spacer, transferBtn, reloadBtn);

        accountsTable = new TableView<>(accountsData);
        VBox.setVgrow(accountsTable, Priority.ALWAYS);

        TableColumn<Account, String> colAccNum = new TableColumn<>("Account Number");
        colAccNum.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colAccNum.setPrefWidth(140);

        TableColumn<Account, Long> colCustId = new TableColumn<>("Customer ID");
        colCustId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustId.setPrefWidth(100);
        colCustId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Account, String> colType = new TableColumn<>("Account Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        colType.setPrefWidth(120);
        colType.setStyle("-fx-alignment: CENTER;");

        TableColumn<Account, String> colBal = new TableColumn<>("Balance");
        colBal.setCellValueFactory(a -> new SimpleStringProperty(String.format("$%,.2f", a.getValue().getBalance())));
        colBal.setPrefWidth(140);
        colBal.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        TableColumn<Account, String> colFeatures = new TableColumn<>("Overdraft / Interest");
        colFeatures.setCellValueFactory(a -> {
            Account acc = a.getValue();
            if (acc instanceof CheckingAccount ca) {
                return new SimpleStringProperty(String.format("Overdraft: $%,.2f", ca.getOverdraftLimit()));
            } else if (acc instanceof SavingsAccount sa) {
                return new SimpleStringProperty(String.format("Interest: %.2f%%", sa.getInterestRate()));
            }
            return new SimpleStringProperty("-");
        });
        colFeatures.setPrefWidth(180);

        TableColumn<Account, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(100);
        colStatus.setStyle("-fx-alignment: CENTER;");

        accountsTable.getColumns().addAll(colAccNum, colCustId, colType, colBal, colFeatures, colStatus);

        root.getChildren().addAll(toolbar, accountsTable);
        return root;
    }

    private void handleTransferFunds() {
        if (accountsData.isEmpty()) {
            showWarning("No Accounts", "No active accounts available for transfer.");
            return;
        }

        TransferDialog dialog = new TransferDialog(accountsData);
        Optional<TransferDialog.TransferRequest> req = dialog.showAndWait();
        req.ifPresent(r -> {
            try {
                bankingService.transferFunds(r.fromAccount(), r.toAccount(), r.amount(), r.remarks());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Transfer Successful");
                alert.setHeaderText("Atomic ACID Transfer Completed via PL/SQL");
                alert.setContentText(String.format("Transferred $%.2f from %s to %s successfully!", r.amount(), r.fromAccount(), r.toAccount()));
                alert.showAndWait();
                refreshAllData();
            } catch (Exception ex) {
                showError("Transfer Failed", ex.getMessage());
            }
        });
    }

    // =========================================================================
    // 5. Budgets Tab
    // =========================================================================
    private Node createBudgetsTab() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(18));

        Label title = new Label("Monthly Category Budgets");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        budgetTable = new TableView<>(budgetData);
        VBox.setVgrow(budgetTable, Priority.ALWAYS);

        TableColumn<Budget, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Budget, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCat.setPrefWidth(160);

        TableColumn<Budget, String> colLimit = new TableColumn<>("Monthly Limit");
        colLimit.setCellValueFactory(b -> new SimpleStringProperty(String.format("$%,.2f", b.getValue().getMonthlyLimit())));
        colLimit.setPrefWidth(140);
        colLimit.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        TableColumn<Budget, String> colSpent = new TableColumn<>("Spent So Far");
        colSpent.setCellValueFactory(b -> new SimpleStringProperty(String.format("$%,.2f", b.getValue().getSpentAmount())));
        colSpent.setPrefWidth(140);
        colSpent.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<Budget, String> colRem = new TableColumn<>("Remaining");
        colRem.setCellValueFactory(b -> new SimpleStringProperty(String.format("$%,.2f", b.getValue().getRemainingAmount())));
        colRem.setPrefWidth(140);
        colRem.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        TableColumn<Budget, String> colMonth = new TableColumn<>("Period");
        colMonth.setCellValueFactory(new PropertyValueFactory<>("periodMonth"));
        colMonth.setPrefWidth(100);
        colMonth.setStyle("-fx-alignment: CENTER;");

        budgetTable.getColumns().addAll(colId, colCat, colLimit, colSpent, colRem, colMonth);

        root.getChildren().addAll(title, budgetTable);
        return root;
    }

    // =========================================================================
    // 6. Transactions Ledger Tab
    // =========================================================================
    private Node createTransactionsTab() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(18));

        Label title = new Label("Audit-Logged Transactions Ledger");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        txTable = new TableView<>(txData);
        VBox.setVgrow(txTable, Priority.ALWAYS);

        TableColumn<Transaction, String> colTxId = new TableColumn<>("Transaction ID");
        colTxId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colTxId.setPrefWidth(140);

        TableColumn<Transaction, String> colAcc = new TableColumn<>("Account");
        colAcc.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colAcc.setPrefWidth(120);

        TableColumn<Transaction, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(t -> new SimpleStringProperty(t.getValue().getType().name()));
        colType.setPrefWidth(120);
        colType.setStyle("-fx-alignment: CENTER;");

        TableColumn<Transaction, String> colAmt = new TableColumn<>("Amount");
        colAmt.setCellValueFactory(t -> new SimpleStringProperty(String.format("$%,.2f", t.getValue().getAmount())));
        colAmt.setPrefWidth(120);
        colAmt.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        TableColumn<Transaction, String> colBal = new TableColumn<>("Balance After");
        colBal.setCellValueFactory(t -> new SimpleStringProperty(String.format("$%,.2f", t.getValue().getBalanceAfter())));
        colBal.setPrefWidth(120);
        colBal.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<Transaction, String> colTarget = new TableColumn<>("Target Account");
        colTarget.setCellValueFactory(new PropertyValueFactory<>("targetAccount"));
        colTarget.setPrefWidth(120);

        TableColumn<Transaction, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(220);

        TableColumn<Transaction, String> colTime = new TableColumn<>("Timestamp");
        colTime.setCellValueFactory(t -> new SimpleStringProperty(t.getValue().getCreatedAt() != null ? t.getValue().getCreatedAt().toString() : ""));
        colTime.setPrefWidth(160);

        txTable.getColumns().addAll(colTxId, colAcc, colType, colAmt, colBal, colTarget, colDesc, colTime);

        root.getChildren().addAll(title, txTable);
        return root;
    }

    // =========================================================================
    // 7. Relational Reports (Oracle Views) Tab
    // =========================================================================
    private Node createReportsTab() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(18));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label selectLbl = new Label("Select Relational View:");
        selectLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        reportSelector = new ComboBox<>(FXCollections.observableArrayList(
                "v_customer_portfolio (Customer Summary & Net Worth)",
                "v_budget_summary (Allocated vs Actual Spent)",
                "v_account_ledger (Full Audited Account Transactions)"
        ));
        reportSelector.setValue(reportSelector.getItems().get(0));
        reportSelector.setPrefWidth(380);
        reportSelector.setOnAction(e -> loadReportViewData());

        Button runViewBtn = new Button("Execute View Query");
        runViewBtn.getStyleClass().addAll("button", "btn-primary");
        runViewBtn.setOnAction(e -> loadReportViewData());

        toolbar.getChildren().addAll(selectLbl, reportSelector, runViewBtn);

        reportTableView = new TableView<>();
        VBox.setVgrow(reportTableView, Priority.ALWAYS);

        root.getChildren().addAll(toolbar, reportTableView);
        return root;
    }

    private void loadReportViewData() {
        String selected = reportSelector.getValue();
        List<Map<String, Object>> rows;

        if (selected.contains("v_customer_portfolio")) {
            rows = reportService.getCustomerPortfolioViewReport();
        } else if (selected.contains("v_budget_summary")) {
            rows = reportService.getBudgetSummaryViewReport();
        } else {
            rows = reportService.getAccountLedgerViewReport(50);
        }

        reportTableView.getColumns().clear();
        reportTableView.getItems().clear();

        if (rows.isEmpty()) {
            return;
        }

        // Dynamically build columns based on map keys
        Map<String, Object> firstRow = rows.get(0);
        for (String colName : firstRow.keySet()) {
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(colName.toUpperCase());
            col.setCellValueFactory(cellData -> {
                Object val = cellData.getValue().get(colName);
                return new SimpleStringProperty(val != null ? val.toString() : "-");
            });
            col.setPrefWidth(130);
            reportTableView.getColumns().add(col);
        }

        reportTableView.getItems().addAll(rows);
    }

    // =========================================================================
    // 8. Live SQL & PL/SQL Execution Console Tab
    // =========================================================================
    private Node createSqlConsoleTab() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(18));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Live DBMS Query & PL/SQL Execution Stream");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button clearBtn = new Button("Clear Console");
        clearBtn.getStyleClass().addAll("button", "btn-secondary");
        clearBtn.setOnAction(e -> sqlLogArea.clear());

        toolbar.getChildren().addAll(title, spacer, clearBtn);

        sqlLogArea = new TextArea();
        sqlLogArea.setEditable(false);
        sqlLogArea.setWrapText(true);
        sqlLogArea.getStyleClass().add("sql-console");
        VBox.setVgrow(sqlLogArea, Priority.ALWAYS);

        // Initial greeting
        sqlLogArea.appendText("-- FinCore Live SQL Inspector initialized.\n");
        sqlLogArea.appendText("-- Every DML (INSERT, SELECT, UPDATE, DELETE) and PL/SQL Package Call will appear here live.\n\n");

        root.getChildren().addAll(toolbar, sqlLogArea);
        return root;
    }

    // =========================================================================
    // Data Loading & Sync
    // =========================================================================
    public void refreshAllData() {
        refreshCrudRecords();
        refreshAccounts();
        refreshBudgets();
        refreshTransactions();
        refreshOverviewKpis();
        if (reportSelector != null) {
            loadReportViewData();
        }
    }

    private void refreshCrudRecords() {
        List<FinancialRecord> records = financeService.getAllRecords();
        masterRecords.setAll(records);
        applyFilters();
    }

    private void refreshAccounts() {
        List<Account> accounts = financeService.getAllAccounts();
        accountsData.setAll(accounts);
    }

    private void refreshBudgets() {
        List<Budget> budgets = financeService.getAllBudgetsWithSpending();
        budgetData.setAll(budgets);
    }

    private void refreshTransactions() {
        List<Transaction> txs = financeService.getRecentTransactions(100);
        txData.setAll(txs);
    }

    private void refreshOverviewKpis() {
        double totalIncome = financeService.getTotalIncome();
        double totalExpense = financeService.getTotalExpenses();
        double net = totalIncome - totalExpense;

        totalIncomeLabel.setText(String.format("$%,.2f", totalIncome));
        totalExpenseLabel.setText(String.format("$%,.2f", totalExpense));
        netBalanceLabel.setText(String.format("$%,.2f", net));
        totalAccountsLabel.setText(accountsData.size() + " Active Accounts");

        // Update Pie Chart
        Map<String, Double> expensesByCat = financeService.getCategoryBreakdown(FinancialRecord.Type.EXPENSE);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        expensesByCat.forEach((cat, amt) -> {
            if (amt > 0) {
                pieData.add(new PieChart.Data(cat + " ($" + String.format("%.0f", amt) + ")", amt));
            }
        });
        expensePieChart.setData(pieData);

        // Update Bar Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Total Income", totalIncome));
        series.getData().add(new XYChart.Data<>("Total Expense", totalExpense));
        series.getData().add(new XYChart.Data<>("Net Flow", net));
        incomeExpenseBarChart.setData(FXCollections.observableArrayList(series));
    }

    // =========================================================================
    // DatabaseManager.SqlListener Implementation
    // =========================================================================
    @Override
    public void onSqlExecuted(String operation, String sql, long executionTimeMs, int affectedRows) {
        String timestamp = LocalDateTime.now().format(TIME_FMT);
        String logLine = String.format("[%s] [%-12s] (%3d ms, %d rows)\n   %s\n\n",
                timestamp, operation, executionTimeMs, affectedRows, sql.replaceAll("\\s+", " ").trim());

        Platform.runLater(() -> {
            if (sqlLogArea != null) {
                sqlLogArea.appendText(logLine);
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
