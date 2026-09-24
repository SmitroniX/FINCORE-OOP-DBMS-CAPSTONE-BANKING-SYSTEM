# Eclipse IDE Setup & Run Guide: FinCore Enterprise Banking System

This project is fully configured for **Eclipse IDE for Java Developers** (2023-09, 2023-12, 2024-03, 2024-06 or newer) with pre-packaged Eclipse metadata (`.project`, `.classpath`, `.settings/`) and ready-to-use Eclipse Run Configurations (`.launch` files).

---

## 📋 Prerequisites

1. **Eclipse IDE for Java Developers** (with m2e plugin, which is included by default).
2. **Java Development Kit (JDK) 21 LTS** installed (e.g. Eclipse Adoptium Temurin 21 or BellSoft Liberica 21).
3. **Docker Desktop** installed and running (for Oracle Database container `fincore-oracle-db` on port `1521`).

---

## 🚀 How to Import into Eclipse

### Method 1: Import as Existing Maven Project (Recommended)

1. Launch **Eclipse IDE**.
2. Select your preferred workspace directory.
3. Click **File** -> **Import...** from the top menu.
4. Expand the **Maven** folder, select **Existing Maven Projects**, and click **Next >**.
5. Click **Browse...** next to *Root Directory*, and select the `oop-dbms-capstone` folder.
6. Eclipse will discover `pom.xml`. Ensure the checkbox next to `/pom.xml` is checked.
7. Click **Finish**. Eclipse will import the project and resolve all dependencies (`javafx-controls`, `ojdbc11`, `junit-jupiter`, etc.) automatically.

---

### Method 2: Import as Existing Project (Zero-Config)

Because `.project` and `.classpath` are pre-configured:
1. Click **File** -> **Import...** -> **General** -> **Existing Projects into Workspace** -> **Next >**.
2. Click **Browse...** next to *Select root directory*, and choose the `oop-dbms-capstone` folder.
3. Eclipse will identify `oop-dbms-capstone`. Ensure it is checked.
4. Click **Finish**.

---

## ⚙️ Verify Java 21 in Eclipse

If Eclipse shows an error about an unbound JRE System Library:
1. Open **Window** -> **Preferences** (on macOS: **Eclipse** -> **Settings...**).
2. Navigate to **Java** -> **Installed JREs**.
3. Ensure a **JDK 21** entry is listed and checked:
   - If missing, click **Add...** -> **Standard VM** -> **Next >**.
   - Set *JRE home* to your JDK 21 installation path (e.g., `C:\Program Files\Eclipse Adoptium\jdk-21...` or `/usr/lib/jvm/temurin-21...`).
   - Click **Finish**, check the JDK 21 box, and click **Apply and Close**.
4. Right-click the `oop-dbms-capstone` project in Project Explorer -> **Maven** -> **Update Project...** (or press `Alt + F5`) -> click **OK**.

---

## 🎯 Pre-Configured Eclipse Launch Configurations

The repository includes pre-built Eclipse `.launch` files in the project root. You do not need to configure VM arguments, main classes, or parameters manually!

### 1. Launch the JavaFX 21 Desktop GUI Dashboard
- Right-click `FinCore-GUI.launch` in the Project Explorer -> **Run As** -> **FinCore-GUI**.
- *Or* click the Green Run button dropdown in Eclipse toolbar and select **FinCore-GUI**.
- Launches the JavaFX 21 dark-themed Banking Dashboard connected to Oracle Database.

### 2. Run the 8-Step Capstone Automated Demonstration
- Right-click `FinCore-Demo.launch` -> **Run As** -> **FinCore-Demo**.
- Executes all 8 verification steps (Oracle JDBC bootstrap, Authentication, KPI dashboard aggregation, 4-step CRUD, OOP polymorphism, ACID atomic fund transfer & rollback, and Multi-table SQL joins) in the **Eclipse Console** tab.

### 3. Run the Interactive Banking Terminal (CLI)
- Right-click `FinCore-CLI.launch` -> **Run As** -> **FinCore-CLI**.
- Runs the interactive console menu in the **Eclipse Console** tab, where you can type commands to create accounts, perform transfers, manage customers, and view reports.

### 4. Run 14/14 JUnit 5 Automated Tests
- Right-click `FinCore-Tests.launch` -> **Run As** -> **FinCore-Tests**.
- Runs all unit and integration tests inside Eclipse's native **JUnit view** (green bar guaranteed).

### 5. Run via Maven Plugin inside Eclipse
- Right-click `FinCore-Maven-JavaFX.launch` -> **Run As** -> **FinCore-Maven-JavaFX**.
- Executes Maven goal `javafx:run` via the m2e integration.

---

## 💡 Why JavaFX Runs Seamlessly in Eclipse

In Java 11+, JavaFX is decoupled from the JDK. Standard JavaFX applications often produce the following error in Eclipse:
```
Error: JavaFX runtime components are missing, and are required to run this application
```
FinCore resolves this automatically:
1. `com.fincore.Main` serves as the entrypoint without directly extending `javafx.application.Application`.
2. Inside `Main.main()`, `Application.launch(MainApp.class, args)` is invoked.
3. This allows the Eclipse Classpath Provider to load the JavaFX 21 libraries from Maven without requiring complex `--module-path` configurations.
4. For native access warning suppression on Java 21, the launch configurations pre-include:
   `--enable-native-access=javafx.graphics,ALL-UNNAMED`

---

## 🛠️ Troubleshooting

| Issue | Cause | Solution |
|---|---|---|
| **`ORA-12541` / Connection Refused** | Oracle Database container is not running. | Run `docker compose up -d` in terminal or double-click `start-db.bat` before launching in Eclipse. |
| **Project shows red exclamation mark `!`** | Unbound JRE 21. | Go to `Window -> Preferences -> Java -> Installed JREs`, add JDK 21, then right-click project -> `Maven -> Update Project`. |
| **JavaFX Window does not appear** | Headless display or missing graphic context. | Check Eclipse Console output. If running on a headless remote server, FinCore will automatically switch to Interactive Console Menu. On Windows/Linux desktop, the JavaFX window opens normally. |
