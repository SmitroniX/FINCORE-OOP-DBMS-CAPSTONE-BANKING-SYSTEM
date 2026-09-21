@echo off
REM ====================================================================
REM Double-click launcher for FinCore Java Swing Graphical Dashboard
REM ====================================================================
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"
call "%SCRIPT_DIR%run.bat" --gui
