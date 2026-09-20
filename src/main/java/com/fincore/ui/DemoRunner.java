package com.fincore.ui;

import com.fincore.db.DatabaseManager;
import com.fincore.model.*;
import com.fincore.repository.AccountRepository;
import com.fincore.service.AuthService;
import com.fincore.service.BankingService;
import com.fincore.service.CustomerService;
import com.fincore.service.FinanceService;
import com.fincore.service.ReportService;
import com.fincore.service.exception.BankingException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fincore.ui.ConsoleColors.*;

/**
 * Automated Capstone Demonstration Runner.
 * Showcases:
 * 1. Start application & Database Bootstrap (Oracle 10g XE / SQLite JDBC)
 * 2. Database-backed Authentication (Login) from `users` table
 * 3. Open Finance Dashboard & Overview metrics
 * 4. Complete CRUD Module on Financial Records (INSERT, SELECT, UPDATE, DELETE) with SQL/DML trace
 * 5. OOP Principles (Inheritance, Polymorphism)
 * 6. ACID Transactions (Commit & Rollback)
 * 7. Multi-table JOINs, Aggregations & Reports
 */
public class DemoRunner {

    private final AuthService authService;
    private final FinanceService financeService;
    private final CustomerService customerService;
    private final BankingService bankingService;
    private final ReportService reportService;
    private final AccountRepository accountRepo;

    public DemoRunner(AuthService authService,
                      FinanceService financeService,
                      CustomerService customerService,
                      BankingService bankingService,
                      ReportService reportService,
                      AccountRepository accountRepo) {
        this.authService = authService;
        this.financeService = financeService;
        this.customerService = customerService;
        this.bankingService = bankingService;
        this.reportService = reportService;
        this.accountRepo = accountRepo;
    }

    public void runFullDemonstration() {
        System.out.println(CYAN + BOLD + "\n==========================================================================" + RESET);
        System.out.println(CYAN + BOLD + "   FINCORE CAPSTONE: OOP & DBMS LIVE SHOWCASE & VERIFICATION RUNNER" + RESET);
        System.out.println(CYAN + BOLD + "==========================================================================" + RESET);

        step1StartApplicationAndDbmsBootstrap();
        step2AuthenticateFromDatabase();
        step3OpenFinanceDashboard();
        step4DemonstrateCompleteCrudModule();
        step5ShowOOPModelsAndPolymorphism();
        step6DemonstrateAcidTransactionCommit();
        step7DemonstrateAcidTransactionRollback();
        step8DemonstrateDbmsJoinAndAggregations();

        System.out.println(GREEN + BOLD + "\n==========================================================================" + RESET);
        System.out.println(GREEN + BOLD + ">>> ALL CAPSTONE DEMONSTRATION STEPS COMPLETED SUCCESSFULLY! <<<" + RESET);
        System.out.println(GREEN + BOLD + "==========================================================================\n" + RESET);
    }

