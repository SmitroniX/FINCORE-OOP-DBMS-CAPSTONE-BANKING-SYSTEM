-- ====================================================================
-- FinCore Database Schema for Oracle Database (Free / XE / 23c / 21c / 10g)
-- Production PL/SQL Packages, Triggers, Views, Sequences & Constraints
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

-- 4. Financial Records Table (CRUD Income & Expense)
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

-- ====================================================================
-- BUSINESS TRIGGERS
-- ====================================================================

-- Trigger A: Automatically log transaction event into audit_logs
CREATE OR REPLACE TRIGGER trg_audit_tx
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (id, action, entity_type, entity_id, performed_by, details, timestamp)
    VALUES (
        seq_audit_logs.NEXTVAL,
        'TRANSACTION_' || :NEW.type,
        'ACCOUNT',
        :NEW.account_number,
        'SYSTEM',
        'Amount: $' || TO_CHAR(:NEW.amount, '999990.99') || ', Balance After: $' || TO_CHAR(:NEW.balance_after, '999990.99') || ', Target: ' || NVL(:NEW.target_account, 'N/A'),
        SYSTIMESTAMP
    );
END;
/

-- Trigger B: Real-time budget warning trigger on financial expense
CREATE OR REPLACE TRIGGER trg_check_budget_alert
AFTER INSERT ON financial_records
FOR EACH ROW
WHEN (NEW.record_type = 'EXPENSE')
DECLARE
    v_limit NUMBER(15, 2);
    v_total_spent NUMBER(15, 2);
BEGIN
    SELECT monthly_limit INTO v_limit FROM budgets WHERE category = :NEW.category AND ROWNUM = 1;
    SELECT NVL(SUM(amount), 0) INTO v_total_spent FROM financial_records WHERE category = :NEW.category AND record_type = 'EXPENSE';
    IF v_total_spent > v_limit THEN
        INSERT INTO audit_logs (id, action, entity_type, entity_id, performed_by, details, timestamp)
        VALUES (
            seq_audit_logs.NEXTVAL,
            'BUDGET_OVERRUN_ALERT',
            'BUDGET',
            :NEW.category,
            'SYSTEM',
            'Category ' || :NEW.category || ' exceeded monthly limit of $' || TO_CHAR(v_limit, '999990.99') || '. Current total spent: $' || TO_CHAR(v_total_spent, '999990.99'),
            SYSTIMESTAMP
        );
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        NULL;
END;
/

-- ====================================================================
-- RELATIONAL VIEWS FOR REPORTING & ANALYTICS
-- ====================================================================

-- View 1: Customer Portfolio multi-table join
CREATE OR REPLACE VIEW v_customer_portfolio AS
SELECT 
    c.id AS customer_id,
    c.customer_code,
    c.name AS customer_name,
    c.email,
    c.role,
    c.status,
    COUNT(DISTINCT a.account_number) AS total_accounts,
    NVL(SUM(CASE WHEN a.account_type = 'SAVINGS' THEN a.balance ELSE 0 END), 0) AS savings_balance,
    NVL(SUM(CASE WHEN a.account_type = 'CHECKING' THEN a.balance ELSE 0 END), 0) AS checking_balance,
    NVL(SUM(a.balance), 0) AS net_worth,
    COUNT(t.id) AS total_transactions
FROM customers c
LEFT JOIN accounts a ON c.id = a.customer_id
LEFT JOIN transactions t ON a.account_number = t.account_number
GROUP BY c.id, c.customer_code, c.name, c.email, c.role, c.status;

-- View 2: Budget Summary comparing allocated budget vs actual spending
CREATE OR REPLACE VIEW v_budget_summary AS
SELECT 
    b.id AS budget_id,
    b.category,
    b.monthly_limit,
    b.period_month,
    NVL(f.total_spent, 0) AS total_spent,
    (b.monthly_limit - NVL(f.total_spent, 0)) AS remaining_budget,
    CASE 
        WHEN b.monthly_limit > 0 THEN ROUND((NVL(f.total_spent, 0) / b.monthly_limit) * 100, 2)
        ELSE 0 
    END AS utilization_pct
FROM budgets b
LEFT JOIN (
    SELECT category, SUM(amount) AS total_spent
    FROM financial_records
    WHERE record_type = 'EXPENSE'
    GROUP BY category
) f ON b.category = f.category;

