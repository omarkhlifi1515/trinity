COMPLETE DOCKER + RENDER DEPLOYMENT SETTINGS

All configurations needed to deploy Trinity Backend with Docker on Render.

=== FILE 1: Dockerfile ===
Location: backend/Dockerfile
Status: ✓ Already created

Content:
```dockerfile
# Multi-stage build for Trinity Backend (Flask HR Portal)
FROM python:3.11-slim as base

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Create non-root user for security
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

# Expose port (Render will set PORT env var)
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:5000/health', timeout=5)" || exit 1

# Start gunicorn (Render will override with $PORT)
CMD ["gunicorn", "application:app", "--bind", "0.0.0.0:5000", "--workers", "3", "--timeout", "60", "--access-logfile", "-", "--error-logfile", "-"]
```

=== FILE 2: render.yaml ===
Location: backend/render.yaml
Status: ✓ Already created

Content:
```yaml
services:
  - type: web
    name: trinity-backend
    env: docker
    dockerfile: Dockerfile
    dockerfilePath: ./Dockerfile
    dockerContext: ./
    envVars:
      - key: SECRET_KEY
        generateValue: true
      - key: DATABASE_URL
        fromDatabase:
          name: trinity-db
          property: connectionString
      - key: FLASK_ENV
        value: production
    healthCheckPath: /health

databases:
  - name: trinity-db
    databaseName: trinity_5mxn
    user: trinity_5mxn_user
    region: oregon
```

=== FILE 3: config.py ===
Location: backend/config.py
Status: ✓ Already updated

Content:
```python
import os

basedir = os.path.abspath(os.path.dirname(__file__))

class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'you-will-never-guess'
    
    # Database Configuration
    # Priority: DATABASE_URL env var (Render/production) > local Google Drive SQLite > local SQLite
    database_url = os.environ.get('DATABASE_URL')
    
    if database_url:
        # Running in cloud (Render with Postgres)
        SQLALCHEMY_DATABASE_URI = database_url
    else:
        # Local development: try Google Drive path, fallback to local
        GOOGLE_DRIVE_DB_PATH = os.environ.get('GOOGLE_DRIVE_DB_PATH') or \
            r'C:/Users/msi/Google Drive/Project/hr_data.db'
        
        if GOOGLE_DRIVE_DB_PATH:
            db_dir = os.path.dirname(GOOGLE_DRIVE_DB_PATH)
            if db_dir and not os.path.exists(db_dir):
                try:
                    os.makedirs(db_dir, exist_ok=True)
                except Exception:
                    GOOGLE_DRIVE_DB_PATH = os.path.join(basedir, 'hr_data.db')
        
        DATABASE_PATH = GOOGLE_DRIVE_DB_PATH if GOOGLE_DRIVE_DB_PATH else os.path.join(basedir, 'hr_data.db')
        SQLALCHEMY_DATABASE_URI = 'sqlite:///' + DATABASE_PATH.replace('\\', '/')
    
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    UPLOAD_FOLDER = 'uploads'
    SECURE_DOCUMENT_FOLDER = 'secure_documents'
```

=== FILE 4: requirements.txt ===
Location: backend/requirements.txt
Status: ✓ Already updated

Required content:
```
Flask>=2.3.0
Flask-Login>=0.6.3
Flask-Bcrypt>=1.0.1
Flask-WTF>=1.1.1
Flask-SQLAlchemy>=3.0.5
WTForms>=3.0.1
Werkzeug>=2.3.7
python-dotenv>=1.0.0
SQLAlchemy>=2.0.20
psycopg2-binary>=2.9.6
gunicorn>=21.2.0
requests>=2.31.0
```

=== RENDER UI CONFIGURATION ===

Step 1: Create Web Service
1. Go to https://dashboard.render.com
2. Click "New" → "Web Service"
3. Connect GitHub repo (authorize if needed)
4. Select trinity repository
5. Fill in:
   - Name: trinity-backend
   - Root Directory: backend/
   - Runtime: Docker (auto-detected from Dockerfile)
   - Region: Oregon (or your preference)
   - Instance Type: Free (or upgraded)
