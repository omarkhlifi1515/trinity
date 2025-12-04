@echo off
REM Red Trinity Backend Server - Windows Startup Script

echo ============================================================
echo Red Trinity Backend Server - Windows
echo ============================================================
echo.
echo WARNING: Running on Windows
echo Most penetration testing tools are NOT available on Windows
echo Attacks will likely fail unless tools are installed separately
echo.
echo For full functionality, run on Parrot OS workstation instead
echo ============================================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.7+ from https://www.python.org/
    pause
    exit /b 1
)

REM Check if virtual environment exists
if not exist "venv" (
    echo Creating virtual environment...
    python -m venv venv
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Install/update dependencies
echo Installing dependencies...
pip install -r requirements.txt --quiet

REM Start server
echo.
echo Starting server...
echo.
python server.py

pause

