# 🎤 FinCore Capstone Project: 4-Person Presentation & Explanation Script

> **Team Size:** 4 Presenters  
> **Total Duration:** 10 – 12 Minutes (~2.5 to 3 minutes per speaker)  
> **Project Name:** FinCore - Enterprise Banking & Finance Management System  
> **Technology Stack:** Java 17, Java Swing (`javax.swing`), JDBC, Oracle 10g XE / SQLite, Maven  
> **Repository:** [https://github.com/SmitroniX/FINCORE-OOP-DBMS-CAPSTONE-BANKING-SYSTEM](https://github.com/SmitroniX/FINCORE-OOP-DBMS-CAPSTONE-BANKING-SYSTEM)

---

## 👥 Speaker Roles & Agenda

| Speaker | Role | Presentation Focus | Live UI / Code Demonstration |
|---|---|---|---|
| **Speaker 1** | Team Lead & Architect | • Project Overview & Motivation<br>• 3-Tier Enterprise Architecture<br>• Oracle DB-backed Authentication (Login) | Launch Application, Show Login Screen, Authenticate via DB |
| **Speaker 2** | OOP & Domain Specialist | • OOP Principles: Encapsulation & Inheritance<br>• Runtime Polymorphism in Banking<br>• Financial Domain Entities | Code walkthrough: `Account` hierarchy & polymorphic adjustments |
| **Speaker 3** | DBMS & Transactions Engineer | • Relational Schema & Oracle 10g XE Design<br>• Foreign Keys & Integrity Constraints<br>• ACID Transactions (Commit & Rollback) | Terminal trace: Atomic transfer commit and simulated rollback |
| **Speaker 4** | Dashboard & CRUD Lead | • Java Swing Finance Dashboard Walkthrough<br>• **Complete CRUD Demonstration** (INSERT, SELECT, UPDATE, DELETE)<br>• Live JDBC SQL/DML Execution Inspector | Live CRUD actions in UI/Terminal & DML queries with timings |

---

## 🗣️ Speaker 1: Introduction, Architecture & Database Authentication
**⏱️ Duration:** ~2.5 Minutes  
**🎬 Cue:** Launch the application (`./run.sh --gui` or `./run.sh --demo`) and show the **Login Screen**.

### 🎙️ Spoken Script:

> "Good morning, respected evaluators and fellow colleagues.
> 
> Today, our team is proud to present **FinCore**, an enterprise-grade Banking and Finance Management System developed as our Object-Oriented Programming and Database Management Systems Capstone Project.
> 
> In modern financial software engineering, two foundational pillars are required:
> 1. **Robust Object-Oriented Architecture** to model complex real-world financial entities cleanly and securely.
> 2. **Relational Database Integrity & Performance** powered by a DBMS such as **Oracle 10g XE**, guaranteeing strict ACID consistency and relational constraints.
> 
> Too often in academic projects, OOP models and DBMS queries are isolated. In FinCore, we have seamlessly unified them into an enterprise **3-Tier Architecture**:
> - **The Presentation Tier:** Built using **Java Swing** (`JFrame`, `JTabbedPane`, `JTable`), providing an intuitive graphical dashboard, paired with a robust terminal CLI and automated demonstration runner.
> - **The Business Service Tier:** Encapsulating core banking logic, validation rules, session handling, and transaction boundaries.
> - **The Data Access Repository Tier:** Employing the Repository Pattern and pure **JDBC** with 100% parameterized `PreparedStatements` to guarantee zero SQL injection risk.
> - **The Relational DBMS Engine:** Fully compatible with **Oracle 10g Express Edition (XE)** using sequences and triggers, with built-in zero-config SQLite support for seamless portability.
> 
> To demonstrate this, let us look at our first requirement: **Database-Backed Authentication**.
> 
> *(Speaker 1 points to the Login Screen)*
> 
> Instead of hardcoding credentials, FinCore authenticates users directly against the relational database `users` table. When an operator enters their username and password, our `AuthService` executes a parameterized SQL query:
> 
> ```sql
> SELECT id, username, password, full_name, role, status, created_at 
> FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE';
> ```
> 
> If an attacker supplies an invalid password or non-existent username, our system immediately intercepts the failure, logs the incident to `audit_logs`, and rejects access. When we authenticate with valid credentials—such as our seeded Administrator `admin` / `admin123`—the system retrieves the operator's security profile and launches our main Finance Dashboard.
> 
> Now, I will pass the floor to **Speaker 2**, who will walk us through how we applied Object-Oriented Principles to model our banking domain."

---

## 🗣️ Speaker 2: OOP Principles (Encapsulation, Inheritance & Polymorphism)
**⏱️ Duration:** ~2.5 Minutes  
**🎬 Cue:** Display the UML Class Diagram (from `docs/ERD_AND_UML.md`) and code snippets of `Account`, `SavingsAccount`, and `CheckingAccount`.

### 🎙️ Spoken Script:

> "Thank you, Speaker 1.
> 
> In FinCore, Object-Oriented Programming is not merely an academic checklist—it directly dictates how money moves and how financial business rules are enforced.
> 
> We incorporated four major OOP tenets:
> 
> 1. **Encapsulation:**
>    In our `Account` class, all core attributes—including `balance`, `customerId`, and `status`—are strictly `private`. Outside components cannot directly mutate balances. Instead, state transitions occur exclusively through synchronized business methods: `deposit()` and `withdraw()`, which enforce domain invariants like rejecting negative amounts or operating on frozen accounts.
> 
> 2. **Inheritance:**
>    We designed an abstract base class `Account` which provides common banking logic and encapsulates account identifiers and audit timestamps. From this base, we extended two distinct account types:
>    - `SavingsAccount`, which models wealth accumulation and long-term deposits.
>    - `CheckingAccount`, which models commercial liquidity and transaction overdrafts.
> 
> 3. **Runtime Polymorphism (Dynamic Method Dispatch):**
>    We defined two core polymorphic method contracts on `Account`:
>    - `public abstract boolean canWithdraw(double amount);`
>    - `public abstract double calculateMonthlyInterestOrFee();`
> 
>    At runtime, Java executes the subclass implementation dynamically:
>    - In **`SavingsAccount`**, withdrawals enforce a strict **Minimum Balance** (e.g. $50.00). If a withdrawal would breach this limit, it is rejected. During monthly batch processing, it calculates a **positive interest credit** based on its Annual Percentage Rate (APR).
>    - In **`CheckingAccount`**, the customer is permitted to withdraw beyond their balance up to an authorized **Overdraft Credit Limit** (e.g. $1,000.00). During monthly batch runs, it deducts a **monthly maintenance fee**.
> 
> 4. **Polymorphic Database Hydration:**
>    When reading records from the relational DBMS via JDBC, our repository inspects the `account_type` column and polymorphically instantiates the correct subclass. The higher-level business services interact solely with generic `Account` references, adhering to the Open/Closed Principle.
> 
> Now, **Speaker 3** will present our relational DBMS architecture, Oracle 10g XE schema design, and ACID transaction guarantees."

---

## 🗣️ Speaker 3: DBMS Architecture, Oracle 10g XE & ACID Transactions
**⏱️ Duration:** ~3.0 Minutes  
**🎬 Cue:** Display the Entity-Relationship (ER) Diagram and demonstrate the ACID Transfer Commit and Rollback.

### 🎙️ Spoken Script:

> "Thank you, Speaker 2.
> 
> Let us now inspect the relational database architecture powering FinCore.
> 
> When dealing with banking and ledger systems, database design must eliminate anomalies and prevent data loss. Our relational schema comprises 7 core tables:
> 1. `users`: Stores authenticated operators with unique usernames and role designations.
> 2. `customers`: Contains customer master data with `UNIQUE` email and customer codes.
> 3. `accounts`: Linked to `customers` via Foreign Key with cascading updates and deletes.
> 4. `transactions`: An immutable double-entry ledger recording deposits, withdrawals, and inter-account transfers.
> 5. `financial_records`: The repository for all revenue income and expenditure records.
> 6. `budgets`: Category allocation limits used for budget tracking.
> 7. `audit_logs`: A regulatory audit trail capturing every sensitive action, operator, and timestamp.
> 
> ### Oracle 10g XE Database Design:
> To ensure compatibility with **Oracle 10g Express Edition**, we engineered dialect-specific SQL scripts (`schema-oracle10g.sql` and `seed-oracle10g.sql`):
> - Primary keys are generated using Oracle **Sequences** (`seq_users`, `seq_fin_records`, etc.) paired with `BEFORE INSERT` triggers.
> - Monetary columns are typed as `NUMBER(15,2)` for exact precision, avoiding floating-point rounding errors.
> - Text fields use `VARCHAR2`.
> - The Oracle JDBC driver `ojdbc11` is configured in our Maven POM.
> 
> ### ACID Transactions:
> The most critical requirement in banking is **ACID compliance** (Atomicity, Consistency, Isolation, Durability).
> 
> Consider an inter-account fund transfer: Account A transfers $1,500 to Account B. This requires 4 coordinated database operations:
> 1. Deduct $1,500 from Account A.
> 2. Credit $1,500 to Account B.
> 3. Insert `TRANSFER_OUT` record in transactions.
> 4. Insert `TRANSFER_IN` record in transactions.
> 
> In `BankingService.java`, we manage transaction boundaries explicitly using JDBC:
> ```java
> conn.setAutoCommit(false); // Begin ACID Transaction
> ```
> If any step fails—for example, if Account A lacks sufficient funds or the database experiences a network timeout—the catch block invokes:
> ```java
> conn.rollback(); // Restore pristine initial state
> ```
> This guarantees **Atomicity**: either all updates succeed completely, or none occur at all. Not a single cent is ever lost or created out of thin air.
> 
> I now hand over to **Speaker 4**, who will demonstrate our Java Swing Finance Dashboard and walk through the complete CRUD lifecycle with live SQL execution."

---

## 🗣️ Speaker 4: Finance Dashboard, Complete CRUD Demonstration & Live SQL Trace
**⏱️ Duration:** ~3.0 Minutes  
**🎬 Cue:** Switch to the active **Finance Dashboard** in Java Swing (or terminal output of `Step 4: CRUD Module`) showing the CRUD table and the bottom **Live JDBC SQL/DML Inspector Panel**.

### 🎙️ Spoken Script:

> "Thank you, Speaker 3.
> 
> Now, let us explore the **Finance Dashboard** and demonstrate our **Complete CRUD Module** in action.
> 
> *(Speaker 4 navigates the Java Swing Dashboard)*
> 
> As you can see, our dashboard provides comprehensive visibility into all financial operations:
> - **Tab 1: Dashboard Overview:** Real-time summary metric cards displaying Total Account Liquidity ($56,943.90), Total Income ($9,650.00), Total Expenses ($2,470.50), and Net Cash Flow ($7,179.50).
> - **Tab 2: Income & Expenses (The CRUD Module):** Interactive data grid for managing financial records.
> - **Tab 3: Accounts:** All savings and checking accounts with APR and overdraft limits.
> - **Tab 4: Budget Tracker:** Relational join calculating spent amounts against monthly limits in real time.
> - **Tab 5: Transactions Ledger:** Full double-entry audit history.
> - **Tab 6: Financial Reports:** Category spending breakdown and expense distribution.
> - **At the bottom of the screen:** Notice our **Live JDBC SQL & DML Execution Inspector**. Every query executed by the application is captured in real-time with its execution time in milliseconds and affected row count!
> 
> ### Live Demonstration of the Complete CRUD Module:
> 
> Watch as we perform the complete 4-step CRUD lifecycle on financial records:
> 
> 1. **INSERT (Create Record):**
>    - We click `➕ Add Record (INSERT)`.
>    - We select `EXPENSE`, category `Software Licenses`, amount `$450.00`, account `CHK-100102`, and memo `Oracle 10g XE Cluster Upgrade`.
>    - When we click Save, the system executes:
>      ```sql
>      INSERT INTO financial_records 
>      (record_type, category, amount, account_number, description, record_date) 
>      VALUES (?, ?, ?, ?, ?, ?);
>      ```
>    - The database generates a new Primary Key—Record #8—and logs the DML operation in under 2 milliseconds.
> 
> 2. **SELECT (Read & Display):**
>    - The table immediately refreshes using:
>      ```sql
>      SELECT id, record_type, category, amount, account_number, description, record_date 
>      FROM financial_records WHERE id = 8;
>      ```
>    - The new record is displayed cleanly in the JTable, and overview metric cards update synchronously.
> 
> 3. **UPDATE (Edit Record):**
>    - We select Record #8 and click `✏️ Edit Record (UPDATE)`.
>    - We adjust the amount from `$450.00` to `$585.50` and append `+ NVMe Storage` to the description.
>    - The system executes:
>      ```sql
>      UPDATE financial_records 
>      SET amount = ?, description = ? 
>      WHERE id = ?;
>      ```
>    - The table reflects the updated amount, and our budget calculation dynamically recalibrates.
> 
> 4. **DELETE (Remove Record):**
>    - Finally, we select the record and click `🗑️ Delete Record (DELETE)`.
>    - A confirmation dialog verifies our intent, executing:
>      ```sql
>      DELETE FROM financial_records WHERE id = 8;
>      ```
>    - The record is purged from the database, and subsequent `SELECT` queries confirm that 0 rows exist for that ID.
> 
> ### Conclusion & Verification:
> In summary, FinCore satisfies all academic and enterprise specifications:
> - Full Object-Oriented Design (Encapsulation, Inheritance, Polymorphism).
> - Relational Database Management with Oracle 10g XE compatibility.
> - Database-backed Authentication with audit trails.
> - An interactive Java Swing graphical dashboard with a real-time SQL execution inspector.
> - Complete and verified CRUD operations backed by 14 automated unit and integration tests.
> 
> Thank you very much for your time and attention. We now welcome questions from the evaluation panel."

---

## 🎯 Quick Presentation Tips & Q&A Preparation

### 1. How to run the demo during the presentation:
```bash
# Option A: Run automated verification showcase in terminal
./run.sh --demo

# Option B: Run Java Swing graphical GUI (with Login and Dashboard)
./run.sh --gui

# Option C: Run interactive console menu
./run.sh --cli
```

### 2. Common Evaluator Questions & Recommended Answers:
- **Q: Why did you use `PreparedStatement` instead of `Statement`?**  
  *Answer:* `PreparedStatement` pre-compiles SQL queries on the database server and parameterizes input values, completely preventing SQL Injection attacks and improving execution performance.
- **Q: How does Oracle 10g XE handle auto-incrementing primary keys?**  
  *Answer:* Oracle 10g XE utilizes database `SEQUENCE` objects (e.g., `seq_fin_records.NEXTVAL`) combined with `BEFORE INSERT` triggers to assign unique identifiers.
- **Q: Where is Polymorphism demonstrated in code?**  
  *Answer:* In `Account.java`, subclasses `SavingsAccount` and `CheckingAccount` override `canWithdraw()` (minimum balance vs. overdraft allowance) and `calculateMonthlyInterestOrFee()` (APR interest vs. maintenance fees).
