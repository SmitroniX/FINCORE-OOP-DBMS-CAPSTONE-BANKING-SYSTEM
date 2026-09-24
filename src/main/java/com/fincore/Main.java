package com.fincore;

import com.fincore.db.DatabaseManager;
import com.fincore.repository.AccountRepository;
import com.fincore.repository.AuditLogRepository;
import com.fincore.repository.BudgetRepository;
import com.fincore.repository.CustomerRepository;
import com.fincore.repository.FinancialRecordRepository;
import com.fincore.repository.TransactionRepository;
import com.fincore.repository.UserRepository;
import com.fincore.repository.impl.JdbcAccountRepository;
import com.fincore.repository.impl.JdbcAuditLogRepository;
import com.fincore.repository.impl.JdbcBudgetRepository;
import com.fincore.repository.impl.JdbcCustomerRepository;
import com.fincore.repository.impl.JdbcFinancialRecordRepository;
import com.fincore.repository.impl.JdbcTransactionRepository;
import com.fincore.repository.impl.JdbcUserRepository;
import com.fincore.service.AuthService;
import com.fincore.service.BankingService;
import com.fincore.service.CustomerService;
import com.fincore.service.FinanceService;
import com.fincore.service.ReportService;
import com.fincore.ui.ConsoleMenu;
import com.fincore.ui.DemoRunner;

import java.awt.GraphicsEnvironment;

import static com.fincore.ui.ConsoleColors.*;

/**
 * Application Entrypoint:
 * - Boots DBMS connection (Oracle 10g XE / SQLite / MySQL via JDBC)
 * - Runs schema migrations and seeds
 * - Wires repositories, services, and domain controllers (Dependency Injection)
 * - Launches Java Swing GUI (LoginFrame -> FinanceDashboardFrame) or CLI/Demo Runner.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(CYAN + BOLD + "============================================================" + RESET);
        System.out.println(CYAN + BOLD + "  FinCore: Object-Oriented DBMS Capstone Banking System" + RESET);
        System.out.println(CYAN + BOLD + "============================================================" + RESET);

        // Register clean shutdown hook for JDBC driver cleanup threads
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Throwable ignored) {
            }
        }));

        // 1. Initialize Database and run schema migrations
        DatabaseManager dbManager = DatabaseManager.getInstance();
        System.out.println("[Bootstrap] Initializing database engine (" + dbManager.getConfig().getDbType() + ")...");
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
        BankingService bankingService = new BankingService(dbManager, accountRepo, txRepo, auditRepo);
        CustomerService customerService = new CustomerService(customerRepo, accountRepo, auditRepo, bankingService);
        ReportService reportService = new ReportService(dbManager, customerRepo, accountRepo, txRepo, auditRepo);
        AuthService authService = new AuthService(userRepo, auditRepo);
        FinanceService financeService = new FinanceService(finRecordRepo, budgetRepo, accountRepo, txRepo, auditRepo);

        // 4. Setup UI and Demo components
        DemoRunner demoRunner = new DemoRunner(authService, financeService, customerService, bankingService, reportService, accountRepo);
        ConsoleMenu consoleMenu = new ConsoleMenu(customerService, bankingService, reportService, accountRepo, demoRunner);

        // 5. Evaluate execution flags
        boolean runDemoOnly = false;
        boolean runCliOnly = false;
        boolean forceGui = false;

        for (String arg : args) {
            if ("--demo".equalsIgnoreCase(arg) || "-d".equalsIgnoreCase(arg) || "--test".equalsIgnoreCase(arg) || "demo".equalsIgnoreCase(arg) || "test".equalsIgnoreCase(arg) || "-t".equalsIgnoreCase(arg)) {
                runDemoOnly = true;
            } else if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg) || "--console".equalsIgnoreCase(arg) || "cli".equalsIgnoreCase(arg) || "console".equalsIgnoreCase(arg)) {
                runCliOnly = true;
            } else if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg) || "gui".equalsIgnoreCase(arg)) {
                forceGui = true;
            }
        }

        if (runDemoOnly) {
            demoRunner.runFullDemonstration();
        } else if (runCliOnly) {
            consoleMenu.start();
        } else {
            // Check display environment
            boolean headless = GraphicsEnvironment.isHeadless();
            if (!headless || forceGui) {
                System.out.println(GREEN + "[GUI] Launching FinCore JavaFX 21 Enterprise Finance Dashboard..." + RESET);
                try {
                    javafx.application.Application.launch(MainApp.class, args);
                } catch (Throwable ex) {
                    System.err.println("[GUI Launch Notice] JavaFX GUI could not be initialized: " + ex.getMessage());
                    System.out.println(YELLOW + "Starting Interactive Console Menu...\n" + RESET);
                    consoleMenu.start();
                }
            } else {
                System.out.println(YELLOW + "[Notice] Headless environment detected (no active display)." + RESET);
                System.out.println(YELLOW + "         • On Windows: Simply run 'run.bat' in CMD to launch the JavaFX 21 UI!" + RESET);
                System.out.println(YELLOW + "         • To run the automated Capstone Demonstration: mvn test or run-demo.bat" + RESET);
                System.out.println(YELLOW + "         • Starting Interactive Console Menu...\n" + RESET);
                consoleMenu.start();
            }
        }
    }
}
