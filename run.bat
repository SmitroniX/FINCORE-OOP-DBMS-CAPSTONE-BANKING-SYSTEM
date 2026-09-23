@echo off
setlocal enabledelayedexpansion
title FinCore Enterprise Banking System [Java 21 ^| JavaFX 21 ^| Oracle Database in Docker]

REM ====================================================================
REM FinCore: Enterprise OOP & DBMS Banking System
REM Windows CMD All-In-One Launcher (Java 21, JavaFX 21, Oracle Database)
REM ====================================================================

echo ====================================================================
echo   FinCore: Enterprise Banking ^& Finance Management System
echo   Stack: Java 21 ^| JavaFX 21 ^| Oracle Database in Docker ^| PL/SQL
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

REM 3. Verify Docker is installed and running
where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not found in your Windows PATH.
    echo FinCore is strictly configured for Oracle Database in Docker.
    echo Please install Docker Desktop: https://www.docker.com/products/docker-desktop/
    echo.
    pause
    exit /b 1
)

docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker Desktop daemon is not currently running.
    echo Please start Docker Desktop and run this script again.
    echo.
    pause
    exit /b 1
)

REM 4. Check Oracle Container status and start if needed
echo [*] Checking Oracle Database container status...
docker inspect -f "{{.State.Running}}" fincore-oracle-db 2>nul | findstr "true" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [*] Starting Oracle Database Free container (fincore-oracle-db on port 1521)...
    docker compose up -d
) else (
    echo [*] Oracle Database container is active.
)

REM 5. Verify Oracle Database is 100% ready and service FREEPDB1 is registered
echo [*] Verifying Oracle Database service (FREEPDB1) is ready...
set /a ORACLE_ATTEMPTS=0
:CHECK_ORACLE_LOOP
docker logs fincore-oracle-db 2>&1 | findstr /C:"DATABASE IS READY TO USE!" >nul
if %ERRORLEVEL% equ 0 goto ORACLE_READY_GO

docker inspect -f "{{.State.Running}}" fincore-oracle-db 2>nul | findstr "true" >nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Oracle container exited unexpectedly.
    echo Please run 'docker logs fincore-oracle-db' for details.
    pause
    exit /b 1
)

set /a ORACLE_ATTEMPTS+=1
if !ORACLE_ATTEMPTS! geq 45 (
    echo.
    echo [NOTICE] Oracle container is completing its final initialization...
    goto ORACLE_READY_GO
)

<nul set /p =.
timeout /t 2 /nobreak >nul
goto CHECK_ORACLE_LOOP

:ORACLE_READY_GO
echo.
echo [SUCCESS] Oracle Database is CONNECTED and READY on port 1521 (Service: FREEPDB1)!
echo.

REM 6. Handle Command-line Arguments
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
echo [*] Target DBMS : Oracle Database (Port 1521 / FREEPDB1)
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
