package com.fincore.ui;

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
                    case "4" -> performDeposit();
                    case "5" -> performWithdrawal();
                    case "6" -> performAcidTransfer();
                    case "7" -> viewAccountStatement();
                    case "8" -> viewBankAnalytics();
                    case "9" -> viewAuditLogs();
                    case "10" -> demoRunner.runFullDemonstration();
                    case "0" -> {
                        System.out.println(GREEN + "\nThank you for using FinCore Banking System. Goodbye!" + RESET);
                        running = false;
                    }
                    default -> System.out.println(RED + "Invalid selection. Please choose an option between 0 and 10." + RESET);
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
        System.out.println("║  " + YELLOW + "[1]" + RESET + "  View Customer Portfolios (DBMS JOIN & GROUP BY)       ║");
        System.out.println("║  " + YELLOW + "[2]" + RESET + "  Register New Customer                                 ║");
        System.out.println("║  " + YELLOW + "[3]" + RESET + "  Open New Bank Account (Savings / Checking)            ║");
        System.out.println("║  " + YELLOW + "[4]" + RESET + "  Deposit Funds                                         ║");
        System.out.println("║  " + YELLOW + "[5]" + RESET + "  Withdraw Funds (Polymorphic Rules & Overdraft)        ║");
        System.out.println("║  " + YELLOW + "[6]" + RESET + "  ACID Funds Transfer (Atomicity & Rollback Guarded)    ║");
        System.out.println("║  " + YELLOW + "[7]" + RESET + "  View Account Statement (Ledger History)              ║");
        System.out.println("║  " + YELLOW + "[8]" + RESET + "  Bank Liquidity Metrics (SQL Aggregations)             ║");
        System.out.println("║  " + YELLOW + "[9]" + RESET + "  Security & Compliance Audit Logs                      ║");
        System.out.println("║  " + YELLOW + "[10]" + RESET + " Run End-to-End Automated Demonstration                ║");
        System.out.println("║  " + RED + "[0]" + RESET + "  Exit Application                                      ║");
        System.out.println(CYAN + BOLD + "╚════════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.print(WHITE + BOLD + "Enter your choice [0-10]: " + RESET);
    }

    private void listAllCustomers() {
        System.out.println(CYAN + "\n--- Customer Portfolios (Relational JOIN Query) ---" + RESET);
        List<CustomerSummaryDTO> list = reportService.getCustomerPortfolioReport();
        System.out.println(String.format("%-10s | %-20s | %-25s | %-10s | %-14s | %-5s",
                "Code", "Name", "Email", "Accounts", "Total Balance", "Txs"));
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (CustomerSummaryDTO dto : list) {
            System.out.println(dto);
        }
    }

    private void registerNewCustomer() {
        System.out.println(CYAN + "\n--- Register New Customer ---" + RESET);
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();

        Customer c = customerService.registerCustomer(name, email, phone);
        System.out.println(GREEN + "Successfully registered customer ID " + c.getId() + " [" + c.getUserCode() + "]" + RESET);
    }

    private void openNewAccount() {
        System.out.println(CYAN + "\n--- Open New Bank Account ---" + RESET);
        System.out.print("Enter Customer ID: ");
        Long customerId = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Account Type ([1] Savings, [2] Checking): ");
        String typeChoice = scanner.nextLine().trim();
        System.out.print("Initial Deposit Amount ($): ");
        double deposit = Double.parseDouble(scanner.nextLine().trim());

        if ("1".equals(typeChoice)) {
            System.out.print("Annual Interest Rate % (e.g. 4.25): ");
            double rate = Double.parseDouble(scanner.nextLine().trim());
            Account acc = customerService.openSavingsAccount(customerId, deposit, rate);
            System.out.println(GREEN + "Created Savings Account: " + acc.getAccountSummary() + RESET);
        } else {
            System.out.print("Overdraft Credit Limit $ (e.g. 1000.00): ");
            double limit = Double.parseDouble(scanner.nextLine().trim());
            Account acc = customerService.openCheckingAccount(customerId, deposit, limit);
            System.out.println(GREEN + "Created Checking Account: " + acc.getAccountSummary() + RESET);
        }
    }

    private void performDeposit() {
        System.out.println(CYAN + "\n--- Deposit Funds ---" + RESET);
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();
        System.out.print("Enter Deposit Amount ($): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Description/Memo: ");
        String memo = scanner.nextLine().trim();

        Transaction tx = bankingService.deposit(accNum, amount, memo);
        System.out.println(GREEN + "Deposit Successful! " + tx + RESET);
    }

    private void performWithdrawal() {
        System.out.println(CYAN + "\n--- Withdraw Funds ---" + RESET);
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();
        System.out.print("Enter Withdrawal Amount ($): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Description/Memo: ");
        String memo = scanner.nextLine().trim();

        Transaction tx = bankingService.withdraw(accNum, amount, memo);
        System.out.println(GREEN + "Withdrawal Successful! " + tx + RESET);
    }

    private void performAcidTransfer() {
        System.out.println(CYAN + "\n--- ACID Funds Transfer ---" + RESET);
        System.out.print("Enter Source Account Number: ");
        String from = scanner.nextLine().trim();
        System.out.print("Enter Destination Account Number: ");
        String to = scanner.nextLine().trim();
        System.out.print("Enter Transfer Amount ($): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Transfer Memo: ");
        String memo = scanner.nextLine().trim();

        bankingService.transferFunds(from, to, amount, memo);
        System.out.println(GREEN + "ACID Transfer Executed & Committed Successfully!" + RESET);
    }

    private void viewAccountStatement() {
        System.out.println(CYAN + "\n--- View Account Statement ---" + RESET);
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();

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
            System.out.println("No recorded transactions.");
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
}