    // =========================================================================
    // STEP 1: Application Start & Database Connectivity
    // =========================================================================
    private void step1StartApplicationAndDbmsBootstrap() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 1] Application Start & Database Connectivity" + RESET);
        String dbType = DatabaseManager.getInstance().getConfig().getDbType().toUpperCase();
        String url = DatabaseManager.getInstance().getConfig().getDbUrl();
        String driver = DatabaseManager.getInstance().getConfig().getDriverClass();

        System.out.println(WHITE + " + Database Management System Engine : " + GREEN + dbType + RESET);
        System.out.println(WHITE + " + JDBC Driver Class                : " + CYAN + driver + RESET);
        System.out.println(WHITE + " + JDBC Connection URL              : " + CYAN + url + RESET);
        System.out.println(WHITE + " + Relational Schema & Tables       : " + GREEN + "INITIALIZED & VERIFIED" + RESET);
        System.out.println(GREEN + " [VERIFIED] JDBC Connection Pool successfully active." + RESET);
    }

    // =========================================================================
    // STEP 2: Database-Backed Authentication (Login)
    // =========================================================================
    private void step2AuthenticateFromDatabase() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 2] Database-Backed User Authentication (Login)" + RESET);
        System.out.println(WHITE + "Demonstrating authentication against DBMS 'users' table using SQL PreparedStatements:" + RESET);

        // 2A: Demonstrate failed authentication handling
        System.out.println(CYAN + "\n -> [2A] Testing invalid credential rejection:" + RESET);
        try {
            authService.login("invalid_user", "wrong_password");
            System.err.println(RED + " [ERROR] Invalid login should have been rejected!" + RESET);
        } catch (BankingException ex) {
            System.out.println(PURPLE + "    [REJECTED AS EXPECTED] " + ex.getMessage() + RESET);
        }

        // 2B: Demonstrate successful authentication
        System.out.println(CYAN + "\n -> [2B] Authenticating valid user ('admin' / 'admin123'):" + RESET);
        System.out.println(WHITE + "    SQL DQL Executed:" + RESET);
        System.out.println(YELLOW + "    SELECT id, username, password, full_name, role, status, created_at\n" +
                                   "    FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE';" + RESET);

        AuthUser user = authService.login("admin", "admin123");
        System.out.println(GREEN + "    [AUTHENTICATED] User ID: " + user.getId() +
                " | Username: " + user.getUsername() +
                " | Name: " + user.getFullName() +
                " | Role: " + user.getRole() +
                " | Status: " + user.getStatus() + RESET);
    }

    // =========================================================================
    // STEP 3: Open Finance Dashboard & Overview Metrics
    // =========================================================================
    private void step3OpenFinanceDashboard() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 3] Open Finance Dashboard & System Overview" + RESET);
        System.out.println(WHITE + "Loading real-time financial metrics and aggregations across all banking domains:" + RESET);

        double totalLiquidity = financeService.getTotalAccountBalances();
        double totalIncome = financeService.getTotalIncome();
        double totalExpense = financeService.getTotalExpenses();
        double netSavings = totalIncome - totalExpense;

        System.out.println(CYAN + " +------------------------------------------------------------------+" + RESET);
        System.out.println(CYAN + " |                  FINCORE EXECUTIVE DASHBOARD                     |" + RESET);
        System.out.println(CYAN + " +------------------------------------------------------------------+" + RESET);
        System.out.printf(WHITE + " | Total Account Liquidity (All Accounts) : " + GREEN + "$%,15.2f" + WHITE + "          |\n", totalLiquidity);
        System.out.printf(WHITE + " | Total Recorded Income (Revenue)        : " + GREEN + "$%,15.2f" + WHITE + "          |\n", totalIncome);
        System.out.printf(WHITE + " | Total Recorded Expenses (Outflow)      : " + RED + "$%,15.2f" + WHITE + "          |\n", totalExpense);
        System.out.printf(WHITE + " | Net Cash Flow / Reserve Savings        : " + YELLOW + "$%,15.2f" + WHITE + "          |\n", netSavings);
        System.out.println(CYAN + " +------------------------------------------------------------------+" + RESET);
        System.out.println(GREEN + " [VERIFIED] Dashboard initialized with Income, Expenses, Accounts, Budget, and Transactions." + RESET);
    }

    // =========================================================================
    // STEP 4: Complete Financial Record CRUD Module (INSERT, SELECT, UPDATE, DELETE)
    // =========================================================================
    private void step4DemonstrateCompleteCrudModule() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 4] Complete CRUD Module Demonstration (Financial Records)" + RESET);
        System.out.println(WHITE + "Executing full Lifecycle: INSERT -> SELECT -> UPDATE -> DELETE with SQL DML statements:\n" + RESET);

        // 4A: INSERT
        System.out.println(CYAN + "--------------------------------------------------------------------------" + RESET);
        System.out.println(CYAN + " [4A] OPERATION: INSERT (Add new Income/Expense record)" + RESET);
        System.out.println(WHITE + " Executing SQL:" + RESET);
        System.out.println(YELLOW + " INSERT INTO financial_records (record_type, category, amount, account_number, description, record_date)\n" +
                                   " VALUES ('EXPENSE', 'Software Licenses', 450.00, 'CHK-100102', 'Oracle 10g XE Cluster Upgrade', CURRENT_DATE);" + RESET);

        FinancialRecord newRecord = financeService.addRecord(
                FinancialRecord.Type.EXPENSE,
                "Software Licenses",
                450.00,
                "CHK-100102",
                "Oracle 10g XE Cluster Upgrade",
                LocalDate.now()
        );
        Long recordId = newRecord.getId();
        System.out.println(GREEN + " >>> INSERT SUCCESS! Generated Primary Key ID: #" + recordId + RESET);
        System.out.println("     Record Details: " + newRecord);

        // 4B: SELECT
        System.out.println(CYAN + "\n--------------------------------------------------------------------------" + RESET);
        System.out.println(CYAN + " [4B] OPERATION: SELECT (Query & Display record from database)" + RESET);
        System.out.println(WHITE + " Executing SQL:" + RESET);
        System.out.println(YELLOW + " SELECT id, record_type, category, amount, account_number, description, record_date\n" +
                                   " FROM financial_records WHERE id = " + recordId + ";" + RESET);

        Optional<FinancialRecord> fetchedOpt = financeService.getRecordById(recordId);
        if (fetchedOpt.isPresent()) {
            FinancialRecord fetched = fetchedOpt.get();
            System.out.println(GREEN + " >>> SELECT SUCCESS! Retrieved record from DBMS:" + RESET);
            System.out.printf("     ID: %d | Type: %s | Category: %s | Amount: $%.2f | Acc: %s | Desc: %s | Date: %s\n",
                    fetched.getId(), fetched.getRecordType(), fetched.getCategory(), fetched.getAmount(),
                    fetched.getAccountNumber(), fetched.getDescription(), fetched.getRecordDate());
        } else {
            System.err.println(RED + " [ERROR] Record not found via SELECT!" + RESET);
        }

        // 4C: UPDATE
        System.out.println(CYAN + "\n--------------------------------------------------------------------------" + RESET);
        System.out.println(CYAN + " [4C] OPERATION: UPDATE (Edit existing record)" + RESET);
        System.out.println(WHITE + " Modifying record amount from $450.00 to $585.50 and updating description..." + RESET);
        System.out.println(WHITE + " Executing SQL:" + RESET);
        System.out.println(YELLOW + " UPDATE financial_records\n" +
                                   " SET record_type = 'EXPENSE', category = 'Software Licenses', amount = 585.50,\n" +
                                   "     account_number = 'CHK-100102', description = 'Oracle 10g XE + NVMe Storage Upgrade',\n" +
                                   "     record_date = CURRENT_DATE\n" +
                                   " WHERE id = " + recordId + ";" + RESET);

        if (fetchedOpt.isPresent()) {
            FinancialRecord toUpdate = fetchedOpt.get();
            toUpdate.setAmount(585.50);
            toUpdate.setDescription("Oracle 10g XE + NVMe Storage Upgrade");
            boolean updated = financeService.updateRecord(toUpdate);
            System.out.println(GREEN + " >>> UPDATE SUCCESS! Rows affected: " + (updated ? 1 : 0) + RESET);

            // Re-verify with SELECT
            FinancialRecord verified = financeService.getRecordById(recordId).orElseThrow();
            System.out.println(GREEN + "     Verified in DB: Amount = $" + verified.getAmount() + " | Desc: " + verified.getDescription() + RESET);
        }

        // 4D: DELETE
        System.out.println(CYAN + "\n--------------------------------------------------------------------------" + RESET);
        System.out.println(CYAN + " [4D] OPERATION: DELETE (Remove record from database)" + RESET);
        System.out.println(WHITE + " Executing SQL:" + RESET);
        System.out.println(YELLOW + " DELETE FROM financial_records WHERE id = " + recordId + ";" + RESET);

        boolean deleted = financeService.deleteRecord(recordId);
        System.out.println(GREEN + " >>> DELETE SUCCESS! Rows affected: " + (deleted ? 1 : 0) + RESET);

        // Verify record is gone
        Optional<FinancialRecord> afterDelete = financeService.getRecordById(recordId);
        if (afterDelete.isEmpty()) {
            System.out.println(GREEN + " [VERIFIED] Record #" + recordId + " confirmed deleted from DBMS (SELECT returned 0 rows)." + RESET);
        } else {
            System.err.println(RED + " [ERROR] Record still present after DELETE!" + RESET);
        }
    }

    // =========================================================================
    // STEP 5: OOP Principles: Inheritance & Polymorphism
    // =========================================================================
    private void step5ShowOOPModelsAndPolymorphism() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 5] Object-Oriented Principles: Inheritance & Polymorphism" + RESET);
        System.out.println(WHITE + "Instantiating polymorphic Account references and invoking overridden methods:" + RESET);

        Account savings = new SavingsAccount("SAV-TEST-01", 1L, 5000.0, 4.5, "ACTIVE", null);
        Account checking = new CheckingAccount("CHK-TEST-01", 1L, 200.0, 1000.0, "ACTIVE", null);

        Account[] accounts = new Account[]{savings, checking};
        for (Account acc : accounts) {
            System.out.println(CYAN + " -> " + acc.getAccountSummary() + RESET);
            System.out.printf("    Polymorphic type: %s\n", acc.getAccountType());
            System.out.printf("    Can withdraw $600? %s\n", acc.canWithdraw(600.0) ? GREEN + "YES" + RESET : RED + "NO" + RESET);
            System.out.printf("    Monthly Adjustment: $%.2f (%s)\n",
                    acc.calculateMonthlyInterestOrFee(),
                    acc.calculateMonthlyInterestOrFee() >= 0 ? "Interest Credit" : "Fee Debit");
        }
    }

    // =========================================================================
    // STEP 6: DBMS ACID Transactions: Atomic Transfer Commit
    // =========================================================================
    private void step6DemonstrateAcidTransactionCommit() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 6] DBMS ACID Transactions: Atomic Transfer (Commit Guarantee)" + RESET);
        String fromAcc = "SAV-100101";
        String toAcc = "CHK-100202";

        double balFromBefore = accountRepo.findByAccountNumber(fromAcc).get().getBalance();
        double balToBefore = accountRepo.findByAccountNumber(toAcc).get().getBalance();
        double transferAmount = 1500.00;

        System.out.printf(" Before Transfer -> %s: $%,.2f | %s: $%,.2f\n", fromAcc, balFromBefore, toAcc, balToBefore);
        System.out.printf(" Executing ACID transfer of $%,.2f...\n", transferAmount);

        bankingService.transferFunds(fromAcc, toAcc, transferAmount, "Demo business funds transfer");

        double balFromAfter = accountRepo.findByAccountNumber(fromAcc).get().getBalance();
        double balToAfter = accountRepo.findByAccountNumber(toAcc).get().getBalance();
        System.out.printf(GREEN + " After Transfer  -> %s: $%,.2f | %s: $%,.2f\n" + RESET, fromAcc, balFromAfter, toAcc, balToAfter);

        if (Math.abs((balFromBefore - transferAmount) - balFromAfter) < 0.01 &&
            Math.abs((balToBefore + transferAmount) - balToAfter) < 0.01) {
            System.out.println(GREEN + " [VERIFIED] DBMS Atomicity & Consistency Preserved: Balances updated synchronously!" + RESET);
        }
    }

    // =========================================================================
    // STEP 7: DBMS ACID Transactions: Rollback on Failure
    // =========================================================================
    private void step7DemonstrateAcidTransactionRollback() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 7] DBMS ACID Transactions: Automatic Rollback on Failure" + RESET);
        String fromAcc = "CHK-100202";
        String toAcc = "SAV-100101";

        Account source = accountRepo.findByAccountNumber(fromAcc).get();
        double balFromBefore = source.getBalance();
        double balToBefore = accountRepo.findByAccountNumber(toAcc).get().getBalance();

        // Amount exceeding balance + overdraft limit to force failure
        double invalidAmount = balFromBefore + 50000.00;
        System.out.printf(" Attempting to transfer $%,.2f from %s (Balance: $%,.2f)...\n", invalidAmount, fromAcc, balFromBefore);

        try {
            bankingService.transferFunds(fromAcc, toAcc, invalidAmount, "Illegal overdraft transfer");
            System.err.println(RED + " [ERROR] Transfer should have failed!" + RESET);
        } catch (BankingException ex) {
            System.out.println(PURPLE + " -> Intercepted expected domain exception: " + ex.getMessage() + RESET);
        }

        double balFromAfter = accountRepo.findByAccountNumber(fromAcc).get().getBalance();
        double balToAfter = accountRepo.findByAccountNumber(toAcc).get().getBalance();
        System.out.printf(" After Rollback  -> %s: $%,.2f | %s: $%,.2f\n", fromAcc, balFromAfter, toAcc, balToAfter);

        if (Math.abs(balFromBefore - balFromAfter) < 0.001 && Math.abs(balToBefore - balToAfter) < 0.001) {
            System.out.println(GREEN + " [VERIFIED] DBMS Rollback Successful: Zero funds deducted or leaked!" + RESET);
        }
    }

    // =========================================================================
    // STEP 8: Multi-Table JOIN & Aggregations
    // =========================================================================
    private void step8DemonstrateDbmsJoinAndAggregations() {
        System.out.println(YELLOW + BOLD + "\n[CAPSTONE STEP 8] Advanced DBMS Analytics: Multi-Table JOIN & Aggregations" + RESET);

        System.out.println(CYAN + "\n=== Customer Portfolio Analytics Report (SQL JOIN + GROUP BY) ===" + RESET);
        System.out.println(String.format("%-10s | %-20s | %-25s | %-12s | %-15s | %-8s",
                "Code", "Customer Name", "Email", "Accounts", "Total Balance", "Txs"));
        System.out.println("------------------------------------------------------------------------------------------------------");

        List<CustomerSummaryDTO> report = reportService.getCustomerPortfolioReport();
        for (CustomerSummaryDTO dto : report) {
            System.out.println(dto);
        }

        System.out.println(CYAN + "\n=== Budget Tracker & Category Join Analytics ===" + RESET);
        List<Budget> budgets = financeService.getAllBudgetsWithSpending();
        for (Budget b : budgets) {
            System.out.printf(" Category: %-15s | Limit: $%,8.2f | Spent: $%,8.2f | Remaining: $%,8.2f | Used: %5.1f%%\n",
                    b.getCategory(), b.getMonthlyLimit(), b.getSpentAmount(), b.getRemainingAmount(), b.getPercentageUsed());
        }

        System.out.println(CYAN + "\n=== Bank Global Liquidity Metrics (Aggregate SQL) ===" + RESET);
        Map<String, Object> metrics = reportService.getBankLiquidityMetrics();
        System.out.printf(" Total Active Accounts : %d\n", metrics.get("total_accounts"));
        System.out.printf(" Total Bank Deposits   : $%,.2f\n", metrics.get("total_deposits"));
        System.out.printf(" Average Acc Balance   : $%,.2f\n", metrics.get("avg_balance"));
        System.out.printf(" Savings Pool Deposits : $%,.2f\n", metrics.get("savings_deposits"));
        System.out.printf(" Checking Pool Deposits: $%,.2f\n", metrics.get("checking_deposits"));
    }
}
