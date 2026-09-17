-- ====================================================================
-- FinCore Initial Seed Data
-- ====================================================================

-- Insert Sample Customers
INSERT OR IGNORE INTO customers (id, customer_code, name, email, phone, role, status) VALUES
(1, 'CUST-1001', 'Alice Johnson', 'alice.johnson@example.com', '+1-555-0101', 'CUSTOMER', 'ACTIVE'),
(2, 'CUST-1002', 'Bob Smith', 'bob.smith@example.com', '+1-555-0102', 'CUSTOMER', 'ACTIVE'),
(3, 'CUST-1003', 'Catherine Davis', 'catherine.d@example.com', '+1-555-0103', 'CUSTOMER', 'ACTIVE'),
(4, 'ADMIN-001', 'System Administrator', 'admin@fincore.internal', '+1-555-0999', 'ADMIN', 'ACTIVE');

-- Insert Sample Accounts (Savings with interest, Checking with overdraft)
INSERT OR IGNORE INTO accounts (account_number, customer_id, account_type, balance, interest_rate, overdraft_limit, status) VALUES
('SAV-100101', 1, 'SAVINGS', 15400.50, 4.25, 0.00, 'ACTIVE'),
('CHK-100102', 1, 'CHECKING', 3200.00, 0.00, 1500.00, 'ACTIVE'),
('SAV-100201', 2, 'SAVINGS', 8750.00, 3.80, 0.00, 'ACTIVE'),
('CHK-100202', 2, 'CHECKING', 1250.75, 0.00, 1000.00, 'ACTIVE'),
('SAV-100301', 3, 'SAVINGS', 24900.00, 4.50, 0.00, 'ACTIVE');

-- Insert Seed Transactions
INSERT OR IGNORE INTO transactions (transaction_id, account_number, type, amount, balance_after, target_account, description) VALUES
('TX-INIT-001', 'SAV-100101', 'DEPOSIT', 15400.50, 15400.50, NULL, 'Initial opening deposit'),
('TX-INIT-002', 'CHK-100102', 'DEPOSIT', 3200.00, 3200.00, NULL, 'Initial checking deposit'),
('TX-INIT-003', 'SAV-100201', 'DEPOSIT', 8750.00, 8750.00, NULL, 'Initial opening deposit'),
('TX-INIT-004', 'CHK-100202', 'DEPOSIT', 1250.75, 1250.75, NULL, 'Payroll direct deposit'),
('TX-INIT-005', 'SAV-100301', 'DEPOSIT', 24900.00, 24900.00, NULL, 'Wire transfer credit');

-- Insert Seed Audit Logs
INSERT OR IGNORE INTO audit_logs (action, entity_type, entity_id, performed_by, details) VALUES
('SYSTEM_BOOTSTRAP', 'SYSTEM', 'ROOT', 'SystemInitializer', 'FinCore banking database initialized with seed records.');
