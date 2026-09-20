-- ====================================================================
-- FinCore Database Schema for Oracle 10g XE / Oracle Database
-- Compliant with Oracle 10g XE SQL DDL, Sequences, and Triggers
-- ====================================================================

-- 1. Users Authentication Table
CREATE TABLE users (
    id NUMBER(10) PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    full_name VARCHAR2(100) NOT NULL,
    role VARCHAR2(20) DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER', 'MANAGER')),
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE seq_users START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_users_id
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT seq_users.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
/

-- 2. Customers Table
CREATE TABLE customers (
    id NUMBER(10) PRIMARY KEY,
    customer_code VARCHAR2(30) NOT NULL UNIQUE,
    name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100) NOT NULL UNIQUE,
    phone VARCHAR2(30),
    role VARCHAR2(20) DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN', 'MANAGER')),
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE seq_customers START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_customers_id
BEFORE INSERT ON customers
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT seq_customers.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
/

-- 3. Bank Accounts Table
CREATE TABLE accounts (
    account_number VARCHAR2(30) PRIMARY KEY,
    customer_id NUMBER(10) NOT NULL,
    account_type VARCHAR2(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'CHECKING')),
    balance NUMBER(15, 2) DEFAULT 0.00 NOT NULL CHECK (balance >= -10000.0),
    interest_rate NUMBER(5, 2) DEFAULT 0.00 NOT NULL,
    overdraft_limit NUMBER(15, 2) DEFAULT 0.00 NOT NULL,
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_cust FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- 4. Financial Records Table (Income & Expense CRUD)
CREATE TABLE financial_records (
    id NUMBER(10) PRIMARY KEY,
    record_type VARCHAR2(20) NOT NULL CHECK (record_type IN ('INCOME', 'EXPENSE')),
    category VARCHAR2(50) NOT NULL,
    amount NUMBER(15, 2) NOT NULL CHECK (amount > 0),
    account_number VARCHAR2(30),
    description VARCHAR2(255),
    record_date DATE DEFAULT SYSDATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fin_acc FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE SET NULL
);

CREATE SEQUENCE seq_fin_records START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_fin_records_id
BEFORE INSERT ON financial_records
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT seq_fin_records.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
/

-- 5. Budgets Table
CREATE TABLE budgets (
    id NUMBER(10) PRIMARY KEY,
    category VARCHAR2(50) NOT NULL UNIQUE,
    monthly_limit NUMBER(15, 2) NOT NULL CHECK (monthly_limit > 0),
    period_month VARCHAR2(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE seq_budgets START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_budgets_id
BEFORE INSERT ON budgets
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT seq_budgets.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
/

-- 6. Transactions Table
CREATE TABLE transactions (
    id NUMBER(10) PRIMARY KEY,
    transaction_id VARCHAR2(50) NOT NULL UNIQUE,
    account_number VARCHAR2(30) NOT NULL,
    type VARCHAR2(20) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN', 'INTEREST', 'FEE')),
    amount NUMBER(15, 2) NOT NULL CHECK (amount > 0),
    balance_after NUMBER(15, 2) NOT NULL,
    target_account VARCHAR2(30),
    description VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_acc FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

CREATE SEQUENCE seq_transactions START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_transactions_id
BEFORE INSERT ON transactions
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT seq_transactions.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
/

-- 7. Audit Logs Table
CREATE TABLE audit_logs (
    id NUMBER(10) PRIMARY KEY,
    action VARCHAR2(50) NOT NULL,
    entity_type VARCHAR2(50) NOT NULL,
    entity_id VARCHAR2(50) NOT NULL,
    performed_by VARCHAR2(100) NOT NULL,
    details VARCHAR2(4000),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE seq_audit_logs START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_audit_logs_id
BEFORE INSERT ON audit_logs
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT seq_audit_logs.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
/

-- 8. Indexes for performance
CREATE INDEX idx_ora_users_user ON users(username);
CREATE INDEX idx_ora_acc_cust ON accounts(customer_id);
CREATE INDEX idx_ora_fin_type ON financial_records(record_type);
CREATE INDEX idx_ora_fin_cat ON financial_records(category);
CREATE INDEX idx_ora_tx_acc ON transactions(account_number);
CREATE INDEX idx_ora_tx_time ON transactions(created_at);
CREATE INDEX idx_ora_audit_time ON audit_logs(timestamp);
