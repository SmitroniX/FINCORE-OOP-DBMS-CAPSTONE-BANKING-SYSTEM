package com.fincore;

import com.fincore.db.DatabaseManager;
import com.fincore.repository.AccountRepository;
import com.fincore.repository.AuditLogRepository;
import com.fincore.repository.CustomerRepository;
import com.fincore.repository.TransactionRepository;
import com.fincore.repository.impl.JdbcAccountRepository;
import com.fincore.repository.impl.JdbcAuditLogRepository;
import com.fincore.repository.impl.JdbcCustomerRepository;
import com.fincore.repository.impl.JdbcTransactionRepository;
import com.fincore.service.BankingService;
import com.fincore.service.CustomerService;
import com.fincore.service.ReportService;
import com.fincore.ui.ConsoleMenu;
import com.fincore.ui.DemoRunner;

import static com.fincore.ui.ConsoleColors.*;

/**
 * Application Entrypoint: Boots DBMS connection, runs schema migrations,
 * wires dependencies (Inversion of Control), and launches UI or Demo mode.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(CYAN + BOLD + "============================================================" + RESET);
        System.out.println(CYAN + BOLD + "  FinCore: Object-Oriented DBMS Capstone Banking System" + RESET);
        System.out.println(CYAN + BOLD + "============================================================" + RESET);

        // 1. Initialize Database and run schema migrations
        DatabaseManager dbManager = DatabaseManager.getInstance();
        System.out.println("[Bootstrap] Initializing database engine (" + dbManager.getConfig().getDbType() + ")...");
        dbManager.initializeDatabase();

        // 2. Wire Data Access Repositories
        CustomerRepository customerRepo = new JdbcCustomerRepository(dbManager);
        AccountRepository accountRepo = new JdbcAccountRepository(dbManager);
        TransactionRepository txRepo = new JdbcTransactionRepository(dbManager);
        AuditLogRepository auditRepo = new JdbcAuditLogRepository(dbManager);

        // 3. Wire Business Services
        BankingService bankingService = new BankingService(dbManager, accountRepo, txRepo, auditRepo);
        CustomerService customerService = new CustomerService(customerRepo, accountRepo, auditRepo, bankingService);
        ReportService reportService = new ReportService(dbManager, customerRepo, accountRepo, txRepo, auditRepo);

        // 4. Setup UI and Demo components
        DemoRunner demoRunner = new DemoRunner(customerService, bankingService, reportService, accountRepo);
        ConsoleMenu consoleMenu = new ConsoleMenu(customerService, bankingService, reportService, accountRepo, demoRunner);

        // 5. Evaluate execution flags
        boolean runDemoOnly = false;
        for (String arg : args) {
            if ("--demo".equalsIgnoreCase(arg) || "-d".equalsIgnoreCase(arg) || "--test".equalsIgnoreCase(arg)) {
                runDemoOnly = true;
                break;
            }
        }

        if (runDemoOnly) {
            demoRunner.runFullDemonstration();
        } else {
            consoleMenu.start();
        }
    }
}