6. Click "Create Web Service"

Step 2: Environment Variables (wait for first deploy attempt)
1. Service page → "Settings" tab
2. Scroll to "Environment" section
3. Add variables:

   Variable 1:
   - Key: SECRET_KEY
   - Value: (click "Generate" → Render creates random secure key)

   Variable 2:
   - Key: DATABASE_URL
   - Value: postgresql://trinity_5mxn_user:6q8XxiTlIiEN7NS9ehPiOGENlWeU4Pqr@dpg-d4o6t22dbo4c73ab07t0-a.oregon-postgres.render.com:5432/trinity_5mxn
   
   (OR use Link Database instead - see Step 3)

   Variable 3:
   - Key: FLASK_ENV
   - Value: production

4. Click "Save" (Render redeploys automatically)

Step 3: Link Database (OPTIONAL - if using Render managed Postgres)
1. Same "Settings" page
2. Scroll to "Linked Resources"
3. Click "Link Database"
4. Select "trinity-db" (the Postgres DB)
5. Render auto-fills DATABASE_URL → no need to paste manually

Step 4: Monitor Deployment
1. Click "Logs" tab
2. Watch build progress (should see):
   - "Building Docker image..."
   - "Installing dependencies..."
   - "Building complete"
   - "Deployment running"
   - "Deployment successful" → Service ready at URL

Step 5: Verify Deployment
1. Service will show URL: https://trinity-backend-xxxxx.onrender.com
2. Test health check: curl https://trinity-backend-xxxxx.onrender.com/health
3. Test login page: https://trinity-backend-xxxxx.onrender.com/auth/login
4. Test admin panel: https://trinity-backend-xxxxx.onrender.com/admin/dashboard (Chef login)

=== DOCKER BUILD FLOW ===

What Render does automatically:

1. Detects Dockerfile in root (backend/)
2. Runs: docker build -t trinity-backend .
3. Installs:
   - Python 3.11
   - System packages (gcc, postgresql-client)
   - Python requirements from requirements.txt
4. Copies app code
5. Creates non-root user for security
6. Exposes port 5000
7. Starts gunicorn with 3 workers
8. Healthcheck runs every 30s to verify app is responding

=== ENVIRONMENT VARIABLES EXPLAINED ===

SECRET_KEY
- Purpose: Flask session encryption key
- Source: Render generates (secure random)
- Never commit to code

DATABASE_URL
- Purpose: Postgres connection string
- Format: postgresql://user:password@host:5432/dbname
- Source: Either manual paste OR auto-linked from trinity-db
- Render internal format: fromDatabase: name: trinity-db, property: connectionString

FLASK_ENV
- Purpose: Tell Flask we're in production
- Value: production
- Effect: Disables debug mode, enables security features

=== DOCKERFILE BREAKDOWN ===

Line 1: FROM python:3.11-slim as base
- Use official Python 3.11 (slim = minimal size)

Lines 5-8: RUN apt-get install...
- Install gcc (for compiling Python packages)
- Install postgresql-client (for DB connections)

Lines 11-13: COPY requirements.txt . && RUN pip install
- Copy dependency list
- Install Python packages (cached for faster rebuilds)

Line 16: COPY . .
- Copy all app code into /app

Lines 19-20: RUN useradd...
- Create non-root user "appuser" (security best practice)
- Change ownership to appuser

Line 23: EXPOSE 5000
- Document that port 5000 is used (informational)

Lines 26-28: HEALTHCHECK
- Every 30s, curl http://localhost:5000/health
- If fails 3 times = mark container unhealthy
- Render uses this to detect crashes

Lines 31-32: CMD
- Start gunicorn with:
  * 3 workers (handle 3 concurrent requests)
  * Timeout 60s (long-running requests)
  * Access logs to stdout (Render captures them)
  * Error logs to stdout

