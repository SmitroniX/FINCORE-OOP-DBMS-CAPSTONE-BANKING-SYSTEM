#!/usr/bin/env bash
set -e

echo "======================================================================"
echo "          FinCore - Oracle Database (Docker Container Starter)        "
echo "======================================================================"

if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker is not installed or not in PATH."
    echo "FinCore will automatically fall back to embedded SQLite if started with run.sh."
    exit 1
fi

echo "[*] Starting Oracle Database Free container..."
docker compose up -d

echo "[*] Container status:"
docker compose ps

echo "======================================================================"
echo " Oracle Database Container is running!"
echo " Host:     localhost:1521"
echo " Service:  FREEPDB1"
echo " User:     fincore_user"
echo " Pass:     fincore_pass"
echo " JDBC URL: jdbc:oracle:thin:@localhost:1521/FREEPDB1"
echo "======================================================================"
