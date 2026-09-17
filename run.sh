#!/usr/bin/env bash
# ====================================================================
# FinCore: OOP & DBMS Capstone Runner Script
# ====================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAR_FILE="$SCRIPT_DIR/target/oop-dbms-capstone-1.0.0-jar-with-dependencies.jar"

if [ "$1" == "--test" ] || [ "$1" == "-t" ]; then
    echo "Running Test Suite..."
    mvn test
    exit 0
fi

# Build fat JAR if not already built or if sources are newer
if [ ! -f "$JAR_FILE" ]; then
    echo "Building executable JAR package (one-time setup)..."
    mvn clean package -DskipTests
fi

# Run application directly with full interactive terminal support
exec java -jar "$JAR_FILE" "$@"
