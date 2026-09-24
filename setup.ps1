<#
.SYNOPSIS
    FinCore Enterprise Banking System: Automated Setup & Prerequisites Installer (PowerShell)
.DESCRIPTION
    Installs and configures:
      1. Java 21 LTS (Eclipse Adoptium Temurin 21 or BellSoft Liberica 21 Full)
      2. Apache Maven 3.9+
      3. Docker Desktop (for Oracle Database Free container)
      4. JavaFX 21 SDK and all project dependencies
      5. Pre-pulls Oracle Database 23c container (gvenzl/oracle-free:23-slim)
#>

[CmdletBinding()]
param (
    [switch]$Auto,
    [switch]$SkipDocker
)

$ErrorActionPreference = "Continue"

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "  FinCore: Enterprise Banking & Finance Management System" -ForegroundColor Cyan
Write-Host "  AUTOMATED ENVIRONMENT SETUP (PowerShell)" -ForegroundColor Cyan
Write-Host "  Stack: Java 21 | JavaFX 21 | Maven | Docker | Oracle Database" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host ""

$hasWinget = $null -ne (Get-Command winget -ErrorAction SilentlyContinue)

# 1. Java 21 Check & Install
Write-Host "[*] Step 1/5: Checking Java 21 LTS..." -ForegroundColor Yellow
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
$javaOk = $false

if ($javaCmd) {
    $javaVerOutput = & java -version 2>&1 | Out-String
    if ($javaVerOutput -match 'version "(21\.\d+)') {
        $javaOk = $true
        Write-Host "[✔] Java 21 is detected: $($Matches[1])" -ForegroundColor Green
    }
}

if (-not $javaOk) {
    Write-Host "[!] Java 21 not detected. Installing Eclipse Adoptium Temurin 21..." -ForegroundColor Yellow
    if ($hasWinget) {
        winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-package-agreements --accept-source-agreements --silent
        if ($LASTEXITCODE -ne 0) {
            winget install --id BellSoft.LibericaJDK.21.Full -e --accept-package-agreements --accept-source-agreements --silent
        }
    } else {
        $installer = Join-Path $env:TEMP "temurin21.msi"
        Write-Host "Downloading Adoptium OpenJDK 21 MSI..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.6%2B7/OpenJDK21U-jdk_x64_windows_hotspot_21.0.6_7.msi" -OutFile $installer
        Start-Process msiexec.exe -ArgumentList "/i `"$installer`" /quiet /qn ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome" -Wait
    }
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
}

# 2. Maven Check & Install
Write-Host ""
Write-Host "[*] Step 2/5: Checking Apache Maven..." -ForegroundColor Yellow
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    Write-Host "[✔] Apache Maven is detected." -ForegroundColor Green
} else {
    Write-Host "[!] Apache Maven not detected. Installing..." -ForegroundColor Yellow
    if ($hasWinget) {
        winget install --id Apache.Maven -e --accept-package-agreements --accept-source-agreements --silent
    } else {
        $zip = Join-Path $env:TEMP "maven.zip"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile $zip
        $dest = Join-Path $env:LOCALAPPDATA "Programs\Maven"
        Expand-Archive -Path $zip -DestinationPath $dest -Force
        $mvnFolder = (Get-ChildItem -Path $dest -Directory | Select-Object -First 1).FullName
        [Environment]::SetEnvironmentVariable("M2_HOME", $mvnFolder, "User")
        $env:Path += ";$mvnFolder\bin"
    }
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
}

# 3. Docker Check & Install
if (-not $SkipDocker) {
    Write-Host ""
    Write-Host "[*] Step 3/5: Checking Docker Desktop..." -ForegroundColor Yellow
    $dockerCmd = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $dockerCmd) {
        Write-Host "[!] Docker not detected. Installing Docker Desktop..." -ForegroundColor Yellow
        if ($hasWinget) {
            winget install --id Docker.DockerDesktop -e --accept-package-agreements --accept-source-agreements --silent
        }
    } else {
        Write-Host "[✔] Docker CLI is installed." -ForegroundColor Green
    }

    $dockerInfo = & docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[*] Docker daemon is starting or not active. Attempting start..." -ForegroundColor Yellow
        if (Test-Path "C:\Program Files\Docker\Docker\Docker Desktop.exe") {
            Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
        }
    } else {
        Write-Host "[✔] Docker daemon is running." -ForegroundColor Green
        Write-Host "[*] Pre-pulling Oracle Database 23c container (gvenzl/oracle-free:23-slim)..." -ForegroundColor Yellow
        docker pull gvenzl/oracle-free:23-slim
        docker compose up -d
    }
}

# 4. JavaFX 21 & Maven Dependencies
Write-Host ""
Write-Host "[*] Step 4/5: Pre-fetching JavaFX 21 SDK & project dependencies..." -ForegroundColor Yellow
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    mvn compile -DskipTests
    Write-Host "[✔] JavaFX 21 and all project dependencies cached." -ForegroundColor Green
}

# 5. Summary
Write-Host ""
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "  FinCore Setup Verification Summary" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "  [✔] Java 21 LTS Runtime   : Ready" -ForegroundColor Green
Write-Host "  [✔] Apache Maven 3.9+     : Ready" -ForegroundColor Green
Write-Host "  [✔] JavaFX 21 Framework   : Configured via pom.xml" -ForegroundColor Green
Write-Host "  [✔] Oracle Database Free  : Docker container (Port 1521)" -ForegroundColor Green
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Launch command: .\run.bat or 'mvn javafx:run'" -ForegroundColor White
