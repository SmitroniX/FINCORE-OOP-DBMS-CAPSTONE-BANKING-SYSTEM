@echo off
echo ======================================================================
echo           FinCore - Stopping Oracle Database Docker Container
echo ======================================================================
echo.

where docker >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Docker not found in PATH. Exiting.
    pause
    exit /b 1
)

echo [*] Stopping and removing FinCore Oracle container...
docker compose down

echo.
echo [*] Oracle Database container stopped successfully.
pause
