package com.fincore;

import com.fincore.db.DatabaseManager;
import com.fincore.model.AuthUser;
import com.fincore.repository.*;
import com.fincore.repository.impl.*;
import com.fincore.service.*;
import com.fincore.ui.javafx.FinanceDashboardView;
import com.fincore.ui.javafx.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FinCore JavaFX 21 Application Entrypoint.
 * Bootstraps Oracle Database connection (or SQLite fallback),
 * runs schema migrations, wires repositories and services, and launches the desktop UI.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private AuthService authService;
    private FinanceService financeService;
    private BankingService bankingService;
    private CustomerService customerService;
    private ReportService reportService;

    @Override
    public void init() {
        // 1. Initialize DBMS and execute migrations
        DatabaseManager dbManager = DatabaseManager.getInstance();
        System.out.println("[Bootstrap] Starting FinCore Banking System...");
        System.out.println("[Bootstrap] Active DBMS: " + dbManager.getActiveEngineDescription());
        dbManager.initializeDatabase();

        // 2. Wire Data Access Repositories
        CustomerRepository customerRepo = new JdbcCustomerRepository(dbManager);
        AccountRepository accountRepo = new JdbcAccountRepository(dbManager);
        TransactionRepository txRepo = new JdbcTransactionRepository(dbManager);
        AuditLogRepository auditRepo = new JdbcAuditLogRepository(dbManager);
        UserRepository userRepo = new JdbcUserRepository(dbManager);
        FinancialRecordRepository finRecordRepo = new JdbcFinancialRecordRepository(dbManager);
        BudgetRepository budgetRepo = new JdbcBudgetRepository(dbManager);

        // 3. Wire Business Services
        this.bankingService = new BankingService(dbManager, accountRepo, txRepo, auditRepo);
        this.customerService = new CustomerService(customerRepo, accountRepo, auditRepo, bankingService);
        this.reportService = new ReportService(dbManager, customerRepo, accountRepo, txRepo, auditRepo);
        this.authService = new AuthService(userRepo, auditRepo);
        this.financeService = new FinanceService(finRecordRepo, budgetRepo, accountRepo, txRepo, auditRepo);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("FinCore - Enterprise Banking System [Java 21 | JavaFX 21 | Oracle in Docker | PL/SQL]");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(680);

        showLoginView();
        primaryStage.show();
    }

    public void showLoginView() {
        LoginView loginView = new LoginView(authService, this::onLoginSuccess);
        Scene scene = new Scene(loginView, 1180, 780);
        scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private void onLoginSuccess(AuthUser user) {
        FinanceDashboardView dashboardView = new FinanceDashboardView(
                authService,
                financeService,
                bankingService,
                customerService,
                reportService,
                this::showLoginView
        );

        Scene scene = new Scene(dashboardView, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
