@echo off
REM ====================================================================
REM Double-click launcher for FinCore Capstone Demonstration Runner
REM ====================================================================
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"
echo ====================================================================
echo   FinCore: Live Capstone Demonstration (Java 21 ^| Oracle DB in Docker)
echo ====================================================================
echo.
call "%SCRIPT_DIR%run.bat" --demo
echo.
echo Demonstration completed. Press any key to exit...
pause >nul
