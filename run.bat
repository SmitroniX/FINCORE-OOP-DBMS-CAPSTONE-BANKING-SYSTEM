@echo off
setlocal enabledelayedexpansion
title FinCore Banking & Finance System

REM ====================================================================
REM FinCore: Enterprise OOP & DBMS Banking System
REM Windows Universal Launcher (Windows 7 / 8 / 10 / 11 / Server)
REM ====================================================================

echo ====================================================================
echo   FinCore: Enterprise OOP & DBMS Banking System (Windows)
echo ====================================================================
echo.

REM 1. Verify Java 17+ is installed and reachable
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java runtime is not detected in your PATH.
    echo Please install Java 17 or higher from https://adoptium.net/ or Oracle JDK.
    echo Ensure JAVA_HOME and PATH are configured.
    echo.
    pause
    exit /b 1
)

REM 2. Determine project root and fat JAR location
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

set "JAR_FILE=%SCRIPT_DIR%target\oop-dbms-capstone-1.0.0-jar-with-dependencies.jar"

if not exist "%JAR_FILE%" (
    if exist "%SCRIPT_DIR%oop-dbms-capstone-1.0.0-jar-with-dependencies.jar" (
        set "JAR_FILE=%SCRIPT_DIR%oop-dbms-capstone-1.0.0-jar-with-dependencies.jar"
    ) else (
        echo [Bootstrap] Pre-built JAR not found. Checking for Maven...
        where mvn >nul 2>&1
        if %ERRORLEVEL% EQU 0 (
            echo [Build] Building project fat JAR using Maven...
            call mvn clean package -DskipTests
        ) else (
            echo [ERROR] Target executable JAR not found at:
            echo   %JAR_FILE%
            echo Please run 'mvn clean package' or compile the project first.
            echo.
            pause
            exit /b 1
        )
    )
)

REM 3. Handle Command-line Arguments
if "%~1"=="--test" goto RUN_TEST
if "%~1"=="-t" goto RUN_TEST
if "%~1"=="--demo" goto RUN_DEMO
if "%~1"=="-d" goto RUN_DEMO
if "%~1"=="--cli" goto RUN_CLI
if "%~1"=="-c" goto RUN_CLI
if "%~1"=="--build" goto RUN_BUILD
if "%~1"=="-b" goto RUN_BUILD

REM Default mode: Launch Java Swing GUI
:RUN_GUI
echo [Launch] Launching FinCore Java Swing Graphical Dashboard...
echo [Info]   Default DBMS Engine: Oracle 10g XE (fallback: SQLite)
echo.

where javaw >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    start "FinCore Banking System" javaw -jar "%JAR_FILE%" --gui
) else (
    java -jar "%JAR_FILE%" --gui
)
exit /b 0

:RUN_DEMO
echo [Launch] Running Capstone Automated Demonstration Runner...
echo.
java -jar "%JAR_FILE%" --demo
goto END

:RUN_CLI
echo [Launch] Starting Interactive Console Menu...
echo.
java -jar "%JAR_FILE%" --cli
goto END

:RUN_TEST
echo [Test] Running Maven Test Suite...
call mvn test
goto END

:RUN_BUILD
echo [Build] Packaging Fat JAR...
call mvn clean package -DskipTests
goto END

:END
endlocal
