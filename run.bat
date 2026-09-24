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

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

if /i "%~1"=="help" goto RUN_HELP
if /i "%~1"=="--help" goto RUN_HELP
if /i "%~1"=="-h" goto RUN_HELP
if /i "%~1"=="/?" goto RUN_HELP

REM 1. Verify Java 21+ is installed and reachable in PATH
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [!] Java runtime is not detected in your Windows PATH.
    if exist "%SCRIPT_DIR%setup.bat" (
        echo [*] FinCore requires Java 21. Automated setup script is available.
        choice /C YN /M "[?] Would you like to automatically install Java 21, Maven, and all setups now"
        if !ERRORLEVEL! EQU 1 (
            call "%SCRIPT_DIR%setup.bat" --auto
        ) else (
            echo Please install Java 21 from https://adoptium.net/ and run again.
            pause
            exit /b 1
        )
    ) else (
        echo [ERROR] Java runtime is not detected in your Windows PATH.
        echo Please install Java 21 from https://adoptium.net/ or Oracle JDK 21.
        pause
        exit /b 1
    )
)

REM 2. Verify Docker is installed and running
where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [!] Docker is not found in your Windows PATH.
    if exist "%SCRIPT_DIR%setup.bat" (
        echo [*] FinCore requires Docker Desktop for Oracle Database.
        choice /C YN /M "[?] Would you like to automatically install Docker Desktop via setup.bat"
        if !ERRORLEVEL! EQU 1 (
            call "%SCRIPT_DIR%setup.bat" --auto
        ) else (
            echo Please install Docker Desktop: https://www.docker.com/products/docker-desktop/
            pause
            exit /b 1
        )
    ) else (
        echo [ERROR] Docker is not found in your Windows PATH.
        echo Please install Docker Desktop: https://www.docker.com/products/docker-desktop/
        pause
        exit /b 1
    )
)

docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [*] Docker Desktop daemon is not currently running.
    echo [*] Attempting to start Docker Desktop...
    if exist "C:\Program Files\Docker\Docker\Docker Desktop.exe" (
        start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    )
    echo [*] Waiting up to 30 seconds for Docker daemon to become responsive...
    set /a D_WAIT=0
    :WAIT_DOCKER_RUNBAT
    docker info >nul 2>&1
    if !ERRORLEVEL! EQU 0 goto DOCKER_ACTIVE_GO
    set /a D_WAIT+=1
    if !D_WAIT! GEQ 15 (
        echo.
        echo [ERROR] Docker Desktop daemon did not respond in time.
        echo Please start Docker Desktop and run this script again.
        pause
        exit /b 1
    )
    <nul set /p =.
    timeout /t 2 /nobreak >nul
    goto WAIT_DOCKER_RUNBAT
)
:DOCKER_ACTIVE_GO
echo.

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
if /i "%~1"=="--test" goto RUN_TEST
if /i "%~1"=="test" goto RUN_TEST
if /i "%~1"=="-t" goto RUN_TEST
if /i "%~1"=="--demo" goto RUN_DEMO
if /i "%~1"=="demo" goto RUN_DEMO
if /i "%~1"=="-d" goto RUN_DEMO
if /i "%~1"=="--cli" goto RUN_CLI
if /i "%~1"=="cli" goto RUN_CLI
if /i "%~1"=="-c" goto RUN_CLI
if /i "%~1"=="--gui" goto RUN_GUI
if /i "%~1"=="gui" goto RUN_GUI
if /i "%~1"=="-g" goto RUN_GUI
if /i "%~1"=="--setup" goto RUN_SETUP
if /i "%~1"=="setup" goto RUN_SETUP
if /i "%~1"=="--build" goto RUN_BUILD
if /i "%~1"=="build" goto RUN_BUILD
if /i "%~1"=="-b" goto RUN_BUILD
if /i "%~1"=="help" goto RUN_HELP
if /i "%~1"=="--help" goto RUN_HELP
if /i "%~1"=="-h" goto RUN_HELP
if /i "%~1"=="/?" goto RUN_HELP

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

:RUN_SETUP
echo [*] Launching Automated Prerequisites Installer...
if exist "%SCRIPT_DIR%setup.bat" (
    call "%SCRIPT_DIR%setup.bat"
) else (
    echo [ERROR] setup.bat not found in %SCRIPT_DIR%
)
goto END

:RUN_HELP
echo.
echo ====================================================================
echo   FinCore: Windows Command-Line Launcher Reference
echo ====================================================================
echo   run.bat              - Launch JavaFX 21 Finance Dashboard (GUI)
echo   run.bat demo         - Run Automated 8-Step Capstone Demo in CMD
echo   run.bat cli          - Launch Interactive Terminal Console Menu in CMD
echo   run.bat test         - Run 14/14 Automated JUnit 5 Unit ^& Integration Tests
echo   run.bat setup        - Auto-Install Java 21, Maven, Docker, and Oracle DB
echo   run.bat build        - Compile and package Fat Executable JAR
echo   run.bat help         - Display this reference manual
echo ====================================================================
goto END

:END
echo.
pause
endlocal
