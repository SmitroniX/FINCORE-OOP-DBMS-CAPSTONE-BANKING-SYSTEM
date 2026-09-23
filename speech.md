# 🎤 FinCore Capstone Project: 4-Person Presentation & Explanation Script

> **Project Name:** FinCore - Enterprise Banking & Finance Management System  
> **Team Size:** 4 Presenters  
> **Total Duration:** 10 – 12 Minutes (~2.5 to 3 minutes per speaker)  
> **Technology Stack:** Java 21 (LTS), JavaFX 21 (`javafx-controls`, `javafx-fxml`), Maven (`javafx-maven-plugin:0.0.8`), Oracle Database in Docker (`gvenzl/oracle-free:23-slim`), Oracle JDBC (`ojdbc11:23.26.3.0.0`), PL/SQL Stored Packages & Triggers, Windows CMD Launchers (`.bat`)  
> **Repository:** [https://github.com/SmitroniX/FINCORE-OOP-DBMS-CAPSTONE-BANKING-SYSTEM](https://github.com/SmitroniX/FINCORE-OOP-DBMS-CAPSTONE-BANKING-SYSTEM)  

---

## 👥 Speaker Roles & Presentation Agenda

| Speaker | Role | Core Topics & Presentation Focus | Live Demonstration / Visual Cue |
|---|---|---|---|
| **Speaker 1** | Team Lead & Software Architect | • Project Mission & Modern Architecture<br>• Tech Stack: Java 21, JavaFX 21 & Windows CMD<br>• Oracle DB-Backed Authentication (Login Module) | Start DB (`start-db.bat`), Launch App (`run.bat`), Show Login View, Authenticate with Oracle DB |
| **Speaker 2** | OOP & Domain Specialist | • Encapsulation & Domain Invariants<br>• Inheritance & Polymorphism in Banking Accounts<br>• Design Patterns: Repository, Observer, Factory | Walk through UML Class Diagram (`docs/ERD_AND_UML.md`) & `SavingsAccount` vs `CheckingAccount` |
| **Speaker 3** | DBMS & Transactions Engineer | • Relational Schema & Oracle DB in Docker<br>• PL/SQL Package (`PKG_BANKING_OPERATIONS`)<br>• Business Triggers, Relational Views & ACID Integrity | Show ER Diagram, Docker container status, PL/SQL transfer execution, and Relational Views |
| **Speaker 4** | Dashboard & CRUD Lead | • JavaFX 21 Modern Finance Dashboard<br>• **Complete 4-Step CRUD Lifecycle** (INSERT, SELECT, UPDATE, DELETE)<br>• Live SQL/PLSQL Stream & Transfer Execution | Execute live CRUD in UI, show Live SQL console, demonstrate PL/SQL inter-account fund transfer |

---

## 🗣️ Speaker 1: Introduction, Architecture & Database Authentication
**⏱️ Duration:** ~2.5 Minutes  
**🎬 Visual Cue:** Show the Windows Command Prompt, run `start-db.bat` (or verify Oracle in Docker), execute `run.bat` to launch the **JavaFX 21 Login Screen**, and highlight the Database Status badge.

### 🎙️ Spoken Script:

> "Good morning, respected evaluators and panel members.
> 
> Today, our team is thrilled to present **FinCore**, an enterprise-grade Banking and Finance Management System engineered for our Object-Oriented Programming and Database Management Systems Capstone Project.
> 
> Modern financial systems demand two uncompromisable foundations:
> 1. **Robust Object-Oriented Architecture** that accurately models complex real-world financial entities, balances, and risk policies.
> 2. **Enterprise Relational Database Management** running a industrial-grade DBMS such as **Oracle Database**, guaranteeing ACID transaction consistency, stored procedural logic (PL/SQL), and strict relational constraints.
> 
> Rather than treating OOP and DBMS as disconnected subjects, FinCore unites them into a cohesive, production-ready enterprise stack:
> - **Language Platform:** Built on **Java 21 (LTS)**, taking advantage of modern Java features and strong type safety.
> - **Presentation Tier:** A sleek, reactive desktop interface built using **JavaFX 21** with a custom dark financial theme, dynamic charting, and responsive tables.
> - **Database Engine:** **Oracle Database (Free / 23c)** containerized inside **Docker** using the official `gvenzl/oracle-free:23-slim` image, communicating via the high-performance Oracle JDBC Driver **`ojdbc11` (version 23.26.3.0.0)**.
> - **Native Platform Support:** Fully engineered for native **Windows Command Prompt (CMD)** execution using zero-friction batch automation scripts: `start-db.bat`, `run.bat`, `run-gui.bat`, and `run-demo.bat`.
> 
> Let us begin by demonstrating our first core requirement: **Database-Backed Authentication**.
> 
> *(Speaker 1 gestures to the JavaFX Login Screen)*
> 
> In FinCore, user credentials are not hardcoded or stored in flat files. Every authentication request queries the relational `users` table inside Oracle Database via parameterized JDBC:
> 
> ```sql
> SELECT id, username, password, full_name, role, status, created_at 
> FROM users 
> WHERE username = ? AND password = ? AND status = 'ACTIVE'
> ```
> 
> If an operator enters an incorrect password, the system blocks access, prevents SQL injection via `PreparedStatement`, and records the security event in our `audit_logs` table.
> 
> When we log in using seeded credentials—such as our Administrator account `admin` / `admin123`—the system authenticates the session against Oracle DB, loads the operator profile, and unlocks the main Finance Dashboard.
> 
> I now turn the floor over to **Speaker 2**, who will explain how we leveraged Object-Oriented Principles to model our core banking domain."

---

## 🗣️ Speaker 2: OOP Principles (Encapsulation, Inheritance & Polymorphism)
**⏱️ Duration:** ~2.5 Minutes  
**🎬 Visual Cue:** Display the UML Class Diagram from `docs/ERD_AND_UML.md`, highlighting the `Account` class hierarchy and repository interfaces.

### 🎙️ Spoken Script:

> "Thank you, Speaker 1.
> 
> In FinCore, Object-Oriented Programming is not just an organizational strategy—it is the direct mechanism that enforces banking business rules and prevents financial loss.
> 
> Let us examine the four foundational OOP principles implemented in our codebase:
> 
> 1. **Encapsulation & Data Hiding:**
>    In our base class `Account`, all sensitive monetary fields—`balance`, `customerId`, and `status`—are strictly `private`. External classes cannot alter an account balance directly. Instead, state transitions can only occur through synchronized domain methods: `deposit()` and `withdraw()`. These methods guard against negative values, unauthorized operations, and frozen accounts.
> 
> 2. **Inheritance:**
>    We designed an abstract base class `Account` which provides common attributes (such as account number, creation timestamp, and owner identification). From this base, we derived two specialized account types:
>    - `SavingsAccount`: Models wealth generation, interest accrual, and minimum balance reserves.
>    - `CheckingAccount`: Models day-to-day liquidity, commercial operations, and authorized overdraft credit lines.
> 
> 3. **Runtime Polymorphism (Dynamic Method Dispatch):**
>    The abstract `Account` class establishes two critical polymorphic method contracts:
>    ```java
>    public abstract boolean canWithdraw(double amount);
>    public abstract double calculateMonthlyInterestOrFee();
>    ```
>    At runtime, the JVM dynamically dispatches the correct business rule depending on the concrete account instance:
>    - For a **`SavingsAccount`**, `canWithdraw()` enforces a mandatory **Minimum Balance** rule (e.g., $50.00). If a withdrawal would drop the balance below this threshold, it is strictly denied. During monthly billing cycles, it polymorphically computes a **positive interest dividend** based on its APR.
>    - For a **`CheckingAccount`**, `canWithdraw()` allows the balance to dip below zero up to an authorized **Overdraft Limit** (e.g., $1,000.00). During monthly billing cycles, it polymorphically deducts an **account maintenance service fee**.
> 
> 4. **Design Patterns:**
>    - **Repository Pattern (`CrudRepository<T, ID>`):** Decouples higher-level banking services from low-level JDBC SQL statements.
>    - **Observer Pattern:** Used by our `DatabaseManager` to broadcast live executed SQL queries directly to our UI console in real time.
>    - **Data Transfer Objects (DTOs):** Encapsulate portfolio summaries, budget variances, and transaction payloads cleanly between layers.
> 
> Now, **Speaker 3** will take us under the hood of our DBMS architecture, Docker deployment, and Oracle PL/SQL stored logic."

---

## 🗣️ Speaker 3: Relational DBMS Architecture, Oracle in Docker & PL/SQL Logic
**⏱️ Duration:** ~3.0 Minutes  
**🎬 Visual Cue:** Display the Entity-Relationship (ER) Diagram, the running Docker container (`docker ps`), and the PL/SQL package definitions (`PKG_BANKING_OPERATIONS`).

### 🎙️ Spoken Script:

> "Thank you, Speaker 2.
> 
> Let us dive into the Database Management Systems (DBMS) engineering powering FinCore.
> 
> ### Containerized Oracle Database:
> To ensure enterprise authenticity, we containerized **Oracle Database (Free / 23c)** in Docker using `docker-compose.yml`. With a single command (`start-db.bat` on Windows or `docker compose up -d`), the container boots on port `1521`, automatically runs our initialization schemas (`01_schema.sql` and `02_seed.sql`), and provisions the pluggable database service `FREEPDB1`. Our application connects natively using the latest Oracle JDBC driver **`ojdbc11:23.26.3.0.0`**.
> 
> *(Speaker 3 points to the ER Diagram)*
> 
> ### Relational Schema & Constraints:
> Our schema comprises 7 tightly-coupled relational tables:
> 1. `users`: System operators with unique login handles and role permissions.
> 2. `customers`: Customer master records with unique national identifiers and emails.
> 3. `accounts`: Bank accounts linked to customers with `ON DELETE CASCADE` foreign keys and `CHECK` constraints on balance limits.
> 4. `transactions`: Double-entry audit ledger recording deposits, withdrawals, and inter-account transfers.
> 5. `financial_records`: Comprehensive income and expense tracking table for cash flow analysis.
> 6. `budgets`: Monthly category spending limits with unique constraints.
> 7. `audit_logs`: Regulatory audit log capturing operations, timestamps, and operator IDs.
> 
> ### Advanced In-Database Logic: PL/SQL Packages, Triggers & Views:
> Beyond standard SQL, we pushed critical business logic directly into the Oracle Database:
> 
> 1. **PL/SQL Stored Package (`PKG_BANKING_OPERATIONS`):**
>    We authored a production PL/SQL package containing:
>    - `TRANSFER_FUNDS(p_source, p_target, p_amount, p_desc)`: Executes an atomic multi-table transfer, updating balances, checking overdraft limits, and inserting double-entry ledger rows directly within Oracle's kernel.
>    - `ADD_FINANCIAL_RECORD(...)`: Validates and records income/expense items atomically.
>    - `GET_CUSTOMER_NET_WORTH(p_cust_id)` and `GET_CATEGORY_SPENT(...)`: High-performance PL/SQL functions for portfolio calculations.
> 
> 2. **Database Business Triggers:**
>    - `trg_audit_tx`: An automated trigger that fires on every transaction insertion, automatically writing an immutable trail to `audit_logs`.
>    - `trg_check_budget_alert`: A trigger that inspects expense entries in real time and raises budget alert flags if spending surpasses threshold allocations.
> 
> 3. **Relational Views for Instant Analytics:**
>    - `v_customer_portfolio`: Joins customers with accounts to aggregate total net worth and active account counts.
>    - `v_budget_summary`: Calculates monthly allocated budget vs actual spending, computing variance and status flags (`OK`, `WARNING`, `EXCEEDED`).
>    - `v_account_ledger` and `v_monthly_financial_report`: Provide unified reporting streams without repeating complex joins in application code.
> 
> ### ACID Transaction Guarantees:
> In `BankingService.java`, fund transfers are executed either via our Oracle PL/SQL package or through parameterized JDBC with explicit transaction demarcation:
> ```java
> conn.setAutoCommit(false); // Begin ACID Transaction
> ```
> If any step fails—such as an invalid target account or an overdraft breach—the system calls `conn.rollback()`. This guarantees **Atomicity**: both accounts update simultaneously, or neither does. No orphan funds can ever occur.
> 
> Now, **Speaker 4** will demonstrate our JavaFX Finance Dashboard and lead the live CRUD and SQL execution walkthrough."

---

## 🗣️ Speaker 4: JavaFX Finance Dashboard, Complete CRUD Demonstration & Live SQL Trace
**⏱️ Duration:** ~3.0 Minutes  
**🎬 Visual Cue:** Switch to the active **JavaFX 21 Finance Dashboard**. Navigate across tabs, perform live CRUD operations, and highlight the bottom **Live SQL/PLSQL Stream Console**.

### 🎙️ Spoken Script:

> "Thank you, Speaker 3.
> 
> Now, let us walk through the **FinCore JavaFX Finance Dashboard** and demonstrate our **Complete CRUD Module** operating against the live Oracle Database.
> 
> *(Speaker 4 navigates the JavaFX 21 Dashboard tabs)*
> 
> Notice the rich, dark-mode financial interface:
> - **Tab 1: 📊 Overview:** Features live KPI metric cards (Total Liquidity, Total Income, Total Expenses, Net Cash Flow) alongside interactive visual charts (Income vs Expense Pie Chart and Category Breakdown Bar Chart).
> - **Tab 2: 💰 Financial Records (Complete CRUD):** The central management grid for income and expense transactions.
> - **Tab 3: 🏦 Accounts:** Displays all savings and checking accounts with real-time balance calculations.
> - **Tab 4: 🎯 Budget Tracker:** Displays category limits vs actual spending with progress meters and alert badges.
> - **Tab 5: 📜 Transactions Ledger:** Full immutable audit history.
> - **Tab 6: 📈 Relational Views:** Direct window into Oracle's `v_customer_portfolio` and `v_budget_summary` views.
> - **Tab 7: ⚡ Live SQL Console:** An interactive SQL terminal allowing evaluators to run arbitrary queries directly against Oracle Database and view formatted tabular results!
> - **Bottom Bar:** Notice our **Live SQL & PL/SQL Stream Console**, which dynamically displays every SQL query executed by the application, including millisecond execution timing!
> 
> ---
> 
> ### Live Demonstration: Complete 4-Step CRUD Lifecycle
> 
> Watch closely as we demonstrate the four fundamental database operations on **Financial Records**:
> 
> #### 1. INSERT (Create Record):
> - We click **`➕ Add Record (INSERT)`**.
> - A modal dialog appears. We select Type: `EXPENSE`, Category: `Cloud Infrastructure`, Amount: `$540.00`, Account: `CHK-100102`, and Memo: `Oracle Free Docker Compute Node`.
> - We click **Save**.
> - The application executes a parameterized JDBC INSERT:
>   ```sql
>   INSERT INTO financial_records 
>   (record_type, category, amount, account_number, description, record_date) 
>   VALUES (?, ?, ?, ?, ?, ?)
>   ```
> - Oracle Database assigns a new Sequence Primary Key (`id = 8`). Notice the bottom SQL console confirms: `Execution time: 2.1 ms | Affected Rows: 1`.
> 
> #### 2. SELECT (Read & Display):
> - The JavaFX `TableView` immediately refreshes via:
>   ```sql
>   SELECT id, record_type, category, amount, account_number, description, record_date 
>   FROM financial_records ORDER BY id DESC
>   ```
> - The newly inserted record appears at the top of the table. Simultaneously, the Overview KPI cards recalculate Total Expenses and Net Cash Flow in real time.
> 
> #### 3. UPDATE (Edit Record):
> - We select our new Record #8 and click **`✏️ Edit Record (UPDATE)`**.
> - We adjust the amount from `$540.00` to `$620.00` and append `[High Memory]` to the description.
> - We click **Save Changes**. The system executes:
>   ```sql
>   UPDATE financial_records 
>   SET record_type = ?, category = ?, amount = ?, account_number = ?, description = ?, record_date = ? 
>   WHERE id = ?
>   ```
> - The row updates instantly in the grid, and our `Budget Tracker` tab immediately updates its spending gauge for `Cloud Infrastructure`.
> 
> #### 4. DELETE (Remove Record):
> - Finally, we select Record #8 and click **`🗑️ Delete Record (DELETE)`**.
> - A confirmation dialog verifies our intent. Upon clicking 'Yes', the system executes:
>   ```sql
>   DELETE FROM financial_records WHERE id = ?
>   ```
> - The row is purged from Oracle Database. A subsequent query confirms that 0 rows exist for that ID, and our KPI cards return to their original baseline balance.
> 
> ---
> 
> ### Live Demonstration: Inter-Account Transfer via PL/SQL Package
> - To demonstrate our advanced DBMS integration, we click **`💸 New Transfer`**.
> - We transfer **$500.00** from Checking Account `CHK-100102` to Savings Account `SAV-100101`.
> - When we submit, our system invokes Oracle's PL/SQL package procedure:
>   ```sql
>   BEGIN PKG_BANKING_OPERATIONS.TRANSFER_FUNDS(?, ?, ?, ?); END;
>   ```
> - Both accounts are updated atomically, two double-entry ledger records are created, and `trg_audit_tx` writes an audit trail.
> 
> ### Project Summary:
> In conclusion, FinCore achieves 100% of capstone objectives:
> - **OOP Excellence:** Encapsulation, Abstract Classes, Polymorphism, Repository & Observer patterns.
> - **Enterprise DBMS:** Oracle Database in Docker, PL/SQL packages, business triggers, relational views, and ACID transactions.
> - **Modern User Experience:** Reactive JavaFX 21 desktop application with real-time SQL execution streaming.
> - **Complete CRUD:** Thoroughly verified INSERT, SELECT, UPDATE, and DELETE operations.
> - **Windows CMD Ready:** Seamless execution on Windows with zero setup via batch scripts.
> 
> Thank you for your time. We are now eager to answer questions from the evaluation panel!"

---

## 🎯 Evaluator Q&A Preparation & Technical Defense

### 1. How do you run the application during the evaluation?

```cmd
:: Method A: Pure Maven Execution (Standard & Cross-Platform)
mvn javafx:run        :: 1. Launch JavaFX 21 GUI directly connected to Oracle DB (or simply 'mvn')
mvn exec:java -Dexec.args="--demo" :: 2. Run automated 8-step Capstone Demo in terminal
mvn exec:java -Dexec.args="--cli"  :: 3. Run interactive console menu in CMD
mvn test              :: 4. Run JUnit 5 test suite (14/14 tests passing)

:: Method B: Windows CMD 1-Click All-In-One Launcher
run.bat               :: Auto-starts Docker, waits for Oracle FREEPDB1, launches GUI
run.bat --demo        :: Runs automated demonstration
run.bat --cli         :: Runs interactive terminal menu
run.bat --test        :: Runs test suite

# On Linux / macOS:
./run.sh --gui        # Launch JavaFX GUI (auto-starts Docker & Oracle)
./run.sh --demo       # Run automated verification showcase
```

### 2. High-Frequency Technical Questions & Answers:

**Q1: How does your application connect to Oracle Database running in Docker?**  
> *Answer:* We run the official `gvenzl/oracle-free:23-slim` image in Docker, exposing port `1521`. In `db.properties`, we connect via the thin JDBC URL `jdbc:oracle:thin:@localhost:1521/FREEPDB1` using Oracle's official `ojdbc11` driver (version `23.26.3.0.0`). The system is strictly configured for Oracle Database (`oracle.fallback.sqlite=false`), and `DatabaseManager` includes a smart retry mechanism that waits for the Oracle Pluggable Database `FREEPDB1` to complete service registration upon startup.

**Q2: What is the purpose of the PL/SQL package `PKG_BANKING_OPERATIONS`?**  
> *Answer:* Moving critical banking operations like `TRANSFER_FUNDS` into a PL/SQL package reduces network round-trips between Java and the database, enforces business rules inside the database engine, and guarantees atomicity at the server level.

**Q3: How do your database triggers work?**  
> *Answer:* `trg_audit_tx` is an `AFTER INSERT` trigger on `transactions` that automatically logs transaction metadata to `audit_logs`. `trg_check_budget_alert` is a `BEFORE INSERT` trigger on `financial_records` that checks if an expense exceeds category thresholds and logs an alert.

**Q4: How did you implement Polymorphism in your Java classes?**  
> *Answer:* The abstract base class `Account` defines `canWithdraw(double amount)` and `calculateMonthlyInterestOrFee()`. `SavingsAccount` overrides them to enforce a minimum balance rule and calculate APR interest. `CheckingAccount` overrides them to support overdraft credit limits and deduct maintenance fees. At runtime, the JVM uses dynamic method dispatch to execute the appropriate method.

**Q5: How do you prevent SQL Injection across all CRUD operations?**  
> *Answer:* Every database query in FinCore uses JDBC `PreparedStatement` with parameterized placeholders (`?`). No raw user input is ever concatenated into SQL strings.
