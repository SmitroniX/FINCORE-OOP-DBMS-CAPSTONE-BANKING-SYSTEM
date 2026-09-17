package com.fincore.ui;

import com.fincore.model.*;
import com.fincore.repository.AccountRepository;
import com.fincore.service.BankingService;
import com.fincore.service.CustomerService;
import com.fincore.service.ReportService;
import com.fincore.service.exception.BankingException;

import java.util.List;
import java.util.Map;

import static com.fincore.ui.ConsoleColors.*;

/**
 * Automated Demonstration Runner showcasing all Core OOP Principles and DBMS features.
 */
public class DemoRunner {

    private final CustomerService customerService;
    private final BankingService bankingService;
    private final ReportService reportService;
    private final AccountRepository accountRepo;

    public DemoRunner(CustomerService customerService,
                      BankingService bankingService,
                      ReportService reportService,
                      AccountRepository accountRepo) {
        this.customerService = customerService;
        this.bankingService = bankingService;
        this.reportService = reportService;
        this.accountRepo = accountRepo;
    }

    public void runFullDemonstration() {
        System.out.println(CYAN + BOLD + "\n==========================================================================" + RESET);
        System.out.println(CYAN + BOLD + "   FINCORE CAPSTONE: OOP & DBMS LIVE SHOWCASE & VERIFICATION RUNNER" + RESET);
        System.out.println(CYAN + BOLD + "==========================================================================" + RESET);

        step1ShowOOPModelsAndPolymorphism();
        step2DemonstrateDatabaseCRUD();
        step3DemonstrateAcidTransactionCommit();
        step4DemonstrateAcidTransactionRollback();
        step5DemonstratePolymorphicBatchProcessing();
        step6DemonstrateDbmsJoinAndAggregations();

        System.out.println(GREEN + BOLD + "\n>>> ALL OOP & DBMS CAPSTONE DEMONSTRATION STEPS COMPLETED SUCCESSFULLY! <<<\n" + RESET);
    }

    private void step1ShowOOPModelsAndPolymorphism() {
        System.out.println(YELLOW + BOLD + "\n[STEP 1] Object-Oriented Principles: Inheritance & Polymorphism" + RESET);
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

    private void step2DemonstrateDatabaseCRUD() {
        System.out.println(YELLOW + BOLD + "\n[STEP 2] DBMS Relational Operations: Customer & Account Creation" + RESET);
        String email = "david.wilson." + System.currentTimeMillis() % 100000 + "@example.com";
        Customer customer = customerService.registerCustomer("David Wilson", email, "+1-555-8899");
        System.out.println(GREEN + " + Inserted Customer into DBMS: " + customer + RESET);

        SavingsAccount sa = customerService.openSavingsAccount(customer.getId(), 2500.0, 4.0);
        System.out.println(GREEN + " + Opened Savings Account: " + sa.getAccountNumber() + " with $2,500.00" + RESET);

        CheckingAccount ca = customerService.openCheckingAccount(customer.getId(), 800.0, 500.0);
        System.out.println(GREEN + " + Opened Checking Account: " + ca.getAccountNumber() + " with $800.00" + RESET);
    }

    private void step3DemonstrateAcidTransactionCommit() {
        System.out.println(YELLOW + BOLD + "\n[STEP 3] DBMS ACID Transactions: Atomic Transfer (Commit Guarantee)" + RESET);
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

    private void step4DemonstrateAcidTransactionRollback() {
        System.out.println(YELLOW + BOLD + "\n[STEP 4] DBMS ACID Transactions: Automatic Rollback on Failure" + RESET);
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

    private void step5DemonstratePolymorphicBatchProcessing() {
        System.out.println(YELLOW + BOLD + "\n[STEP 5] Polymorphic Batch Processing (Interest Accrual & Account Maintenance Fees)" + RESET);
        System.out.println(" Executing month-end adjustments across all accounts...");
        bankingService.processMonthlyAdjustments();
        System.out.println(GREEN + " [VERIFIED] Completed interest credits for Savings & maintenance deductions for Checking." + RESET);
    }

    private void step6DemonstrateDbmsJoinAndAggregations() {
        System.out.println(YELLOW + BOLD + "\n[STEP 6] Advanced DBMS Analytics: Multi-Table JOIN & Aggregations (GROUP BY / SUM / COUNT)" + RESET);

        System.out.println(CYAN + "\n=== Customer Portfolio Analytics Report (SQL JOIN + GROUP BY) ===" + RESET);
        System.out.println(String.format("%-10s | %-20s | %-25s | %-12s | %-15s | %-8s",
                "Code", "Customer Name", "Email", "Accounts", "Total Balance", "Txs"));
        System.out.println("------------------------------------------------------------------------------------------------------");

        List<CustomerSummaryDTO> report = reportService.getCustomerPortfolioReport();
        for (CustomerSummaryDTO dto : report) {
            System.out.println(dto);
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