=== BUILD COMMANDS (Reference) ===

To build locally and test:

```powershell
cd 'C:\Users\msi\Desktop\trnity\backend'

# Build Docker image
docker build -t trinity-backend:latest .

# Run container locally (test)
docker run -p 5000:5000 `
  -e DATABASE_URL="postgresql://..." `
  -e SECRET_KEY="test-key" `
  -e FLASK_ENV="production" `
  trinity-backend:latest

# Then test: http://localhost:5000/health
```

(Render does this automatically, you don't need to run these commands)

=== EXPECTED BUILD TIME ===

- First deployment: 3-5 minutes (downloads base image, installs packages)
- Subsequent deployments: 1-2 minutes (Docker layer cache)

=== TROUBLESHOOTING ===

Issue: "Build failed - requirements.txt not found"
Fix: Ensure requirements.txt exists in backend/ folder
Check: ls backend/requirements.txt

Issue: "Dockerfile not found"
Fix: Dockerfile must be in backend/ root (where render.yaml is)
Check: ls backend/Dockerfile

Issue: "Health check failing"
Fix: App may still be starting; Render waits 5s before first check
Check: Logs should show app started successfully

Issue: "Database connection refused"
Fix: DATABASE_URL env var not set or incorrect
Fix: Verify in Render Settings → Environment section
Fix: Check Postgres database is running (not suspended)

Issue: "ModuleNotFoundError"
Fix: Package not in requirements.txt
Fix: Add to requirements.txt and push to GitHub (auto-redeploy)

=== RENDER DASHBOARD LOCATIONS ===

Service Overview:
- Dashboard.render.com → Services → trinity-backend

Logs (debug issues):
- trinity-backend → Logs tab

Settings (env vars, linked resources):
- trinity-backend → Settings tab

Deployments (history, rollback):
- trinity-backend → Deployments tab

Events (build/deploy status):
- trinity-backend → Events tab

=== ZERO-DOWNTIME UPDATES ===

To update code without downtime:

1. Make changes locally
2. Test: python run.py (local)
3. Commit and push: git push origin main
4. Render auto-detects push
5. New build starts (old instance still running)
6. New instance becomes healthy
7. Old instance replaced (no downtime)

=== MONITORING & LOGS ===

View live logs:
- Render Dashboard → trinity-backend → Logs tab
- Streaming view of Flask app output

Check health:
- Curl: curl https://trinity-backend-xxxxx.onrender.com/health
- Response: {"status": "healthy", "database": "connected"}

Scale up (paid):
- Settings → Instance Type → select paid tier
- Get more RAM/CPU for higher traffic

=== DATABASE BACKUP ===

Render managed Postgres:
- Free tier: daily backups for 7 days
- Paid tier: hourly backups, longer retention

Download backup:
- Dashboard → trinity-db → Backups tab
- Download for safe-keeping

Manual export:
- Use pgAdmin or pgBackRest (Render provides tools)

=== FINAL CHECKLIST ===

Before deploying:
☐ Dockerfile exists in backend/
☐ render.yaml exists in backend/
☐ requirements.txt includes gunicorn, psycopg2-binary
☐ config.py reads DATABASE_URL
☐ app/__init__.py has /health endpoint
☐ Code pushed to GitHub
☐ Postgres database created (Render or Supabase)

During deployment (Render UI):
☐ Web Service created (Docker runtime)
☐ SECRET_KEY env var set (or click Generate)
☐ DATABASE_URL env var set (or linked database)
☐ FLASK_ENV = production
☐ Health check passes (Logs tab)

After deployment:
☐ Service URL shows (e.g., trinity-backend-xxxxx.onrender.com)
☐ /health endpoint responds 200 OK
☐ /auth/login page loads
☐ /admin/dashboard accessible with Chef login

Everything is ready. Just follow the Render UI steps above to deploy!
