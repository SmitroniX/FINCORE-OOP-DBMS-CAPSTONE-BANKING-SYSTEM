#!/usr/bin/env bash
# ====================================================================
# FinCore: OOP & DBMS Capstone All-In-One Runner Script
# Supports:
#   ./run.sh          -> Launches JavaFX 21 GUI (with Oracle DB in Docker)
#   ./run.sh --demo   -> Runs automated Capstone Demonstration
#   ./run.sh --cli    -> Runs interactive console menu
#   ./run.sh --test   -> Runs test suite
# ====================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 1. Check Docker if available
if command -v docker &> /dev/null; then
    if docker info &> /dev/null; then
        if ! docker inspect -f "{{.State.Running}}" fincore-oracle-db 2>/dev/null | grep -q "true"; then
            echo "[*] Starting Oracle Database container in Docker..."
            docker compose up -d
        fi

        echo "[*] Verifying Oracle Database service FREEPDB1 is ready..."
        ATTEMPTS=0
        while [ $ATTEMPTS -lt 35 ]; do
            if docker logs fincore-oracle-db 2>&1 | grep -q "DATABASE IS READY TO USE!"; then
                echo "[SUCCESS] Oracle Database is CONNECTED and READY on port 1521!"
                break
            fi
            printf "."
            sleep 2
            ATTEMPTS=$((ATTEMPTS+1))
        done
        echo ""
    fi
fi

if [ "$1" == "--test" ] || [ "$1" == "-t" ]; then
    echo "[*] Running Maven Test Suite..."
    mvn test
    exit 0
fi

if [ "$1" == "--demo" ] || [ "$1" == "-d" ]; then
    echo "[*] Running Automated Capstone Demonstration..."
    mvn exec:java -Dexec.args="--demo"
    exit 0
fi

if [ "$1" == "--cli" ] || [ "$1" == "-c" ]; then
    echo "[*] Starting Interactive Console Menu..."
    mvn exec:java -Dexec.args="--cli"
    exit 0
fi

if [ "$1" == "--build" ] || [ "$1" == "-b" ]; then
    echo "[*] Building executable JAR package..."
    mvn clean package -DskipTests
    exit 0
fi

# Default: Launch JavaFX 21 GUI
echo "[*] Launching FinCore JavaFX 21 Dashboard..."
mvn javafx:run
