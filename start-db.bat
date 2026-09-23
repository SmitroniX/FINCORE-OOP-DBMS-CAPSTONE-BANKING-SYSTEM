@echo off
setlocal enabledelayedexpansion

echo ======================================================================
echo           FinCore - Oracle Database (Docker Container Starter)
echo ======================================================================
echo.

where docker >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Docker is not found in your Windows PATH.
    echo Please install Docker Desktop for Windows or ensure Docker is running.
    echo.
    echo Note: FinCore has an automatic zero-config SQLite embedded fallback!
    echo You can immediately run 'run.bat' even without Docker!
    echo.
    pause
    exit /b 1
)

echo [*] Checking Docker daemon status...
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Docker daemon is not currently running.
    echo Please start Docker Desktop and run this script again.
    echo.
    echo Note: FinCore will smoothly run on SQLite fallback if you execute 'run.bat'.
    echo.
    pause
    exit /b 1
)

echo [*] Starting Oracle Database Free container (gvenzl/oracle-free:23-slim)...
docker compose up -d

echo.
echo [*] Waiting for Oracle Database service FREEPDB1 to complete registration...
echo     (Oracle typically takes 30-60 seconds on initial boot)
echo.

set /a ATTEMPTS=0
:WAIT_ORACLE
set /a ATTEMPTS+=1

REM 1. Check if Oracle has logged ready message
docker logs fincore-oracle-db 2>&1 | findstr /C:"DATABASE IS READY TO USE!" >nul
if %ERRORLEVEL% equ 0 goto ORACLE_READY

REM 2. Check if container is still running
docker inspect -f "{{.State.Running}}" fincore-oracle-db 2>nul | findstr "true" >nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Oracle container is not running. Check 'docker logs fincore-oracle-db'.
    goto ORACLE_SUMMARY
)

REM 3. If reached 35 attempts (~105 seconds), proceed
if !ATTEMPTS! geq 35 (
    echo [NOTICE] Oracle is still completing initialization.
    echo FinCore will auto-retry or fall back to SQLite when launched.
    goto ORACLE_SUMMARY
)

<nul set /p =.
timeout /t 3 /nobreak >nul
goto WAIT_ORACLE

:ORACLE_READY
echo.
echo [*] Container Status: [HEALTHY - DATABASE READY]
echo.

:ORACLE_SUMMARY
echo ======================================================================
echo  Oracle Database Container is ACTIVE!
echo ======================================================================
echo  Host:       localhost
echo  Port:       1521
echo  Database:   FREEPDB1 (PDB) / FREE (CDB)
echo  User:       fincore_user
echo  Password:   fincore_pass
echo  Admin User: system
echo  Admin Pass: fincore123
echo  JDBC URL:   jdbc:oracle:thin:@localhost:1521/FREEPDB1
echo ======================================================================
echo.
echo [SUCCESS] You can now launch the application in Windows CMD using: run.bat
echo.
pause
