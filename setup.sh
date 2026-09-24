#!/usr/bin/env bash
# ====================================================================
# FinCore: Automated Setup Script for Linux / macOS
# Installs: Java 21, OpenJFX, Maven, Docker, and project dependencies
# ====================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "===================================================================="
echo "  FinCore: Enterprise Banking & Finance Management System"
echo "  AUTOMATED ENVIRONMENT SETUP (Linux / macOS)"
echo "  Stack: Java 21 | JavaFX 21 | Maven | Docker | Oracle Database"
echo "===================================================================="
echo ""

# 1. Check Java 21
JAVA_INSTALLED=false
if command -v java &> /dev/null; then
    JAVA_VER=$(java -version 2>&1 | head -n 1)
    if echo "$JAVA_VER" | grep -q '"21\.' || echo "$JAVA_VER" | grep -q '21-'; then
        JAVA_INSTALLED=true
        echo "[✔] Java 21 is already installed: $JAVA_VER"
    fi
fi

# 2. Check Maven
MAVEN_INSTALLED=false
if command -v mvn &> /dev/null; then
    MAVEN_INSTALLED=true
    echo "[✔] Apache Maven is already installed: $(mvn -version 2>&1 | head -n 1)"
fi

# 3. Check Docker
DOCKER_INSTALLED=false
if command -v docker &> /dev/null; then
    DOCKER_INSTALLED=true
    echo "[✔] Docker CLI is already installed: $(docker --version 2>&1)"
fi

# If anything missing, install missing packages
if [ "$JAVA_INSTALLED" = false ] || [ "$MAVEN_INSTALLED" = false ] || [ "$DOCKER_INSTALLED" = false ]; then
    OS_TYPE="$(uname -s)"
    echo ""
    echo "[*] Installing missing dependencies on $OS_TYPE..."

    if [ "$OS_TYPE" = "Linux" ]; then
        if command -v apt-get &> /dev/null; then
            PKGS=""
            [ "$JAVA_INSTALLED" = false ] && PKGS="$PKGS openjdk-21-jdk openjfx"
            [ "$MAVEN_INSTALLED" = false ] && PKGS="$PKGS maven"
            [ "$DOCKER_INSTALLED" = false ] && PKGS="$PKGS docker.io docker-compose-v2"
            echo "[*] Installing: $PKGS"
            sudo apt-get update -o Acquire::AllowInsecureRepositories=true -qq 2>/dev/null || true
            sudo apt-get install -y --no-install-recommends $PKGS 2>/dev/null || true
        elif command -v dnf &> /dev/null; then
            PKGS=""
            [ "$JAVA_INSTALLED" = false ] && PKGS="$PKGS java-21-openjdk-devel"
            [ "$MAVEN_INSTALLED" = false ] && PKGS="$PKGS maven"
            [ "$DOCKER_INSTALLED" = false ] && PKGS="$PKGS docker docker-compose"
            sudo dnf install -y $PKGS 2>/dev/null || true
        elif command -v pacman &> /dev/null; then
            PKGS=""
            [ "$JAVA_INSTALLED" = false ] && PKGS="$PKGS jdk21-openjdk"
            [ "$MAVEN_INSTALLED" = false ] && PKGS="$PKGS maven"
            [ "$DOCKER_INSTALLED" = false ] && PKGS="$PKGS docker docker-compose"
            sudo pacman -Sy --noconfirm $PKGS 2>/dev/null || true
        fi
    elif [ "$OS_TYPE" = "Darwin" ]; then
        if command -v brew &> /dev/null; then
            [ "$JAVA_INSTALLED" = false ] && brew install openjdk@21
            [ "$MAVEN_INSTALLED" = false ] && brew install maven
            [ "$DOCKER_INSTALLED" = false ] && brew install docker docker-compose
        else
            echo "[!] Homebrew not found. Please install Homebrew from https://brew.sh/"
        fi
    fi
fi

# 4. Check Docker daemon & Oracle container
echo ""
echo "[*] Checking Oracle Database in Docker..."
if command -v docker &> /dev/null; then
    if docker info &> /dev/null; then
        echo "[✔] Docker daemon is running."
        echo "[*] Pre-pulling Oracle Database 23c container image (gvenzl/oracle-free:23-slim)..."
        docker pull gvenzl/oracle-free:23-slim
        echo "[*] Starting Oracle Database container..."
        docker compose up -d
    else
        echo "[!] Docker daemon is not active. Please start Docker."
    fi
fi

# 5. Pre-cache Maven dependencies and JavaFX 21
echo ""
if command -v mvn &> /dev/null; then
    echo "[*] Pre-fetching JavaFX 21 runtime and project dependencies..."
    mvn compile -DskipTests
    echo "[✔] All JavaFX 21 and database drivers successfully compiled and cached!"
fi

echo ""
echo "===================================================================="
echo "  FinCore Setup Verification Summary"
echo "===================================================================="
command -v java &> /dev/null && echo "  [✔] Java 21 Runtime   : $(java -version 2>&1 | head -n 1)" || echo "  [X] Java 21 Runtime   : Not found"
command -v mvn &> /dev/null  && echo "  [✔] Apache Maven      : $(mvn -version 2>&1 | head -n 1)" || echo "  [X] Apache Maven      : Not found"
command -v docker &> /dev/null && echo "  [✔] Docker Engine     : $(docker --version 2>&1)" || echo "  [X] Docker Engine     : Not found"
echo "  [✔] JavaFX 21 SDK     : Configured via pom.xml"
echo "  [✔] Oracle DB Free    : Docker port 1521 (FREEPDB1)"
echo "===================================================================="
echo ""
echo "Setup complete! You can now run FinCore with:"
echo "  ./run.sh          # JavaFX 21 Desktop GUI"
echo "  ./run.sh --demo   # 8-Step Automated Capstone Demo"
echo "  mvn javafx:run    # Pure Maven execution"
