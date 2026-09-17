# 📊 FinCore: ER Diagram, UML Class Diagrams & Architectural Design

This document contains complete system diagrams, entity-relationship models, UML class hierarchies, sequence diagrams, and the database schema data dictionary for the **FinCore Banking System Capstone Project**.

---

## 📑 Table of Contents
1. [Entity-Relationship (ER) Diagram](#1-entity-relationship-erd-diagram)
2. [UML Class Diagram (OOP Architecture)](#2-uml-class-diagram-oop-architecture)
3. [ACID Transaction Sequence Diagram](#3-acid-transaction-sequence-diagram)
4. [3-Tier System Architecture Diagram](#4-3-tier-system-architecture-diagram)
5. [Database Data Dictionary & Relational Specification](#5-database-data-dictionary)

---

## 1. Entity-Relationship (ER) Diagram

The following diagram models the relational schema, cardinality, primary keys (`PK`), foreign keys (`FK`), and attributes.

```mermaid
erDiagram
    CUSTOMERS ||--o{ ACCOUNTS : "owns (1:N)"
    ACCOUNTS ||--o{ TRANSACTIONS : "generates (1:N)"
    CUSTOMERS ||--o{ AUDIT_LOGS : "triggers actions (1:N)"

    CUSTOMERS {
        BIGINT id PK "Auto Increment"
        VARCHAR customer_code UK "Unique Identifier"
        VARCHAR name "Full Name"
        VARCHAR email UK "Unique Email Address"
        VARCHAR phone "Contact Number"
        VARCHAR role "CUSTOMER / ADMIN / MANAGER"
        VARCHAR status "ACTIVE / SUSPENDED / CLOSED"
        TIMESTAMP created_at "Registration Timestamp"
    }

    ACCOUNTS {
        VARCHAR account_number PK "Unique Account Number"
        BIGINT customer_id FK "References CUSTOMERS(id)"
        VARCHAR account_type "SAVINGS / CHECKING"
        DECIMAL balance "Current Balance"
        DECIMAL interest_rate "Annual APR (for Savings)"
        DECIMAL overdraft_limit "Credit Allowance (for Checking)"
        VARCHAR status "ACTIVE / FROZEN / CLOSED"
        TIMESTAMP created_at "Account Creation Timestamp"
    }

    TRANSACTIONS {
        BIGINT id PK "Auto Increment"
        VARCHAR transaction_id UK "UUID Reference"
        VARCHAR account_number FK "References ACCOUNTS(account_number)"
        VARCHAR type "DEPOSIT / WITHDRAWAL / TRANSFER_OUT / TRANSFER_IN"
        DECIMAL amount "Transaction Value (>0)"
        DECIMAL balance_after "Ledger Balance After Execution"
        VARCHAR target_account "Recipient/Sender Account Number"
        VARCHAR description "Memo or Transaction Purpose"
        TIMESTAMP created_at "Execution Timestamp"
    }

    AUDIT_LOGS {
        BIGINT id PK "Auto Increment"
        VARCHAR action "SYSTEM Action Code"
        VARCHAR entity_type "Target Entity Name"
        VARCHAR entity_id "Target Entity Key"
        VARCHAR performed_by "Operator or System User"
        TEXT details "Operation Payload and Audit Trace"
        TIMESTAMP timestamp "Occurrence Timestamp"
    }
```

### Relational Integrity Rules:
* **One-to-Many (`1:N`) Customers to Accounts**: A customer can maintain multiple bank accounts (e.g., Savings and Checking), but each account belongs to exactly one customer (`ON DELETE CASCADE`).
* **One-to-Many (`1:N`) Accounts to Transactions**: An account generates multiple ledger movements, but each transaction entry strictly belongs to one primary account.
* **Auditability**: All state mutations generate append-only logs in `audit_logs`.

---

## 2. UML Class Diagram (OOP Architecture)

This diagram illustrates **Abstraction**, **Inheritance**, **Polymorphism**, and **Dependency Inversion** between the Presentation, Service, Repository, and Model layers.

```mermaid
classDiagram
    %% Hierarchy: User Domain
    class User {
        <<abstract>>
        -Long id
        -String userCode
        -String name
        -String email
        -String phone
        -String status
        -LocalDateTime createdAt
        +getRoleDescription()* String
        +getId() Long
        +getName() String
        +getEmail() String
    }

    class Customer {
        -String customerTier
        +getRoleDescription() String
        +getCustomerTier() String
    }

    class Admin {
        -String department
        -int clearanceLevel
        +getRoleDescription() String
        +getDepartment() String
    }

    User <|-- Customer : Extends
    User <|-- Admin : Extends

    %% Hierarchy: Account Domain
    class Account {
        <<abstract>>
        -String accountNumber
        -Long customerId
        -double balance
        -String status
        -LocalDateTime createdAt
        +canWithdraw(double amount)* boolean
        +calculateMonthlyInterestOrFee()* double
        +getAccountType()* AccountType
        +getAccountSummary()* String
        +deposit(double amount) void
        +withdraw(double amount) void
        +getBalance() double
    }

    class SavingsAccount {
        -double interestRate
        -double minimumBalance
        +canWithdraw(double amount) boolean
        +calculateMonthlyInterestOrFee() double
        +getAccountType() AccountType
        +getAccountSummary() String
    }

    class CheckingAccount {
        -double overdraftLimit
        -double monthlyMaintenanceFee
        +canWithdraw(double amount) boolean
        +calculateMonthlyInterestOrFee() double
        +getAccountType() AccountType
        +getAccountSummary() String
    }

    Account <|-- SavingsAccount : Implements (APR & Min Bal)
    Account <|-- CheckingAccount : Implements (Overdraft & Fee)

    %% Repository Contracts
    class CrudRepository~T, ID~ {
        <<interface>>
        +save(T entity) T
        +findById(ID id) Optional~T~
        +findAll() List~T~
        +update(T entity) boolean
        +deleteById(ID id) boolean
    }

    class CustomerRepository {
        <<interface>>
        +findByCustomerCode(String code) Optional~Customer~
        +getCustomerPortfolioSummaries() List~CustomerSummaryDTO~
    }

    class AccountRepository {
        <<interface>>
        +findByAccountNumber(String accNum) Optional~Account~
        +findByCustomerId(Long customerId) List~Account~
        +updateBalance(Connection conn, String accNum, double bal) boolean
    }

    CrudRepository <|-- CustomerRepository
    CrudRepository <|-- AccountRepository

    %% Service Layer
    class BankingService {
        -DatabaseManager dbManager
        -AccountRepository accountRepo
        -TransactionRepository txRepo
        -AuditLogRepository auditRepo
        +deposit(String accNum, double amount, String memo) Transaction
        +withdraw(String accNum, double amount, String memo) Transaction
        +transferFunds(String from, String to, double amt, String memo) void
        +processMonthlyAdjustments() void
    }

    BankingService --> AccountRepository : Uses
    BankingService --> Account : Manipulates polymorphically
```

---

## 3. ACID Transaction Sequence Diagram

This sequence diagram details the end-to-end execution of `BankingService.transferFunds()` demonstrating **DBMS Atomicity**, **Rollback protection**, and **Double-Entry Ledger recording**.

```mermaid
sequenceDiagram
    autonumber
    actor User as Client / CLI
    participant Service as BankingService
    participant Conn as JDBC Connection
    participant AccRepo as AccountRepository
    participant TxRepo as TransactionRepository
    participant Audit as AuditLogRepository
    participant DB as Relational Database

    User->>Service: transferFunds(FromAcc, ToAcc, $1500)
    Service->>Conn: setAutoCommit(false) [BEGIN TRANSACTION]
    
    Service->>AccRepo: findByAccountNumber(conn, FromAcc)
    AccRepo->>DB: SELECT * FROM accounts WHERE account_number = ?
    DB-->>AccRepo: Return Source Account
    
    Service->>AccRepo: findByAccountNumber(conn, ToAcc)
    AccRepo->>DB: SELECT * FROM accounts WHERE account_number = ?
    DB-->>AccRepo: Return Target Account
    
    rect rgb(230, 245, 255)
        Note over Service: OOP Validation: Source.canWithdraw($1500)
    end

    alt Insufficient Funds or Account Inactive
        Service->>Conn: rollback() [RESTORE INITIAL STATE]
        Service->>Conn: setAutoCommit(true)
        Service-->>User: Throw InsufficientFundsException (0 funds deducted)
    else Validation Successful
        Note over Service: Source.withdraw(1500) & Target.deposit(1500)
        Service->>AccRepo: updateBalance(conn, FromAcc, newSourceBal)
        AccRepo->>DB: UPDATE accounts SET balance = ? WHERE account_number = ?
        
        Service->>AccRepo: updateBalance(conn, ToAcc, newTargetBal)
        AccRepo->>DB: UPDATE accounts SET balance = ? WHERE account_number = ?
        
        Service->>TxRepo: save(conn, TransferOutRecord)
        TxRepo->>DB: INSERT INTO transactions (TRANSFER_OUT, amount, ...)
        
        Service->>TxRepo: save(conn, TransferInRecord)
        TxRepo->>DB: INSERT INTO transactions (TRANSFER_IN, amount, ...)
        
        Service->>Audit: log(conn, "TRANSFER", ...)
        Audit->>DB: INSERT INTO audit_logs (...)
        
        Service->>Conn: commit() [ATOMIC PERSISTENCE]
        Service->>Conn: setAutoCommit(true)
        Service-->>User: Transfer Completed Successfully
    end
```

---

## 4. 3-Tier System Architecture Diagram

```mermaid
flowchart TD
    subgraph Presentation_Layer["1. Presentation Layer"]
        CLI["Interactive Console Menu (ConsoleMenu.java)"]
        DEMO["Automated Verification Demo (DemoRunner.java)"]
    end

    subgraph Business_Service_Layer["2. Business Logic & Service Layer"]
        BS["BankingService (ACID Transfers, Interest, Fees)"]
        CS["CustomerService (Customer Lifecycle & Onboarding)"]
        RS["ReportService (DBMS Relational Analytics & DTOs)"]
    end

    subgraph Data_Access_Layer["3. Data Access (DAO / Repository) Layer"]
        CR["JdbcCustomerRepository"]
        AR["JdbcAccountRepository (Polymorphic Row Hydration)"]
        TR["JdbcTransactionRepository (Immutable Ledger)"]
        ALR["JdbcAuditLogRepository (Security Logging)"]
        DBM["DatabaseManager (Connection Lifecycle & Transaction Pool)"]
    end

    subgraph Storage_Layer["4. Relational Storage Engine"]
        SQLITE[("SQLite (Embedded fincore_banking.db)")]
        MYSQL[("MySQL (Enterprise InnoDB Server)")]
    end

    CLI --> BS & CS & RS
    DEMO --> BS & CS & RS

    BS --> AR & TR & ALR & DBM
    CS --> CR & AR & ALR
    RS --> CR & AR & TR & ALR & DBM

    CR & AR & TR & ALR --> DBM
    DBM --> SQLITE
    DBM -.->|Switchable in db.properties| MYSQL
```

---

## 5. Database Data Dictionary

### Table 1: `customers`
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` / `BIGINT` | `PRIMARY KEY`, `AUTOINCREMENT` | Unique internal customer ID |
| `customer_code` | `VARCHAR(30)` | `NOT NULL`, `UNIQUE` | Business identifier (e.g., `CUST-1001`) |
| `name` | `VARCHAR(100)` | `NOT NULL` | Customer's full name |
| `email` | `VARCHAR(100)` | `NOT NULL`, `UNIQUE` | Unique contact email address |
| `phone` | `VARCHAR(30)` | `NULLABLE` | Telephone contact number |
| `role` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'CUSTOMER'` | Role classification (`CUSTOMER`, `ADMIN`) |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'ACTIVE'` | Status (`ACTIVE`, `SUSPENDED`, `CLOSED`) |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Account creation timestamp |

### Table 2: `accounts`
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `account_number` | `VARCHAR(30)` | `PRIMARY KEY` | Unique account string (e.g. `SAV-100101`) |
| `customer_id` | `INTEGER` / `BIGINT` | `NOT NULL`, `FOREIGN KEY` | References `customers(id)` with `CASCADE` |
| `account_type` | `VARCHAR(20)` | `NOT NULL`, `CHECK (SAVINGS/CHECKING)` | Discriminator for polymorphic class mapping |
| `balance` | `DECIMAL(15,2)` | `NOT NULL`, `DEFAULT 0.00` | Account ledger balance |
| `interest_rate` | `DECIMAL(5,2)` | `DEFAULT 0.00` | Annual interest percentage (for Savings) |
| `overdraft_limit`| `DECIMAL(15,2)` | `DEFAULT 0.00` | Overdraft credit allowance (for Checking) |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'ACTIVE'` | Operational status (`ACTIVE`, `FROZEN`, etc.) |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Account creation timestamp |

### Table 3: `transactions`
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` / `BIGINT` | `PRIMARY KEY`, `AUTOINCREMENT` | Internal transaction ID |
| `transaction_id` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` | External UUID reference identifier |
| `account_number` | `VARCHAR(30)` | `NOT NULL`, `FOREIGN KEY` | References `accounts(account_number)` with `CASCADE` |
| `type` | `VARCHAR(20)` | `NOT NULL` | Type: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_OUT`, etc. |
| `amount` | `DECIMAL(15,2)` | `NOT NULL`, `CHECK (amount > 0)` | Absolute value transferred |
| `balance_after` | `DECIMAL(15,2)` | `NOT NULL` | Balance of the account after this transaction |
| `target_account` | `VARCHAR(30)` | `NULLABLE` | Destination/source account number for transfers |
| `description` | `VARCHAR(255)` | `NULLABLE` | Transfer memo or payment purpose |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Transaction execution timestamp |

### Table 4: `audit_logs`
| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` / `BIGINT` | `PRIMARY KEY`, `AUTOINCREMENT` | Primary key |
| `action` | `VARCHAR(50)` | `NOT NULL` | Action code (`TRANSFER`, `CREATE_ACCOUNT`, etc.) |
| `entity_type` | `VARCHAR(50)` | `NOT NULL` | Entity name (`CUSTOMER`, `ACCOUNT`, `TRANSACTION`) |
| `entity_id` | `VARCHAR(50)` | `NOT NULL` | Identifier of affected record |
| `performed_by` | `VARCHAR(100)` | `NOT NULL` | User or automated process |
| `details` | `TEXT` | `NULLABLE` | Human-readable log details |
| `timestamp` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Log record timestamp |

---

## 6. Database Indexes for Performance Optimization

To guarantee high throughput and minimize scan overhead on high-frequency lookups, the following B-Tree indexes are defined:

1. **`idx_accounts_customer_id`** on `accounts(customer_id)`: Enables instant sub-millisecond retrieval of all accounts owned by a customer.
2. **`idx_transactions_account_num`** on `transactions(account_number)`: Optimizes account statement lookups and transaction history.
3. **`idx_transactions_created_at`** on `transactions(created_at)`: Optimizes date-filtered reporting and month-end ledger audits.
4. **`idx_audit_logs_timestamp`** on `audit_logs(timestamp)`: Optimizes regulatory compliance history searches.
