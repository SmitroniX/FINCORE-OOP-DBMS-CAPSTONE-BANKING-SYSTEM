@echo off
setlocal enabledelayedexpansion
title FinCore Enterprise Banking System [Java 21 ^| JavaFX 21 ^| Oracle in Docker]

REM ====================================================================
REM FinCore: Enterprise OOP & DBMS Banking System
REM Windows CMD Universal Launcher (Java 21, JavaFX 21, Oracle Database)
REM ====================================================================

echo ====================================================================
echo   FinCore: Enterprise Banking ^& Finance System (Windows CMD)
echo   Stack: Java 21 ^| JavaFX 21 ^| Oracle DB in Docker ^| PL/SQL
echo ====================================================================
echo.

REM 1. Verify Java 21+ is installed and reachable in PATH
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java runtime is not detected in your Windows PATH.
    echo Please install Java 21 from https://adoptium.net/ or Oracle JDK 21.
    echo Ensure JAVA_HOME and PATH are configured.
    echo.
    pause
    exit /b 1
)

REM 2. Determine project root
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

REM 3. Handle Command-line Arguments
if "%~1"=="--test" goto RUN_TEST
if "%~1"=="-t" goto RUN_TEST
if "%~1"=="--demo" goto RUN_DEMO
if "%~1"=="-d" goto RUN_DEMO
if "%~1"=="--cli" goto RUN_CLI
if "%~1"=="-c" goto RUN_CLI
if "%~1"=="--build" goto RUN_BUILD
if "%~1"=="-b" goto RUN_BUILD

REM Default: Launch JavaFX 21 Desktop GUI via Maven
:RUN_GUI
echo [*] Launching FinCore JavaFX 21 Enterprise Finance Dashboard...
echo [*] Active DBMS Target : Oracle Database Free in Docker (Port 1521 / FREEPDB1)
echo [*] Embedded Fallback  : SQLite zero-config engine (if Oracle is offline)
echo.

where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    call mvn javafx:run
) else (
    set "JAR_FILE=%SCRIPT_DIR%target\oop-dbms-capstone-1.0.0-jar-with-dependencies.jar"
    if exist "!JAR_FILE!" (
        java -jar "!JAR_FILE!"
    ) else (
        echo [ERROR] Maven not found and JAR not pre-built. Please install Maven or build the project.
        pause
        exit /b 1
    )
)
goto END

:RUN_DEMO
echo [*] Running Capstone Automated Demonstration & Verification Suite...
echo.
call mvn exec:java -Dexec.args="--demo"
goto END

:RUN_CLI
echo [*] Starting Interactive Console Menu in Windows CMD...
echo.
call mvn exec:java -Dexec.args="--cli"
goto END

:RUN_TEST
echo [*] Running Automated JUnit 5 Unit & Integration Tests...
call mvn test
goto END

:RUN_BUILD
echo [*] Packaging Full Executable JAR with Dependencies...
call mvn clean package -DskipTests
goto END

:END
echo.
pause
endlocal
