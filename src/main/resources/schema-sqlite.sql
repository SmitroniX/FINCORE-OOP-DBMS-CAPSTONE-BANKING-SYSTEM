-- ====================================================================
-- FinCore Database Schema (SQLite)
-- Supports Relational Constraints, Foreign Keys, Indexes, Check Constraints
-- ====================================================================

PRAGMA foreign_keys = ON;

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    role TEXT NOT NULL DEFAULT 'CUSTOMER' CHECK(role IN ('CUSTOMER', 'ADMIN', 'MANAGER')),
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bank Accounts Table (Supports Savings and Checking OOP sub-types)
CREATE TABLE IF NOT EXISTS accounts (
    account_number TEXT PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    account_type TEXT NOT NULL CHECK(account_type IN ('SAVINGS', 'CHECKING')),
    balance REAL NOT NULL DEFAULT 0.0 CHECK(balance >= -10000.0),
    interest_rate REAL NOT NULL DEFAULT 0.0,
    overdraft_limit REAL NOT NULL DEFAULT 0.0,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- 3. Transactions Table (ACID Ledger with foreign keys)
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id TEXT NOT NULL UNIQUE,
    account_number TEXT NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN', 'INTEREST', 'FEE')),
    amount REAL NOT NULL CHECK(amount > 0),
    balance_after REAL NOT NULL,
    target_account TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

-- 4. Audit Logs Table (Security and Governance tracking)
CREATE TABLE IF NOT EXISTS audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    performed_by TEXT NOT NULL,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Indexes for fast query performance & DBMS optimization
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_transactions_account_num ON transactions(account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
