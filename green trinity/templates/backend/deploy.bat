@echo off
REM Trinity Backend: Quick Deploy to Render
REM This script helps you set up and deploy the backend to Render with PostgreSQL
REM Run this from: C:\Users\msi\Desktop\trnity\backend

setlocal enabledelayedexpansion

echo.
echo ================================
echo Trinity Backend Render Setup
echo ================================
echo.

REM Check if we're in the right directory
if not exist "requirements.txt" (
    echo ERROR: Please run this script from the backend/ folder
    exit /b 1
)

echo Step 1: Create virtual environment...
if not exist ".venv" (
    python -m venv .venv
    echo Virtual environment created.
) else (
    echo Virtual environment already exists.
)

echo.
echo Step 2: Activate virtual environment and install dependencies...
call .venv\Scripts\activate.bat
pip install --upgrade pip
pip install -r requirements.txt

echo.
echo Step 3: PostgreSQL Database URL
echo.
echo Paste your PostgreSQL connection URL below.
echo Example: postgresql://user:password@host:5432/dbname
echo.
echo If you don't have one yet:
echo - Create a free Postgres at Render (https://render.com) or Supabase (https://supabase.com)
echo - Copy the connection string
echo.
set /p PG_URL="Enter your PostgreSQL URL: "

if "!PG_URL!"=="" (
    echo ERROR: PostgreSQL URL is required
    exit /b 1
)

echo.
echo Step 4: SQLite file location
echo.
set /p SQLITE_PATH="Enter path to your SQLite file (e.g., G:/My Drive/Project/hr_data.db): "

if "!SQLITE_PATH!"=="" (
    set SQLITE_PATH=G:/My Drive/Project/hr_data.db
)

if not exist "!SQLITE_PATH!" (
    echo WARNING: SQLite file not found at !SQLITE_PATH!
    echo Skipping migration. You can run it later manually.
) else (
    echo.
    echo Step 5: Running migration from SQLite to PostgreSQL...
    echo This may take a moment...
    python migrate_sqlite_to_postgres.py --sqlite "!SQLITE_PATH!" --pg "!PG_URL!"
    
    if !errorlevel! equ 0 (
        echo.
        echo SUCCESS: Data migrated to PostgreSQL!
    ) else (
        echo.
        echo ERROR: Migration failed. Check the error above.
        exit /b 1
    )
)

echo.
echo ================================
echo Setup Complete!
echo ================================
echo.
echo Next steps:
echo 1. Push your code to GitHub:
echo    git add .
echo    git commit -m "Add Render deployment files"
echo    git push origin main
echo.
echo 2. Go to Render dashboard: https://dashboard.render.com
echo.
echo 3. Create a new Web Service:
echo    - Connect your GitHub repo
echo    - Select the trinity repo
echo    - Root Directory: backend/
echo    - Runtime: Docker
echo.
echo 4. Set environment variables:
echo    - DATABASE_URL: !PG_URL!
echo    - SECRET_KEY: (click Generate)
echo    - FLASK_ENV: production
echo.
echo 5. Deploy and wait 3-5 minutes
echo.
echo 6. Test your deployment:
echo    - Health check: https://your-service-url.onrender.com/health
echo    - Admin panel: https://your-service-url.onrender.com/admin/dashboard
echo.
echo For detailed instructions, see: RENDER_DEPLOYMENT_GUIDE.md
echo.
pause
