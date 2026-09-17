package com.fincore.ui;

import com.fincore.db.DatabaseManager;
import com.fincore.model.Account;
import com.fincore.model.AuditLog;
import com.fincore.model.Customer;
import com.fincore.model.CustomerSummaryDTO;
import com.fincore.model.Transaction;
import com.fincore.repository.AccountRepository;
import com.fincore.service.BankingService;
import com.fincore.service.CustomerService;
import com.fincore.service.ReportService;
import com.fincore.service.exception.BankingException;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static com.fincore.ui.ConsoleColors.*;

/**
 * Interactive Console UI allowing full hands-on exploration of the OOP & DBMS banking application.
 */
public class ConsoleMenu {

    private final CustomerService customerService;
    private final BankingService bankingService;
    private final ReportService reportService;
    private final AccountRepository accountRepo;
    private final DemoRunner demoRunner;
    private final Scanner scanner;

    public ConsoleMenu(CustomerService customerService,
                       BankingService bankingService,
                       ReportService reportService,
                       AccountRepository accountRepo,
                       DemoRunner demoRunner) {
        this.customerService = customerService;
        this.bankingService = bankingService;
        this.reportService = reportService;
        this.accountRepo = accountRepo;
        this.demoRunner = demoRunner;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> listAllCustomers();
                    case "2" -> registerNewCustomer();
                    case "3" -> openNewAccount();
                    case "4" -> viewCustomerAccounts();
                    case "5" -> performDeposit();
                    case "6" -> performWithdrawal();
                    case "7" -> performAcidTransfer();
                    case "8" -> viewAccountStatement();
                    case "9" -> viewBankAnalytics();
                    case "10" -> viewAuditLogs();
                    case "11" -> demoRunner.runFullDemonstration();
                    case "12" -> resetDatabase();
                    case "0" -> {
                        System.out.println(GREEN + "\nThank you for using FinCore Banking System. Goodbye!" + RESET);
                        running = false;
                    }
                    default -> System.out.println(RED + "Invalid selection. Please choose an option from the menu." + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Operation Error: " + e.getMessage() + RESET);
            }
        }
    }

    private void printMainMenu() {
        System.out.println(CYAN + BOLD + "\n╔════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + BOLD + "║           FINCORE: OOP & DBMS CAPSTONE BANKING SYSTEM             ║" + RESET);
        System.out.println(CYAN + BOLD + "╠════════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.println("║  " + YELLOW + "[1]"  + RESET + "  View Customer Portfolios (DBMS JOIN & GROUP BY)       ║");
        System.out.println("║  " + YELLOW + "[2]"  + RESET + "  Register New Customer                                 ║");
        System.out.println("║  " + YELLOW + "[3]"  + RESET + "  Open New Bank Account (Savings / Checking)            ║");
        System.out.println("║  " + YELLOW + "[4]"  + RESET + "  View All Accounts of a Specific Customer              ║");
        System.out.println("║  " + YELLOW + "[5]"  + RESET + "  Deposit Funds                                         ║");
        System.out.println("║  " + YELLOW + "[6]"  + RESET + "  Withdraw Funds (Polymorphic Rules & Overdraft)        ║");
        System.out.println("║  " + YELLOW + "[7]"  + RESET + "  ACID Funds Transfer (Atomicity & Rollback Guarded)    ║");
        System.out.println("║  " + YELLOW + "[8]"  + RESET + "  View Account Statement (Ledger History)              ║");
        System.out.println("║  " + YELLOW + "[9]"  + RESET + "  Bank Liquidity Metrics (SQL Aggregations)             ║");
        System.out.println("║  " + YELLOW + "[10]" + RESET + " View Security & Compliance Audit Logs                 ║");
        System.out.println("║  " + YELLOW + "[11]" + RESET + " Run End-to-End Automated Demonstration                ║");
        System.out.println("║  " + PURPLE + "[12]" + RESET + " Reset Database to Fresh Seed Data                     ║");
        System.out.println("║  " + RED    + "[0]"  + RESET + "  Exit Application                                      ║");
        System.out.println(CYAN + BOLD + "╚════════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.print(WHITE + BOLD + "Enter your choice [0-12]: " + RESET);
    }

    private void listAllCustomers() {
        System.out.println(CYAN + "\n--- Customer Portfolios (Relational JOIN Query) ---" + RESET);
        List<CustomerSummaryDTO> list = reportService.getCustomerPortfolioReport();
        System.out.println(String.format("%-4s | %-10s | %-20s | %-25s | %-10s | %-14s | %-5s",
                "ID", "Code", "Name", "Email", "Accounts", "Total Balance", "Txs"));
        System.out.println("---------------------------------------------------------------------------------------------------------");
        for (CustomerSummaryDTO dto : list) {
            System.out.println(dto);
        }
    }

    private void registerNewCustomer() {
        System.out.println(CYAN + "\n--- Register New Customer ---" + RESET);
        String name = promptNonEmpty("Enter Full Name: ");
        String email = promptNonEmpty("Enter Email Address: ");
        String phone = promptNonEmpty("Enter Phone Number: ");

        Customer c = customerService.registerCustomer(name, email, phone);
        System.out.println(GREEN + "Successfully registered customer ID " + c.getId() + " [" + c.getUserCode() + "]" + RESET);
    }

    private void openNewAccount() {
        System.out.println(CYAN + "\n--- Open New Bank Account ---" + RESET);
        String custInput = promptNonEmpty("Enter Customer ID or Code (e.g. 1 or CUST-1001): ");
        Customer customer = customerService.findCustomerByIdOrCode(custInput);
        System.out.println(GREEN + "Found Customer: " + customer.getName() + " (" + customer.getUserCode() + ")" + RESET);

        System.out.print("Account Type ([1] Savings, [2] Checking): ");
        String typeChoice = scanner.nextLine().trim();
        double deposit = promptDouble("Initial Deposit Amount ($): ", 0.0);

        if ("1".equals(typeChoice)) {
            double rate = promptDouble("Annual Interest Rate % (e.g. 4.25): ", 0.0);
            Account acc = customerService.openSavingsAccount(customer.getId(), deposit, rate);
            System.out.println(GREEN + "Created Savings Account: " + acc.getAccountSummary() + RESET);
        } else {
            double limit = promptDouble("Overdraft Credit Limit $ (e.g. 1000.00): ", 0.0);
            Account acc = customerService.openCheckingAccount(customer.getId(), deposit, limit);
            System.out.println(GREEN + "Created Checking Account: " + acc.getAccountSummary() + RESET);
        }
    }

    private void viewCustomerAccounts() {
        System.out.println(CYAN + "\n--- View Customer Accounts ---" + RESET);
        String custInput = promptNonEmpty("Enter Customer ID or Code: ");
        Customer customer = customerService.findCustomerByIdOrCode(custInput);

        List<Account> accounts = customerService.getAccountsByCustomerId(customer.getId());
        System.out.println(YELLOW + "Accounts for " + customer.getName() + " [" + customer.getUserCode() + "]:" + RESET);
        if (accounts.isEmpty()) {
            System.out.println("  No accounts found for this customer.");
        } else {
            for (Account acc : accounts) {
                System.out.println("  " + acc.getAccountSummary());
            }
        }
    }

    private void performDeposit() {
        System.out.println(CYAN + "\n--- Deposit Funds ---" + RESET);
        String accNum = promptNonEmpty("Enter Account Number: ");
        double amount = promptDouble("Enter Deposit Amount ($): ", 0.01);
        System.out.print("Enter Description/Memo: ");
        String memo = scanner.nextLine().trim();

        Transaction tx = bankingService.deposit(accNum, amount, memo.isEmpty() ? "Cash Deposit" : memo);
        System.out.println(GREEN + "Deposit Successful! " + tx + RESET);
    }

    private void performWithdrawal() {
        System.out.println(CYAN + "\n--- Withdraw Funds ---" + RESET);
        String accNum = promptNonEmpty("Enter Account Number: ");
        double amount = promptDouble("Enter Withdrawal Amount ($): ", 0.01);
        System.out.print("Enter Description/Memo: ");
        String memo = scanner.nextLine().trim();

        Transaction tx = bankingService.withdraw(accNum, amount, memo.isEmpty() ? "Cash Withdrawal" : memo);
        System.out.println(GREEN + "Withdrawal Successful! " + tx + RESET);
    }

    private void performAcidTransfer() {
        System.out.println(CYAN + "\n--- ACID Funds Transfer ---" + RESET);
        String from = promptNonEmpty("Enter Source Account Number: ");
        String to = promptNonEmpty("Enter Destination Account Number: ");
        double amount = promptDouble("Enter Transfer Amount ($): ", 0.01);
        System.out.print("Enter Transfer Memo: ");
        String memo = scanner.nextLine().trim();

        bankingService.transferFunds(from, to, amount, memo.isEmpty() ? "Funds Transfer" : memo);
        System.out.println(GREEN + "ACID Transfer Executed & Committed Successfully!" + RESET);
    }

    private void viewAccountStatement() {
        System.out.println(CYAN + "\n--- View Account Statement ---" + RESET);
        String accNum = promptNonEmpty("Enter Account Number: ");

        Optional<Account> accOpt = accountRepo.findByAccountNumber(accNum);
        if (accOpt.isEmpty()) {
            System.out.println(RED + "Account not found: " + accNum + RESET);
            return;
        }

        Account acc = accOpt.get();
        System.out.println(YELLOW + "\nAccount Summary: " + acc.getAccountSummary() + RESET);
        System.out.println("Ledger Transaction History:");
        List<Transaction> txs = reportService.getAccountStatement(accNum);
        if (txs.isEmpty()) {
            System.out.println("  No recorded transactions.");
        } else {
            for (Transaction t : txs) {
                System.out.println("  " + t);
            }
        }
    }

    private void viewBankAnalytics() {
        System.out.println(CYAN + "\n--- Global Bank Liquidity & Analytics (SQL Aggregations) ---" + RESET);
        Map<String, Object> m = reportService.getBankLiquidityMetrics();
        System.out.printf(" Total Registered Accounts : %d\n", m.get("total_accounts"));
        System.out.printf(" Total System Deposits     : $%,.2f\n", m.get("total_deposits"));
        System.out.printf(" Average Account Balance   : $%,.2f\n", m.get("avg_balance"));
        System.out.printf(" Total in Savings Accounts : $%,.2f\n", m.get("savings_deposits"));
        System.out.printf(" Total in Checking Accounts: $%,.2f\n", m.get("checking_deposits"));
    }

    private void viewAuditLogs() {
        System.out.println(CYAN + "\n--- System Security Audit Trail ---" + RESET);
        List<AuditLog> logs = reportService.getRecentAuditLogs(15);
        for (AuditLog log : logs) {
            System.out.println("  " + log);
        }
    }

    private void resetDatabase() {
        System.out.println(YELLOW + "\nAre you sure you want to reset all tables and reload clean seed data? (yes/no): " + RESET);
        String confirm = scanner.nextLine().trim();
        if ("yes".equalsIgnoreCase(confirm) || "y".equalsIgnoreCase(confirm)) {
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS audit_logs;");
                stmt.execute("DROP TABLE IF EXISTS transactions;");
                stmt.execute("DROP TABLE IF EXISTS accounts;");
                stmt.execute("DROP TABLE IF EXISTS customers;");
                System.out.println("[Reset] Dropped existing tables.");
                DatabaseManager.getInstance().initializeDatabase();
                System.out.println(GREEN + "Database cleanly reset to default seed records!" + RESET);
            } catch (Exception ex) {
                System.out.println(RED + "Reset failed: " + ex.getMessage() + RESET);
            }
        } else {
            System.out.println("Reset cancelled.");
        }
    }

    // Helper input methods with validation
    private String promptNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(RED + "Input cannot be empty. Please try again." + RESET);
        }
    }

    private double promptDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace("$", "").replace(",", "");
            try {
                double val = Double.parseDouble(input);
                if (val >= min) {
                    return val;
                }
                System.out.println(RED + "Value must be at least " + min + ". Please try again." + RESET);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid number format. Please enter a valid numerical value." + RESET);
            }
        }
    }
}
