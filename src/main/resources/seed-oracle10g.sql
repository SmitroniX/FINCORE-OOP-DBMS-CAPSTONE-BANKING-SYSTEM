-- ====================================================================
-- FinCore Initial Seed Data (Oracle 10g XE / Enterprise)
-- ====================================================================

-- 1. Insert Authentication Users
INSERT INTO users (id, username, password, full_name, role, status)
VALUES (seq_users.NEXTVAL, 'admin', 'admin123', 'System Administrator', 'ADMIN', 'ACTIVE');

INSERT INTO users (id, username, password, full_name, role, status)
VALUES (seq_users.NEXTVAL, 'asmit', 'password123', 'Asmit Jogdand', 'MANAGER', 'ACTIVE');

INSERT INTO users (id, username, password, full_name, role, status)
VALUES (seq_users.NEXTVAL, 'alice', 'alice123', 'Alice Johnson', 'USER', 'ACTIVE');

-- 2. Insert Sample Customers
INSERT INTO customers (id, customer_code, name, email, phone, role, status)
VALUES (seq_customers.NEXTVAL, 'CUST-1001', 'Alice Johnson', 'alice.johnson@example.com', '+1-555-0101', 'CUSTOMER', 'ACTIVE');

INSERT INTO customers (id, customer_code, name, email, phone, role, status)
VALUES (seq_customers.NEXTVAL, 'CUST-1002', 'Bob Smith', 'bob.smith@example.com', '+1-555-0102', 'CUSTOMER', 'ACTIVE');

INSERT INTO customers (id, customer_code, name, email, phone, role, status)
VALUES (seq_customers.NEXTVAL, 'CUST-1003', 'Catherine Davis', 'catherine.d@example.com', '+1-555-0103', 'CUSTOMER', 'ACTIVE');

-- 3. Insert Accounts
INSERT INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status)
VALUES ('SAV-100101', 1, 'SAVINGS', 15400.50, 4.25, 0.00, 'ACTIVE');

INSERT INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status)
VALUES ('CHK-100102', 1, 'CHECKING', 3200.00, 0.00, 1500.00, 'ACTIVE');

INSERT INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status)
VALUES ('SAV-100201', 2, 'SAVINGS', 8750.00, 3.80, 0.00, 'ACTIVE');

INSERT INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status)
VALUES ('CHK-100202', 2, 'CHECKING', 1250.75, 0.00, 1000.00, 'ACTIVE');

-- 4. Insert Financial Records (Income & Expenses)
INSERT INTO financial_records (id, record_type, category, amount, account_number, description, record_date)
VALUES (seq_fin_records.NEXTVAL, 'INCOME', 'Salary', 6500.00, 'CHK-100102', 'Monthly Corporate Salary Payment', SYSDATE - 10);

INSERT INTO financial_records (id, record_type, category, amount, account_number, description, record_date)
VALUES (seq_fin_records.NEXTVAL, 'INCOME', 'Consulting', 2400.00, 'SAV-100101', 'Enterprise Cloud Architecture Consultation', SYSDATE - 5);

INSERT INTO financial_records (id, record_type, category, amount, account_number, description, record_date)
VALUES (seq_fin_records.NEXTVAL, 'EXPENSE', 'Housing', 1650.00, 'CHK-100102', 'Monthly Residential Lease Payment', SYSDATE - 12);

INSERT INTO financial_records (id, record_type, category, amount, account_number, description, record_date)
VALUES (seq_fin_records.NEXTVAL, 'EXPENSE', 'Groceries', 485.50, 'CHK-100102', 'Weekly Supermarket & Organic Pantry Groceries', SYSDATE - 4);

INSERT INTO financial_records (id, record_type, category, amount, account_number, description, record_date)
VALUES (seq_fin_records.NEXTVAL, 'EXPENSE', 'Utilities', 215.00, 'CHK-100102', 'High-Speed Fiber Internet & Electricity', SYSDATE - 3);

-- 5. Insert Budgets
INSERT INTO budgets (id, category, monthly_limit, period_month)
VALUES (seq_budgets.NEXTVAL, 'Housing', 2000.00, '2026-09');

INSERT INTO budgets (id, category, monthly_limit, period_month)
VALUES (seq_budgets.NEXTVAL, 'Groceries', 800.00, '2026-09');

INSERT INTO budgets (id, category, monthly_limit, period_month)
VALUES (seq_budgets.NEXTVAL, 'Utilities', 350.00, '2026-09');

INSERT INTO budgets (id, category, monthly_limit, period_month)
VALUES (seq_budgets.NEXTVAL, 'Entertainment', 400.00, '2026-09');

-- 6. Insert Transactions
INSERT INTO transactions (id, transaction_id, account_number, type, amount, balance_after, target_account, description)
VALUES (seq_transactions.NEXTVAL, 'TX-INIT-001', 'SAV-100101', 'DEPOSIT', 15400.50, 15400.50, NULL, 'Initial opening deposit');

INSERT INTO transactions (id, transaction_id, account_number, type, amount, balance_after, target_account, description)
VALUES (seq_transactions.NEXTVAL, 'TX-INIT-002', 'CHK-100102', 'DEPOSIT', 3200.00, 3200.00, NULL, 'Initial checking deposit');

-- 7. Insert Audit Logs
INSERT INTO audit_logs (id, action, entity_type, entity_id, performed_by, details)
VALUES (seq_audit_logs.NEXTVAL, 'SYSTEM_BOOTSTRAP', 'SYSTEM', 'ROOT', 'SystemInitializer', 'FinCore Oracle 10g XE database initialized.');

COMMIT;
