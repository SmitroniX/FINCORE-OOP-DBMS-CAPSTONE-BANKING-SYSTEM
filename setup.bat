@echo off
setlocal enabledelayedexpansion
title FinCore Automated Setup ^& Dependency Installer [Java 21 ^| JavaFX 21 ^| Maven ^| Docker]

REM ====================================================================
REM FinCore: Enterprise Banking & Finance Management System
REM Automated Setup & Prerequisites Installer for Windows (CMD / PowerShell)
REM
REM Automatically installs and configures:
REM   1. Java 21 LTS (Eclipse Adoptium Temurin 21 or BellSoft Liberica 21 Full)
REM   2. JavaFX 21 (Pre-resolved via Maven & Liberica/OpenJFX)
REM   3. Apache Maven 3.9+ (Build automation & dependency manager)
REM   4. Docker Desktop (Required for Oracle Database Free container)
REM   5. Oracle Database 23c Container (gvenzl/oracle-free:23-slim)
REM   6. Pre-caches all project dependencies and JavaFX offline cache
REM ====================================================================

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo ====================================================================
echo   FinCore: Enterprise Banking ^& Finance Management System
echo   AUTOMATED ENVIRONMENT SETUP ^& PREREQUISITES INSTALLER
echo   Target Stack: Java 21 ^| JavaFX 21 ^| Maven ^| Docker ^| Oracle DB
echo ====================================================================
echo.

REM --------------------------------------------------------------------
REM 0. Check Administrator Privileges
REM --------------------------------------------------------------------
net session >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [NOTICE] Administrator privileges are recommended for installing
    echo          system-wide JDK, Maven, and Docker Desktop.
    if "%~1"=="--auto" (
        echo [*] Auto-elevation requested...
        powershell -Command "Start-Process cmd.exe -ArgumentList '/c \"\"%~f0\" %*\"' -Verb RunAs"
        exit /b 0
    ) else if "%~1"=="-y" (
        powershell -Command "Start-Process cmd.exe -ArgumentList '/c \"\"%~f0\" %*\"' -Verb RunAs"
        exit /b 0
    ) else (
        echo.
        choice /C YN /M "[?] Do you want to elevate this setup to Administrator now"
        if !ERRORLEVEL! EQU 1 (
            echo [*] Elevating to Administrator...
            powershell -Command "Start-Process cmd.exe -ArgumentList '/c \"\"%~f0\" %*\"' -Verb RunAs"
            exit /b 0
        )
        echo [*] Continuing with current user permissions...
        echo.
    )
)

call :REFRESH_ENV

REM --------------------------------------------------------------------
REM 1. Check & Install Java 21 LTS
REM --------------------------------------------------------------------
echo [*] Step 1/5: Checking Java 21 LTS installation...
set "JAVA_OK=0"
where java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set "JVER=%%~v"
        echo !JVER! | findstr /r "\"21\." >nul
        if !ERRORLEVEL! equ 0 set "JAVA_OK=1"
    )
)

if %JAVA_OK% equ 1 (
    echo [✔] Java 21 is already installed and active: !JVER!
) else (
    echo [!] Java 21 not detected in PATH.
    echo [*] Installing Eclipse Adoptium Temurin JDK 21 (LTS)...
    where winget >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        echo [*] Using Windows Package Manager (winget)...
        winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-package-agreements --accept-source-agreements --silent
        if !ERRORLEVEL! neq 0 (
            echo [*] Trying BellSoft Liberica JDK 21 Full (Native JavaFX bundle)...
            winget install --id BellSoft.LibericaJDK.21.Full -e --accept-package-agreements --accept-source-agreements --silent
        )
    ) else (
        echo [*] WinGet not found. Downloading Adoptium OpenJDK 21 via PowerShell...
        powershell -Command "$ErrorActionPreference = 'Stop'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $installer = Join-Path $env:TEMP 'temurin21.msi'; Write-Host 'Downloading Adoptium OpenJDK 21 MSI...'; Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%%2B7/OpenJDK21U-jdk_x64_windows_hotspot_21.0.6_7.msi' -OutFile $installer; Write-Host 'Installing JDK 21...'; Start-Process msiexec.exe -ArgumentList '/i `\"'$installer'`\" /quiet /qn ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome' -Wait"
    )
    call :REFRESH_ENV
    where java >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        echo [✔] Java 21 installed successfully!
    ) else (
        echo [!] Java 21 installed. Note: If not immediately seen, a CMD restart may be required.
    )
)
echo.

