-- ====================================================================
-- FinCore Initial Seed Data (SQLite / MySQL)
-- ====================================================================

-- 1. Insert Authentication Users (Username & Password for DB Authentication)
INSERT OR IGNORE INTO users (id, username, password, full_name, role, status) VALUES
(1, 'admin', 'admin123', 'System Administrator', 'ADMIN', 'ACTIVE'),
(2, 'asmit', 'password123', 'Asmit Jogdand', 'MANAGER', 'ACTIVE'),
(3, 'alice', 'alice123', 'Alice Johnson', 'USER', 'ACTIVE');

-- 2. Insert Sample Customers
INSERT OR IGNORE INTO customers (id, customer_code, name, email, phone, role, status) VALUES
(1, 'CUST-1001', 'Alice Johnson', 'alice.johnson@example.com', '+1-555-0101', 'CUSTOMER', 'ACTIVE'),
(2, 'CUST-1002', 'Bob Smith', 'bob.smith@example.com', '+1-555-0102', 'CUSTOMER', 'ACTIVE'),
(3, 'CUST-1003', 'Catherine Davis', 'catherine.d@example.com', '+1-555-0103', 'CUSTOMER', 'ACTIVE'),
(4, 'ADMIN-001', 'System Administrator', 'admin@fincore.internal', '+1-555-0999', 'ADMIN', 'ACTIVE');

-- 3. Insert Sample Accounts (Savings with interest, Checking with overdraft)
INSERT OR IGNORE INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status) VALUES
('SAV-100101', 1, 'SAVINGS', 15400.50, 4.25, 0.00, 'ACTIVE'),
('CHK-100102', 1, 'CHECKING', 3200.00, 0.00, 1500.00, 'ACTIVE'),
('SAV-100201', 2, 'SAVINGS', 8750.00, 3.80, 0.00, 'ACTIVE'),
('CHK-100202', 2, 'CHECKING', 1250.75, 0.00, 1000.00, 'ACTIVE'),
('SAV-100301', 3, 'SAVINGS', 24900.00, 4.50, 0.00, 'ACTIVE');

-- 4. Insert Sample Financial Records (Income & Expenses)
INSERT OR IGNORE INTO financial_records (id, record_type, category, amount, account_number, description, record_date) VALUES
(1, 'INCOME', 'Salary', 6500.00, 'CHK-100102', 'Monthly Corporate Salary Payment', DATE('now', '-10 days')),
(2, 'INCOME', 'Consulting', 2400.00, 'SAV-100101', 'Enterprise Cloud Architecture Consultation', DATE('now', '-5 days')),
(3, 'INCOME', 'Investments', 750.00, 'SAV-100101', 'Quarterly Dividend Distribution', DATE('now', '-2 days')),
(4, 'EXPENSE', 'Housing', 1650.00, 'CHK-100102', 'Monthly Residential Lease Payment', DATE('now', '-12 days')),
(5, 'EXPENSE', 'Groceries', 485.50, 'CHK-100102', 'Weekly Supermarket & Organic Pantry Groceries', DATE('now', '-4 days')),
(6, 'EXPENSE', 'Utilities', 215.00, 'CHK-100102', 'High-Speed Fiber Internet & Electricity', DATE('now', '-3 days')),
(7, 'EXPENSE', 'Entertainment', 120.00, 'CHK-100102', 'Streaming Subscriptions & Weekend Cinema', DATE('now', '-1 day'));

-- 5. Insert Sample Category Budgets
INSERT OR IGNORE INTO budgets (id, category, monthly_limit, period_month) VALUES
(1, 'Housing', 2000.00, '2026-09'),
(2, 'Groceries', 800.00, '2026-09'),
(3, 'Utilities', 350.00, '2026-09'),
(4, 'Entertainment', 400.00, '2026-09'),
(5, 'Consulting', 5000.00, '2026-09');

-- 6. Insert Seed Transactions
INSERT OR IGNORE INTO transactions (transaction_id, account_number, type, amount, balance_after, target_account, description) VALUES
('TX-INIT-001', 'SAV-100101', 'DEPOSIT', 15400.50, 15400.50, NULL, 'Initial opening deposit'),
('TX-INIT-002', 'CHK-100102', 'DEPOSIT', 3200.00, 3200.00, NULL, 'Initial checking deposit'),
('TX-INIT-003', 'SAV-100201', 'DEPOSIT', 8750.00, 8750.00, NULL, 'Initial opening deposit'),
('TX-INIT-004', 'CHK-100202', 'DEPOSIT', 1250.75, 1250.75, NULL, 'Payroll direct deposit'),
('TX-INIT-005', 'SAV-100301', 'DEPOSIT', 24900.00, 24900.00, NULL, 'Wire transfer credit');

-- 7. Insert Seed Audit Logs
INSERT OR IGNORE INTO audit_logs (action, entity_type, entity_id, performed_by, details) VALUES
('SYSTEM_BOOTSTRAP', 'SYSTEM', 'ROOT', 'SystemInitializer', 'FinCore database initialized with users, accounts, budgets, and financial records.');
