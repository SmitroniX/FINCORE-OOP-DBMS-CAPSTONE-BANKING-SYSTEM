#!/usr/bin/env bash
# ====================================================================
# FinCore: OOP & DBMS Capstone Runner Script
# Supports:
#   ./run.sh          -> Auto-detects GUI/Console or runs interactively
#   ./run.sh --demo   -> Runs the full Capstone Demonstration
#   ./run.sh --gui    -> Launches Java Swing GUI (auto-uses xvfb if headless)
#   ./run.sh --cli    -> Runs interactive console menu
#   ./run.sh --test   -> Runs test suite
# ====================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAR_FILE="$SCRIPT_DIR/target/oop-dbms-capstone-1.0.0-jar-with-dependencies.jar"

if [ "$1" == "--test" ] || [ "$1" == "-t" ]; then
    echo "Running Maven Test Suite..."
    mvn test
    exit 0
fi

if [ "$1" == "--build" ] || [ "$1" == "-b" ]; then
    echo "Building executable JAR package..."
    mvn clean package -DskipTests
    exit 0
fi

# Build fat JAR if not already built or if sources are newer
if [ ! -f "$JAR_FILE" ]; then
    echo "Building executable JAR package..."
    mvn clean package -DskipTests
fi

# If GUI requested or default on headless server with xvfb-run
if [ "$1" == "--gui" ] && [ -z "$DISPLAY" ] && command -v xvfb-run >/dev/null 2>&1; then
    echo "[Info] Launching Java Swing GUI with virtual display (xvfb-run)..."
    exec xvfb-run -a java -jar "$JAR_FILE" "$@"
fi

# Run application directly with interactive terminal support
exec java -jar "$JAR_FILE" "$@"