-- View 3: Complete Account Ledger with transaction audit trail
CREATE OR REPLACE VIEW v_account_ledger AS
SELECT 
    a.account_number,
    a.account_type,
    a.balance,
    c.name AS customer_name,
    c.customer_code,
    t.transaction_id,
    t.type AS tx_type,
    t.amount AS tx_amount,
    t.balance_after,
    t.created_at AS tx_time,
    t.description AS tx_desc
FROM accounts a
JOIN customers c ON a.customer_id = c.id
LEFT JOIN transactions t ON a.account_number = t.account_number;

-- View 4: Monthly Financial Inflow/Outflow Summary
CREATE OR REPLACE VIEW v_monthly_financial_report AS
SELECT 
    TO_CHAR(record_date, 'YYYY-MM') AS report_month,
    NVL(SUM(CASE WHEN record_type = 'INCOME' THEN amount ELSE 0 END), 0) AS total_income,
    NVL(SUM(CASE WHEN record_type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS total_expense,
    (NVL(SUM(CASE WHEN record_type = 'INCOME' THEN amount ELSE 0 END), 0) - 
     NVL(SUM(CASE WHEN record_type = 'EXPENSE' THEN amount ELSE 0 END), 0)) AS net_savings,
    COUNT(*) AS record_count
FROM financial_records
GROUP BY TO_CHAR(record_date, 'YYYY-MM');

-- ====================================================================
-- PL/SQL PACKAGE: PKG_BANKING_OPERATIONS
-- ====================================================================

CREATE OR REPLACE PACKAGE PKG_BANKING_OPERATIONS AS
    PROCEDURE TRANSFER_FUNDS(
        p_from_account IN VARCHAR2,
        p_to_account   IN VARCHAR2,
        p_amount       IN NUMBER,
        p_description  IN VARCHAR2,
        p_status       OUT VARCHAR2,
        p_message      OUT VARCHAR2
    );

    PROCEDURE ADD_FINANCIAL_RECORD(
        p_type         IN VARCHAR2,
        p_category     IN VARCHAR2,
        p_amount       IN NUMBER,
        p_acc_number   IN VARCHAR2,
        p_description  IN VARCHAR2,
        p_new_id       OUT NUMBER
    );

    FUNCTION GET_CUSTOMER_NET_WORTH(p_customer_id IN NUMBER) RETURN NUMBER;
    FUNCTION GET_CATEGORY_SPENT(p_category IN VARCHAR2) RETURN NUMBER;
END PKG_BANKING_OPERATIONS;
/

CREATE OR REPLACE PACKAGE BODY PKG_BANKING_OPERATIONS AS

    PROCEDURE TRANSFER_FUNDS(
        p_from_account IN VARCHAR2,
        p_to_account   IN VARCHAR2,
        p_amount       IN NUMBER,
        p_description  IN VARCHAR2,
        p_status       OUT VARCHAR2,
        p_message      OUT VARCHAR2
    ) IS
        v_from_bal NUMBER(15, 2);
        v_from_overdraft NUMBER(15, 2);
        v_from_status VARCHAR2(20);
        v_to_bal NUMBER(15, 2);
        v_to_status VARCHAR2(20);
        v_tx_id1 VARCHAR2(50);
        v_tx_id2 VARCHAR2(50);
    BEGIN
        IF p_amount <= 0 THEN
            p_status := 'FAIL';
            p_message := 'Transfer amount must be strictly greater than zero.';
            RETURN;
        END IF;

        IF p_from_account = p_to_account THEN
            p_status := 'FAIL';
            p_message := 'Source and destination accounts cannot be identical.';
            RETURN;
        END IF;

        -- Lock source account
        BEGIN
            SELECT balance, overdraft_limit, status 
            INTO v_from_bal, v_from_overdraft, v_from_status
            FROM accounts 
            WHERE account_number = p_from_account 
            FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                p_status := 'FAIL';
                p_message := 'Source account not found: ' || p_from_account;
                RETURN;
        END;

        IF v_from_status != 'ACTIVE' THEN
            p_status := 'FAIL';
            p_message := 'Source account is not active (Status: ' || v_from_status || ').';
            RETURN;
        END IF;

        -- Check sufficient balance + overdraft
        IF (v_from_bal + v_from_overdraft) < p_amount THEN
            p_status := 'FAIL';
            p_message := 'Insufficient funds in ' || p_from_account || '. Available balance: $' || TO_CHAR(v_from_bal + v_from_overdraft, '999990.99');
            RETURN;
        END IF;

        -- Lock target account
        BEGIN
            SELECT balance, status 
            INTO v_to_bal, v_to_status
            FROM accounts 
            WHERE account_number = p_to_account 
            FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                p_status := 'FAIL';
                p_message := 'Target account not found: ' || p_to_account;
                RETURN;
        END;

        IF v_to_status != 'ACTIVE' THEN
            p_status := 'FAIL';
            p_message := 'Target account is not active (Status: ' || v_to_status || ').';
            RETURN;
        END IF;

        -- Update balances
        UPDATE accounts SET balance = balance - p_amount WHERE account_number = p_from_account;
        UPDATE accounts SET balance = balance + p_amount WHERE account_number = p_to_account;

        -- Create unique transaction ledger entries
        v_tx_id1 := 'TX-TRF-OUT-' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');
        v_tx_id2 := 'TX-TRF-IN-' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');

        INSERT INTO transactions (id, transaction_id, account_number, type, amount, balance_after, target_account, description)
        VALUES (seq_transactions.NEXTVAL, v_tx_id1, p_from_account, 'TRANSFER_OUT', p_amount, v_from_bal - p_amount, p_to_account, p_description);

        INSERT INTO transactions (id, transaction_id, account_number, type, amount, balance_after, target_account, description)
        VALUES (seq_transactions.NEXTVAL, v_tx_id2, p_to_account, 'TRANSFER_IN', p_amount, v_to_bal + p_amount, p_from_account, p_description);

        COMMIT;
        p_status := 'SUCCESS';
        p_message := 'Successfully transferred $' || TO_CHAR(p_amount, '999990.99') || ' from ' || p_from_account || ' to ' || p_to_account;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            p_status := 'ERROR';
            p_message := 'PL/SQL Exception: ' || SQLERRM;
    END TRANSFER_FUNDS;

    PROCEDURE ADD_FINANCIAL_RECORD(
        p_type         IN VARCHAR2,
        p_category     IN VARCHAR2,
        p_amount       IN NUMBER,
        p_acc_number   IN VARCHAR2,
        p_description  IN VARCHAR2,
        p_new_id       OUT NUMBER
    ) IS
    BEGIN
        SELECT seq_fin_records.NEXTVAL INTO p_new_id FROM dual;
        INSERT INTO financial_records (id, record_type, category, amount, account_number, description, record_date)
        VALUES (p_new_id, p_type, p_category, p_amount, p_acc_number, p_description, SYSDATE);
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END ADD_FINANCIAL_RECORD;

    FUNCTION GET_CUSTOMER_NET_WORTH(p_customer_id IN NUMBER) RETURN NUMBER IS
        v_total NUMBER(15, 2) := 0;
    BEGIN
        SELECT NVL(SUM(balance), 0) INTO v_total FROM accounts WHERE customer_id = p_customer_id;
        RETURN v_total;
    END GET_CUSTOMER_NET_WORTH;

    FUNCTION GET_CATEGORY_SPENT(p_category IN VARCHAR2) RETURN NUMBER IS
        v_spent NUMBER(15, 2) := 0;
    BEGIN
        SELECT NVL(SUM(amount), 0) INTO v_spent FROM financial_records WHERE category = p_category AND record_type = 'EXPENSE';
        RETURN v_spent;
    END GET_CATEGORY_SPENT;

END PKG_BANKING_OPERATIONS;
/

-- ====================================================================
-- PERFORMANCE INDEXES
-- ====================================================================
CREATE INDEX idx_ora_users_user ON users(username);
CREATE INDEX idx_ora_acc_cust ON accounts(customer_id);
CREATE INDEX idx_ora_fin_type ON financial_records(record_type);
CREATE INDEX idx_ora_fin_cat ON financial_records(category);
CREATE INDEX idx_ora_tx_acc ON transactions(account_number);
CREATE INDEX idx_ora_tx_time ON transactions(created_at);
CREATE INDEX idx_ora_audit_time ON audit_logs(timestamp);