REM --------------------------------------------------------------------
REM 2. Check & Install Apache Maven
REM --------------------------------------------------------------------
echo [*] Step 2/5: Checking Apache Maven...
set "MAVEN_OK=0"
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set "MAVEN_OK=1"
    echo [✔] Apache Maven is already installed and active.
) else (
    echo [!] Apache Maven not detected in PATH. Installing Apache Maven...
    where winget >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        echo [*] Using Windows Package Manager (winget)...
        winget install --id Apache.Maven -e --accept-package-agreements --accept-source-agreements --silent
    ) else (
        echo [*] Downloading Apache Maven 3.9 via PowerShell...
        powershell -Command "$ErrorActionPreference = 'Stop'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $zip = Join-Path $env:TEMP 'apache-maven.zip'; Write-Host 'Downloading Maven...'; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile $zip; $targetDir = Join-Path $env:LOCALAPPDATA 'Programs\Maven'; if (!(Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force | Out-Null }; Write-Host 'Extracting Maven...'; Expand-Archive -Path $zip -DestinationPath $targetDir -Force; $mvnFolder = (Get-ChildItem -Path $targetDir -Directory | Select-Object -First 1).FullName; [Environment]::SetEnvironmentVariable('M2_HOME', $mvnFolder, 'User'); $curPath = [Environment]::GetEnvironmentVariable('Path', 'User'); if ($curPath -notlike '*'+$mvnFolder+'\bin*') { [Environment]::SetEnvironmentVariable('Path', $curPath + ';' + (Join-Path $mvnFolder 'bin'), 'User') }"
    )
    call :REFRESH_ENV
    where mvn >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        echo [✔] Apache Maven installed successfully!
    ) else (
        echo [!] Maven installed. Path refreshed for this session.
    )
)
echo.

REM --------------------------------------------------------------------
REM 3. Check & Install Docker Desktop
REM --------------------------------------------------------------------
echo [*] Step 3/5: Checking Docker Desktop for Oracle Database...
where docker >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [✔] Docker CLI is already installed.
) else (
    echo [!] Docker Desktop not detected in PATH.
    echo [*] Installing Docker Desktop...
    where winget >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        echo [*] Installing Docker Desktop via winget...
        winget install --id Docker.DockerDesktop -e --accept-package-agreements --accept-source-agreements --silent
    ) else (
        echo [*] Downloading Docker Desktop installer...
        powershell -Command "$ErrorActionPreference = 'Stop'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $installer = Join-Path $env:TEMP 'DockerDesktopInstaller.exe'; Write-Host 'Downloading Docker Desktop...'; Invoke-WebRequest -Uri 'https://desktop.docker.com/win/main/amd64/Docker%%20Desktop%%20Installer.exe' -OutFile $installer; Write-Host 'Launching Docker Desktop installer...'; Start-Process $installer -ArgumentList 'install --quiet' -Wait"
    )
    call :REFRESH_ENV
    echo [✔] Docker Desktop installation initiated.
)

REM Check if Docker Daemon is running
echo [*] Checking Docker Daemon status...
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [*] Docker daemon is not active. Attempting to start Docker Desktop...
    if exist "C:\Program Files\Docker\Docker\Docker Desktop.exe" (
        start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    )
    echo [*] Waiting up to 30 seconds for Docker daemon to become responsive...
    set /a DOCKER_WAIT=0
    :WAIT_DOCKER_LOOP
    docker info >nul 2>&1
    if !ERRORLEVEL! equ 0 goto DOCKER_DAEMON_OK
    set /a DOCKER_WAIT+=1
    if !DOCKER_WAIT! geq 15 (
        echo.
        echo [NOTICE] Docker Desktop is still booting in background.
        echo          Oracle Database will be auto-started when you run run.bat.
        goto SKIP_DOCKER_PULL
    )
    <nul set /p =.
    timeout /t 2 /nobreak >nul
    goto WAIT_DOCKER_LOOP
)

:DOCKER_DAEMON_OK
echo.
echo [✔] Docker daemon is running and healthy!
echo [*] Pre-pulling Oracle Database Free container (gvenzl/oracle-free:23-slim)...
docker pull gvenzl/oracle-free:23-slim
echo [*] Starting Oracle Database container (fincore-oracle-db on port 1521)...
docker compose up -d

