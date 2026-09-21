#!/usr/bin/env bash
set -e

echo "[*] Stopping FinCore Oracle Database container..."
docker compose down
echo "[*] Container stopped."
