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
echo [*] Container status:
docker compose ps

echo.
echo ======================================================================
echo  Oracle Database Container is starting up!
echo ======================================================================
echo  Host:       localhost
echo  Port:       1521
echo  Database:   FREEPDB1 (or xe)
echo  User:       fincore_user
echo  Password:   fincore_pass
echo  Admin User: system
echo  Admin Pass: fincore123
echo  JDBC URL:   jdbc:oracle:thin:@localhost:1521/FREEPDB1
echo ======================================================================
echo.
echo Note: Initializing Oracle container typically takes 30-60 seconds on first run.
echo You can now launch the JavaFX application in CMD using: run.bat
echo.
pause