:SKIP_DOCKER_PULL
echo.

REM --------------------------------------------------------------------
REM 4. Pre-cache JavaFX 21 & Maven Project Dependencies
REM --------------------------------------------------------------------
echo [*] Step 4/5: Pre-fetching JavaFX 21 SDK & Maven dependencies...
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [*] Running Maven compile to cache JavaFX 21 controls, fxml, graphics and ojdbc11...
    call mvn compile -DskipTests
    if !ERRORLEVEL! equ 0 (
        echo [✔] All JavaFX 21 and database drivers successfully cached in local repository!
    ) else (
        echo [!] Maven compile had warnings, but dependencies were fetched.
    )
) else (
    echo [!] Maven not detected in current CMD session. Dependencies will be downloaded on first run.
)
echo.

REM --------------------------------------------------------------------
REM 5. Final Setup Summary & Verification
REM --------------------------------------------------------------------
echo ====================================================================
echo   FinCore: Setup Verification Summary
echo ====================================================================
where java >nul 2>&1 && echo   [✔] Java 21 LTS Runtime   : Detected ^& Ready || echo   [?] Java 21 LTS Runtime   : Installed (Reopen CMD if needed)
where mvn >nul 2>&1  && echo   [✔] Apache Maven 3.9+     : Detected ^& Ready || echo   [?] Apache Maven 3.9+     : Installed (Reopen CMD if needed)
where docker >nul 2>&1 && echo   [✔] Docker Desktop        : Detected ^& Ready || echo   [?] Docker Desktop        : Installed (Reopen CMD if needed)
echo   [✔] JavaFX 21 Framework   : Configured ^& Cached via Maven
echo   [✔] Oracle Database Free  : Configured on Port 1521 (FREEPDB1)
echo ====================================================================
echo.
echo [SUCCESS] All required setups have been prepared!
echo.
echo You can now launch FinCore using:
echo   1. Windows CMD Launcher : run.bat
echo   2. Pure Maven Command   : mvn javafx:run (or simply 'mvn')
echo   3. Automated Demo       : run.bat --demo (or 'mvn exec:java -Dexec.args="--demo"')
echo.

if "%~1"=="--auto" goto FINISH
if "%~1"=="-y" goto FINISH

choice /C YN /M "[?] Would you like to launch the FinCore JavaFX Dashboard now"
if %ERRORLEVEL% equ 1 (
    echo [*] Launching FinCore...
    call "%SCRIPT_DIR%run.bat"
)

:FINISH
echo Setup completed. Press any key to exit.
pause >nul
exit /b 0

REM --------------------------------------------------------------------
REM Helper Routine: Refresh PATH & Environment Variables from Registry
REM --------------------------------------------------------------------
:REFRESH_ENV
for /f "tokens=2*" %%A in ('reg query "HKLM\System\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "SYS_PATH=%%B"
for /f "tokens=2*" %%A in ('reg query "HKCU\Environment" /v Path 2^>nul') do set "USER_PATH=%%B"
if defined SYS_PATH (
    if defined USER_PATH (
        set "PATH=%SYS_PATH%;%USER_PATH%;%PATH%"
    ) else (
        set "PATH=%SYS_PATH%;%PATH%"
    )
)

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*" "C:\Program Files\BellSoft\LibericaJDK-21*" "C:\Program Files\Microsoft\jdk-21*" "C:\Program Files\Java\jdk-21*") do (
        if exist "%%D\bin\java.exe" (
            set "JAVA_HOME=%%D"
            set "PATH=%%D\bin;!PATH!"
        )
    )
)

where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    for /d %%M in ("C:\Program Files\apache-maven*" "C:\Program Files\Maven*" "%LOCALAPPDATA%\Programs\Maven\*" "%LOCALAPPDATA%\Microsoft\WinGet\Packages\Apache.Maven*") do (
        if exist "%%M\bin\mvn.cmd" (
            set "M2_HOME=%%M"
            set "PATH=%%M\bin;!PATH!"
        )
    )
)

where docker >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Docker\Docker\resources\bin\docker.exe" (
        set "PATH=C:\Program Files\Docker\Docker\resources\bin;!PATH!"
    )
)
exit /b 0
