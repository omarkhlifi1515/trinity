TRINITY PROJECT - NO DOCKER DEPLOYMENT GUIDE

All apps configured for native runtime deployment (no Docker containers).

=== APP 1: BACKEND (Flask HR Portal) ===

Status: ✓ Ready for Render (Python native)

File: backend/render.yaml
```yaml
services:
  - type: web
    name: trinity-backend
    env: python
    pythonVersion: 3.11
    buildCommand: pip install -r requirements.txt
    startCommand: gunicorn application:app --bind 0.0.0.0:$PORT --workers 3 --timeout 60
    envVars:
      - key: SECRET_KEY
        generateValue: true
      - key: DATABASE_URL
        fromDatabase:
          name: trinity-db
          property: connectionString
      - key: FLASK_ENV
        value: production

databases:
  - name: trinity-db
    databaseName: trinity_5mxn
    user: trinity_5mxn_user
    region: oregon
```

Deployment (Render):
1. Create Web Service → GitHub → backend/ folder
2. Runtime: Python (auto-detected)
3. Render runs: pip install -r requirements.txt
4. Render starts: gunicorn on $PORT
5. Set env vars: SECRET_KEY (generate), DATABASE_URL (link DB), FLASK_ENV=production

Local testing (before deploy):
```powershell
cd backend
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:DATABASE_URL = "postgresql://user:pass@host:5432/db"
python run.py
# Test: http://localhost:5000/health
```

=== APP 2: BLUE TRINITY (Defender & Manager Agent) ===

Status: ✓ Ready for local deployment or scheduled job

This is a background script, not a web service. Run locally or scheduled:

Installation:
```powershell
cd 'blue trinity'
pip install watchdog
# Edit blue_trinity_agent.py: set DRIVE_PATH to your Google Drive folder
python blue_trinity_agent.py manager --once
```

For Render (if you want to deploy as internal service):
Create render.yaml:
```yaml
services:
  - type: worker
    name: blue-trinity-agent
    env: python
    pythonVersion: 3.11
    buildCommand: pip install -r requirements.txt
    startCommand: python blue_trinity_agent.py run --interval 86400
    envVars:
      - key: DRIVE_PATH
        value: "/mnt/drive/Project"  # where logs are accessible
      - key: DATABASE_URL
        value: "sqlite:///path/to/hr_data.db"
    internal: true
```

(Worker = background job, not accessible from internet)

OR keep local (recommended):
- Run on your PC daily via Windows Task Scheduler
- No Render deployment needed
- Direct access to Google Drive files

=== APP 3: RED TRINITY (Parrot Backend) ===

Status: ⚠️ NOT recommended for public Render (security risk)

Why not on Render:
- Executes arbitrary system commands (nmap, msfconsole, etc.)
- RCE (Remote Code Execution) vulnerability if exposed
- Requires Parrot OS tools not available on Render
- Should remain on private/local machine only

If you must deploy (NOT recommended):
- Keep on local Parrot OS machine only
- Or deploy to private infrastructure (not Render public)
- Do NOT make publicly accessible

File: red_trinity/parrot_backend/server.py (no changes needed - keep local)

=== APP 4: ANDROID APPS ===

Status: Build locally with Gradle (not Render)

Files: 
- app2/ (Trinity Android App)
- red_trinity/android_app/

These are Android mobile apps. Deployment:
1. Build locally: ./gradlew assembleRelease
2. Upload to Google Play Store or distribute APK
3. Not deployed to Render (Render is web hosting)

No render.yaml needed for Android apps.

=== RENDER DEPLOYMENT ARCHITECTURE (NO DOCKER) ===

Backend (Web Service):
  Render → Python 3.11 runtime
  ├── pip install -r requirements.txt
  ├── gunicorn start
  └── Connect to Postgres DB
  
Blue Trinity (optional Worker):
  Render → Python 3.11 runtime (internal, non-public)
  ├── pip install watchdog
  ├── Run script periodically
  └── (OR run local on your PC)

Red Trinity (Local only):
  Your PC → Parrot OS
  ├── python server.py
  └── Local network only (NOT public)

Android Apps (Local build):
  Your PC → ./gradlew assembleRelease
  ├── Generate APK
  └── Upload to Play Store

=== STEP-BY-STEP: DEPLOY BACKEND TO RENDER (PYTHON NATIVE) ===

Step 1: Prepare locally
```powershell
cd 'C:\Users\msi\Desktop\trnity\backend'
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
# Test with your Postgres URL
$env:DATABASE_URL = "postgresql://..."
python run.py
# Should show: Running on http://127.0.0.1:5000
```

Step 2: Push to GitHub
```powershell
cd 'C:\Users\msi\Desktop\trnity'
git add .
git commit -m "Remove Docker, use Python native runtime"
git push origin main
```

Step 3: Create Render Web Service
1. Go to https://dashboard.render.com
2. Click "New" → "Web Service"
3. Connect GitHub and select trinity repo
4. Configuration:
   - Name: trinity-backend
   - Root Directory: backend/
   - Runtime: Python (auto-detected)
   - Region: Oregon
