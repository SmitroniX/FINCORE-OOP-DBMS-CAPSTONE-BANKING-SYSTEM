package com.fincore.service;

import com.fincore.model.*;
import com.fincore.repository.AccountRepository;
import com.fincore.repository.AuditLogRepository;
import com.fincore.repository.CustomerRepository;
import com.fincore.service.exception.BankingException;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Service managing customer life-cycles and account openings.
 */
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final AuditLogRepository auditRepo;
    private final BankingService bankingService;

    public CustomerService(CustomerRepository customerRepo,
                           AccountRepository accountRepo,
                           AuditLogRepository auditRepo,
                           BankingService bankingService) {
        this.customerRepo = customerRepo;
        this.accountRepo = accountRepo;
        this.auditRepo = auditRepo;
        this.bankingService = bankingService;
    }

    /**
     * Registers a new customer into the database.
     */
    public Customer registerCustomer(String name, String email, String phone) {
        if (customerRepo.findByEmail(email).isPresent()) {
            throw new BankingException("A customer with email " + email + " already exists.");
        }

        String customerCode = "CUST-" + (1000 + new Random().nextInt(9000));
        Customer customer = new Customer(customerCode, name, email, phone);
        Customer saved = customerRepo.save(customer);

        auditRepo.log("CREATE_CUSTOMER", "CUSTOMER", saved.getUserCode(), "SYSTEM",
                "New customer onboarded: " + saved.getName());

        return saved;
    }

    /**
     * Opens a new Savings Account for a customer.
     */
    public SavingsAccount openSavingsAccount(Long customerId, double initialDeposit, double interestRate) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new BankingException("Customer ID " + customerId + " not found."));

        String accNumber = "SAV-" + (100000 + new Random().nextInt(900000));
        SavingsAccount account = new SavingsAccount(accNumber, customer.getId(), 0.0, interestRate, "ACTIVE", null);
        accountRepo.save(account);

        if (initialDeposit > 0) {
            bankingService.deposit(accNumber, initialDeposit, "Initial Account Opening Deposit");
        }

        auditRepo.log("OPEN_ACCOUNT", "SAVINGS_ACCOUNT", accNumber, "SYSTEM",
                "Opened Savings Account for " + customer.getName() + " with $" + initialDeposit);

        return (SavingsAccount) accountRepo.findByAccountNumber(accNumber).orElse(account);
    }

    /**
     * Opens a new Checking Account for a customer.
     */
    public CheckingAccount openCheckingAccount(Long customerId, double initialDeposit, double overdraftLimit) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new BankingException("Customer ID " + customerId + " not found."));

        String accNumber = "CHK-" + (100000 + new Random().nextInt(900000));
        CheckingAccount account = new CheckingAccount(accNumber, customer.getId(), 0.0, overdraftLimit, "ACTIVE", null);
        accountRepo.save(account);

        if (initialDeposit > 0) {
            bankingService.deposit(accNumber, initialDeposit, "Initial Account Opening Deposit");
        }

        auditRepo.log("OPEN_ACCOUNT", "CHECKING_ACCOUNT", accNumber, "SYSTEM",
                "Opened Checking Account for " + customer.getName() + " with $" + initialDeposit);

        return (CheckingAccount) accountRepo.findByAccountNumber(accNumber).orElse(account);
    }

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepo.findById(id);
    }

    public List<Account> getAccountsByCustomerId(Long customerId) {
        return accountRepo.findByCustomerId(customerId);
    }
}
