# 🎤 FinCore Capstone Project: Group Presentation & Explanation Script

> **Team Size:** 4 Presenters  
> **Total Duration:** 10 – 12 Minutes (~2.5 to 3 minutes per speaker)  
> **Project Name:** FinCore - Enterprise OOP & DBMS Banking System  
> **Repository:** [https://github.com/SmitroniX/FINCORE-OOP-DBMS-CAPSTONE-BANKING-SYSTEM](https://github.com/SmitroniX/FINCORE-OOP-DBMS-CAPSTONE-BANKING-SYSTEM)

---

## 👥 Speaker Roles Overview

| Speaker | Name / Role | Core Topic Covered | Key Slides / Screen Demonstrations |
|---|---|---|---|
| **Speaker 1** | Team Lead / System Architect | Project Introduction, Motivation, Architecture & Core Abstractions | Slide 1–3: Problem statement, System Architecture Diagram |
| **Speaker 2** | OOP & Domain Specialist | OOP Principles: Encapsulation, Inheritance & Polymorphic Banking Rules | Slide 4–5: Class hierarchy (`Account`, `SavingsAccount`, `CheckingAccount`), Code walkthrough |
| **Speaker 3** | DBMS & Database Engineer | Relational Schema, Integrity Constraints & ACID Transaction Atomicity | Slide 6–7: ER Diagram, Foreign Keys, Transfer commit & rollback code |
| **Speaker 4** | Analytics & QA Lead | Relational SQL Analytics (JOINs & Aggregations), Test Suite & Live Demo | Slide 8–10: Terminal Live Demo (`./run.sh --demo`), JUnit results & Conclusion |

---

## 🗣️ Speaker 1: Introduction, Motivation & System Architecture
**⏱️ Target Time:** 2.5 Minutes  
**🎬 Cue:** Present the Title Slide & System Architecture Diagram

### 🎙️ Dialogue / Spoken Script:

> "Good morning, respected professors, evaluators, and colleagues.
> 
> Today, our team is proud to present **FinCore**, an enterprise-grade banking engine developed as our Capstone Project. 
> 
> When designing financial software, two foundational pillars determine whether a system succeeds or fails:
> 1. **Robust Software Architecture**, powered by Object-Oriented Programming (OOP) to model complex real-world financial entities.
> 2. **Data Integrity & Consistency**, powered by a Relational Database Management System (DBMS) capable of enforcing strict ACID guarantees.
> 
> In traditional academic projects, OOP code and DBMS operations are often disconnected—data is either stored in flat files or treated with primitive SQL scripts that ignore race conditions and data anomalies. 
> 
> In **FinCore**, we bridged this gap completely. We built a 3-tier enterprise architecture consisting of:
> - **The Presentation Layer:** An interactive, ANSI-colored terminal CLI and an automated demo runner.
> - **The Business Service Layer:** Where business logic, domain validation, and ACID transaction boundaries are enforced.
> - **The Data Access (Repository) Layer:** Featuring generic CRUD abstractions and parameterized JDBC implementations that eliminate SQL injection.
> - **The Relational Storage Engine:** Supporting embedded, zero-configuration SQLite for development and instant portability, as well as production-ready MySQL through standard properties configuration.
> 
> At the core of our user domain, we implemented **OOP Abstraction and Encapsulation**. The abstract base class `User` encapsulates common credentials, contact information, and audit metadata with strict input validation. It is extended into distinct user roles: retail `Customer`s and banking `Admin`s, enforcing role segregation.
> 
> Now, I will hand over to **Speaker 2**, who will walk us through how we utilized Inheritance, Polymorphism, and Domain Modeling."

---

## 🗣️ Speaker 2: OOP Principles (Encapsulation, Inheritance & Polymorphism)
**⏱️ Target Time:** 2.5 Minutes  
**🎬 Cue:** Switch to Class Hierarchy Diagram & Code Snippets of `Account`, `SavingsAccount`, and `CheckingAccount`

### 🎙️ Dialogue / Spoken Script:

> "Thank you, Speaker 1.
> 
> In FinCore, Object-Oriented Programming is not just theory—it actively governs how money moves and how financial rules are enforced.
> 
> Let's look at our account hierarchy:
> 
> 1. **Encapsulation:**
>    In our `Account` base class, all sensitive fields—such as account balance, customer ID, and account status—are strictly `private`. The balance cannot be modified arbitrarily from outside. It can only change through the synchronized `deposit()` and `withdraw()` domain methods, which enforce critical invariants such as rejecting negative amounts and verifying account status.
> 
> 2. **Inheritance:**
>    We established an abstract class `Account`, and derived two specialized account types:
>    - `SavingsAccount`, which models wealth accumulation.
>    - `CheckingAccount`, which models everyday liquidity and transactional overdrafts.
> 
> 3. **Polymorphism (Runtime Dynamic Binding):**
>    We declared two key abstract polymorphic contracts in `Account`:
>    - `canWithdraw(double amount)`
>    - `calculateMonthlyInterestOrFee()`
> 
>    Notice how dynamic dispatch behaves differently for each subclass:
>    - In **`SavingsAccount`**, the withdrawal logic enforces a mandatory **Minimum Balance** (for example, $50.00). If a withdrawal would drop the account below this threshold, it is rejected. When month-end batch processing occurs, `calculateMonthlyInterestOrFee()` returns a **positive interest credit** calculated from the account's Annual Percentage Rate (APR).
>    - In contrast, in **`CheckingAccount`**, `canWithdraw()` permits the customer to draw funds beyond their balance up to an authorized **Overdraft Credit Limit** (e.g., $1,000.00). In month-end processing, it returns a **negative monthly maintenance fee**.
> 
> 4. **Polymorphic Hydration from Database:**
>    In our JDBC repository layer, when records are read from the database, our factory inspects the `account_type` column and polymorphically instantiates either a `SavingsAccount` or `CheckingAccount` object. Business services operate solely against the generic `Account` reference without needing to know concrete types.
> 
> Next, **Speaker 3** will explain how the relational database schema and DBMS ACID transactions safeguard financial integrity."

---

## 🗣️ Speaker 3: DBMS Architecture, Schema Design & ACID Transactions
**⏱️ Target Time:** 3 Minutes  
**🎬 Cue:** Switch to Entity-Relationship (ER) Diagram & `BankingService.transferFunds()` Code

### 🎙️ Dialogue / Spoken Script:

> "Thank you, Speaker 2.
> 
> Now let's examine the Database Management System (DBMS) layer of FinCore.
> 
> Financial databases require uncompromising integrity. Our relational schema is designed with 4 normalized tables:
> 1. `customers`: Stores user profile data with `UNIQUE` email and customer code constraints.
> 2. `accounts`: Tied to `customers` via a Foreign Key with `ON DELETE CASCADE`. It incorporates database-level `CHECK` constraints to guarantee valid account types and enforce maximum overdraft boundaries.
> 3. `transactions`: An immutable double-entry ledger recording deposits, withdrawals, and inter-account transfers.
> 4. `audit_logs`: A regulatory audit trail capturing who performed every action, the timestamp, and the before-and-after entity details.
> 
> **ACID Transactions in Action:**
> The greatest test of a banking system is an inter-account fund transfer. If money is deducted from Account A, but a failure occurs before it is credited to Account B, money would vanish into thin air.
> 
> In FinCore, we solve this using explicit DBMS transaction management in `BankingService`:
> 1. We disable auto-commit via `connection.setAutoCommit(false)`, establishing a transaction boundary.
> 2. We retrieve both accounts and execute polymorphic overdraft and balance checks.
> 3. We execute both updates—debiting the source account and crediting the destination account.
> 4. We record two matching ledger entries in the `transactions` table (a `TRANSFER_OUT` and a `TRANSFER_IN`).
> 5. We record an audit log entry.
> 6. Only when all operations succeed do we issue `connection.commit()`, ensuring **Atomicity and Durability**.
> 
> **Rollback Protection:**
> If an exception occurs at any point—whether due to insufficient funds, an inactive account, or a database glitch—the entire operation is trapped in a `catch` block that immediately executes `connection.rollback()`.
> 
> In our automated tests, we deliberately attempted illegal overdraft transfers of $50,000. The DBMS rolled back instantly: zero cents were lost, and balances remained 100% consistent.
> 
> Additionally, 100% of our SQL statements utilize parameterized `PreparedStatement`s, ensuring complete immunity to SQL Injection attacks.
> 
> I will now turn the stage to **Speaker 4** to present our analytical SQL queries, test suite, and live demonstration."

---

## 🗣️ Speaker 4: Relational Analytics, Verification & Live Demo
**⏱️ Target Time:** 2.5 – 3 Minutes  
**🎬 Cue:** Switch to Terminal Screen (`./run.sh --demo` or `./run.sh --test`)

### 🎙️ Dialogue / Spoken Script:

> "Thank you, Speaker 3.
> 
> Beyond transactions, modern banking requires deep relational business intelligence.
> 
> **Advanced SQL Joins and Aggregations:**
> In `ReportService`, we implemented complex multi-table SQL queries utilizing `LEFT JOIN`, `SUM`, `COUNT`, and `GROUP BY`. 
> 
> Rather than naive multi-table joins that suffer from Cartesian product fan-out, our query uses pre-aggregated subqueries to calculate:
> - Total active accounts per customer.
> - Exact consolidated net balance across savings and checking portfolios.
> - Total historical transactions.
> - Global bank liquidity, calculating total deposits and breakdown pools between savings and checking.
> 
> **Testing and Verification:**
> To guarantee production reliability, we implemented a comprehensive **JUnit 5** test suite:
> - Unit tests verifying polymorphic minimum balance and overdraft calculations.
> - Integration tests verifying that ACID transfers commit correctly and rollback cleanly under failure.
> - Repository tests verifying CRUD persistence and relational JOIN reporting against an isolated test database.
> 
> *(Speaker 4 switches to terminal and runs `./run.sh --demo`)*
> 
> As you can see on screen:
> - Step 1 verifies runtime polymorphism.
> - Step 2 creates relational records in the database.
> - Step 3 executes an ACID funds transfer and commits atomically.
> - Step 4 simulates an invalid transfer: the system catches the domain exception and executes a clean database rollback.
> - Step 5 runs batch interest accrual for savings and fee deduction for checking.
> - Step 6 generates our live Customer Portfolio Report and Liquidity Metrics.
> 
> **Conclusion:**
> FinCore proves how Object-Oriented software engineering and Database Management Systems complement one another to build robust, scalable, and secure financial software.
> 
> The entire project is packaged with Maven, fully documented on GitHub, and ready for deployment.
> 
> Thank you, and we now welcome any questions from the panel!"

---

## 🎯 Evaluator Q&A Cheat Sheet (Prepared Answers for the Team)

### Q1: "Why did you choose SQLite by default instead of MySQL or PostgreSQL?"
> **Answer (Speaker 1 or 3):**  
> *"We designed the system with database portability in mind. SQLite is built into our configuration as the zero-dependency embedded default, meaning anyone evaluating the project can clone it and run `./run.sh` without configuring local database daemons or credentials. However, our `DatabaseConfig` and `DatabaseManager` follow the Factory pattern—by changing a single line in `db.properties` (`db.type=mysql`), the application seamlessly connects to an enterprise MySQL server with InnoDB engine support."*

### Q2: "How does your code prevent SQL Injection?"
> **Answer (Speaker 3):**  
> *"We avoid raw string concatenation in SQL queries. Every single query across our JDBC repositories uses `PreparedStatement` with parameterized placeholders (`?`). The JDBC driver handles type checking and escaping at the protocol level, making SQL injection impossible."*

### Q3: "How is Polymorphism demonstrated in your database integration?"
> **Answer (Speaker 2):**  
> *"When hydrating accounts from the `accounts` table in `JdbcAccountRepository`, our code checks the `account_type` discriminator column. If it is `SAVINGS`, it instantiates a `SavingsAccount` with its specific APR; if it is `CHECKING`, it instantiates a `CheckingAccount` with its overdraft limit. High-level services interact solely with the base `Account` interface, invoking polymorphic methods like `canWithdraw()` and `calculateMonthlyInterestOrFee()` at runtime."*

### Q4: "What happens if the power cuts or a server crashes halfway through a transfer?"
> **Answer (Speaker 3):**  
> *"Because we set `connection.setAutoCommit(false)`, the DBMS logs intermediate updates in its Write-Ahead Log (WAL/journal). If a crash occurs before `connection.commit()` is issued, the database automatically performs crash recovery upon reboot, rolling back uncommitted changes and guaranteeing that the database returns to a consistent, uncorrupted state."*
