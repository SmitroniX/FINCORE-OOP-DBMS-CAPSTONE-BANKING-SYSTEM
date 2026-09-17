#!/usr/bin/env bash
# ====================================================================
# FinCore: OOP & DBMS Capstone Runner Script
# ====================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================================="
echo " Building and Launching FinCore Banking System Capstone"
echo "========================================================="

if [ "$1" == "--demo" ] || [ "$1" == "-d" ]; then
    echo "Running in Automated Demonstration Mode..."
    mvn exec:java -Dexec.args="--demo"
elif [ "$1" == "--test" ] || [ "$1" == "-t" ]; then
    echo "Running Test Suite..."
    mvn test
else
    echo "Launching Interactive Console Interface..."
    mvn exec:java
fi