5. Click "Create Web Service"

Step 4: Set environment variables
1. Service dashboard → Settings
2. Add variables:
   - SECRET_KEY: (click Generate)
   - DATABASE_URL: (link trinity-db OR paste postgresql://...)
   - FLASK_ENV: production
3. Click Save

Step 5: Deploy
1. Render auto-builds: pip install -r requirements.txt (1-2 min)
2. Starts: gunicorn (30s)
3. Shows URL: https://trinity-backend-xxxxx.onrender.com

Step 6: Test
```powershell
# Health check
curl https://trinity-backend-xxxxx.onrender.com/health

# Login page
https://trinity-backend-xxxxx.onrender.com/auth/login

# Admin panel (Chef login)
https://trinity-backend-xxxxx.onrender.com/admin/dashboard
```

=== ENVIRONMENT VARIABLES (EXPLAINED) ===

SECRET_KEY
- Purpose: Flask session encryption
- Source: Click "Generate" in Render
- Value: Random secure string (never hardcode)

DATABASE_URL
- Purpose: Postgres connection
- Format: postgresql://user:password@host:5432/dbname
- Source: Link trinity-db in Render OR paste from Supabase
- Value: postgresql://trinity_5mxn_user:PASSWORD@host:5432/trinity_5mxn

FLASK_ENV
- Purpose: Production mode
- Value: production
- Effect: Disables debug, enables security features

=== BUILD PROCESS (NO DOCKER) ===

Render automatically:
1. Detects Python 3.11 requirement
2. Creates virtual environment on Render server
3. Runs: pip install -r requirements.txt
4. Installs: Flask, SQLAlchemy, gunicorn, psycopg2-binary, etc.
5. Starts: gunicorn application:app --bind 0.0.0.0:$PORT
6. Service listens on port assigned by Render (via $PORT variable)

Build time: 1-2 minutes (first), <1 minute (cached)

=== TROUBLESHOOTING (PYTHON NATIVE) ===

Issue: "Build failed - ModuleNotFoundError"
Fix: Package missing from requirements.txt
Solution: Add package to requirements.txt, push, Render auto-rebuilds

Issue: "Database connection refused"
Fix: DATABASE_URL not set or wrong
Solution: Check Render Settings → Environment section
Solution: Verify Postgres database is accessible

Issue: "Module not found: gunicorn"
Fix: gunicorn missing from requirements.txt
Solution: Add gunicorn>=21.2.0 to requirements.txt

Issue: "Secret key 'you-will-never-guess' used (not secure)"
Fix: SECRET_KEY not set in Render env vars
Solution: Generate and set SECRET_KEY in Render Settings

Issue: Application crashes after deployment
Fix: Missing database tables
Solution: Run migration before deploying:
```powershell
python migrate_sqlite_to_postgres.py --sqlite "..." --pg "postgresql://..."
```

=== NO DOCKER ADVANTAGES ===

✓ Simpler (no Dockerfile to maintain)
✓ Faster deploys (no container build)
✓ Fewer moving parts (easier debugging)
✓ Render handles Python setup automatically
✓ Smaller attack surface (no container orchestration)
✓ Compatible with all Render plans (including free)

=== UPDATING CODE ===

After changes:
```powershell
git push origin main
```

Render automatically:
1. Detects new commits
2. Rebuilds: pip install (cached, fast)
3. Restarts: gunicorn
4. Zero downtime (new instance replaces old)

=== LOGS & MONITORING ===

View logs:
1. Render Dashboard → trinity-backend → Logs
2. Real-time streaming of Flask output
3. Errors, access logs, debug info

Health check:
```powershell
curl https://trinity-backend-xxxxx.onrender.com/health
# Response: {"status": "healthy", "database": "connected"}
```

Scale up (if needed):
- Settings → Instance Type → upgrade to paid plan
- More RAM/CPU for higher traffic

=== FILE CHECKLIST ===

Before deploying:
☐ backend/render.yaml (Python runtime, no Docker)
☐ backend/config.py (reads DATABASE_URL)
☐ backend/requirements.txt (gunicorn, psycopg2-binary included)
☐ backend/app/__init__.py (has /health endpoint)
☐ Code pushed to GitHub

During deploy (Render):
☐ Web Service created
☐ SECRET_KEY generated
☐ DATABASE_URL linked or pasted
☐ FLASK_ENV = production
☐ Build succeeds (check Logs)
☐ Service shows "Live" status

After deploy:
☐ /health endpoint responds
☐ /auth/login loads
☐ /admin/dashboard accessible (Chef login)
☐ Database tables exist (check Postgres)

=== SUMMARY ===

Backend: Python native runtime on Render (simple, fast)
Blue Trinity: Local or optional Render worker
Red Trinity: Keep local only (security)
Android: Build locally with Gradle

No Docker needed. Deployment is straightforward:
1. Push code to GitHub
2. Create Render Web Service
3. Set env vars
4. Done (Render handles rest)

Ready to deploy! 🚀
