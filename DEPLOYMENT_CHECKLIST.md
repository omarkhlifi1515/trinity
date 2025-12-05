TRINITY PROJECT - DEPLOYMENT CHECKLIST & SUMMARY

All apps are now configured and ready for deployment. Below is a complete checklist.

=== BACKEND (HR Portal) ===

Files Added/Modified:
✓ config.py - Updated to use DATABASE_URL env var (Render) or local SQLite fallback
✓ Dockerfile - Containerized Flask app with gunicorn
✓ render.yaml - Render deployment config (uses Docker build, links to Postgres DB)
✓ app/__init__.py - Added health check endpoint (/health)
✓ app/chef.py - Chef/Admin database management routes (view, edit, delete employees)
✓ migrate_sqlite_to_postgres.py - Safe SQLite → Postgres migration script
✓ requirements.txt - Added psycopg2-binary and SQLAlchemy
✓ RENDER_DEPLOYMENT_GUIDE.md - Full step-by-step Render deployment instructions
✓ deploy.bat - Interactive PowerShell deployment script

Database Setup:
1. Create managed Postgres on Render (https://render.com) or Supabase
2. Get connection URL: postgresql://user:password@host:5432/dbname
3. Run migration locally: python migrate_sqlite_to_postgres.py --sqlite "path/to/hr_data.db" --pg "YOUR_PG_URL"
4. Verify data in Postgres dashboard

Chef/Admin Capabilities:
- URL: /admin/dashboard (requires Chef login)
- View all employees (filter by status, role, paginated)
- Edit employee: name, status, role, contact info
- Delete individual employees: /admin/employee/<id>/delete
- Bulk delete: /admin/bulk-delete (JSON API)
- Export data: /admin/database/export
- View stats: /admin/database/stats

Deployment Steps:
1. Push code to GitHub (include all new files)
2. Create Render Web Service:
   - Connect GitHub repo
   - Root directory: backend/
   - Runtime: Docker (Dockerfile will be auto-detected)
3. Set env variables:
   - DATABASE_URL: (your Postgres connection string)
   - SECRET_KEY: (Render generates or paste a secure key)
   - FLASK_ENV: production
4. Deploy (Render builds and starts automatically)
5. Test: https://your-service-url.onrender.com/health

=== BLUE TRINITY (Defender & Manager Agent) ===

Files Added:
✓ blue trinity/blue_trinity_agent.py - Complete Python agent with two functions
✓ blue trinity/README.md - Installation and usage instructions

Functions:
- Manager: Connects to shared SQLite DB, generates warning letters for absent employees, provides health summary
- Defender: Watches access.log in real-time, detects SQLi/XSS/DoS attacks, appends to blacklist.txt

Configuration:
- Edit DRIVE_PATH in blue_trinity_agent.py (set to your Google Drive folder)
- Install watchdog: pip install watchdog
- Usage examples:
  * manager --once (run once)
  * monitor (watch logs live)
  * run --interval 86400 (run both + health endpoint)
  * serve --port 8000 (health HTTP endpoint only)

Deployment:
- Keep this as a local background agent or schedule it daily via Windows Task Scheduler
- OR deploy to a private Render service (marked Internal) if you want cloud hosting
- It watches files on your Google Drive, so it must run on a machine with Drive access

=== RED TRINITY (Parrot Backend - Penetration Tools) ===

Status: NOT recommended for public Render deployment (runs system commands, security risk)

Options:
A) Keep local on Parrot OS VM (safest)
B) Run on private infrastructure (restricted network)
C) Convert to restricted/safe API (remove arbitrary command execution)
D) If you must cloud-host: mark as Internal/Private on Render (not public)

Current file: red_trinity/parrot_backend/server.py

Note: See SECURITY_RECOMMENDATIONS.md in backend/ for details.

=== ANDROID APPS ===

Java 21 Upgrade (Optional):
- app2/ (Trinity Android App - Gradle-based) - Currently Java 11, can upgrade to Java 21
- red_trinity/android_app/ - Similar setup
- Upgrade requires: AGP 8.13+ (already have), Gradle 8.13 (already have), JDK 21
- To upgrade, update build.gradle.kts compileOptions to Java 21 and kotlinOptions jvmTarget to "21"
- Note: Not required for functionality, mainly for future compatibility

=== QUICK START CHECKLIST ===

Step 1: Backend to Render
  [ ] Create Postgres database (Render or Supabase)
  [ ] Run migration: python migrate_sqlite_to_postgres.py
  [ ] Push code to GitHub
  [ ] Create Render Web Service (backend/)
  [ ] Set DATABASE_URL, SECRET_KEY, FLASK_ENV
  [ ] Deploy and test /health endpoint

Step 2: Blue Trinity Local Setup
  [ ] Edit blue_trinity_agent.py DRIVE_PATH
  [ ] pip install watchdog
  [ ] Test: python blue_trinity_agent.py manager --once
  [ ] Schedule daily job or run in background (python blue_trinity_agent.py run)

Step 3: Optional - Blue Trinity Frontend
  [ ] Create React app in blue trinity/ folder
  [ ] Add calls to backend API (https://your-backend.onrender.com/api/...)
  [ ] Deploy as static site to Render

Step 4: Red Trinity (Keep Local)
  [ ] Keep parrot_backend on private Parrot OS machine
  [ ] Document network setup and access controls
  [ ] Do not expose publicly

Step 5: Android Apps (Optional Upgrade)
  [ ] Review Java 21 upgrade requirements
  [ ] Update build.gradle.kts if needed
  [ ] Test local build

=== IMPORTANT SECURITY REMINDERS ===

✓ Do NOT commit DATABASE_URL or SECRET_KEY to GitHub - use Render env vars
✓ Do NOT expose parrot_backend publicly (system command execution = RCE risk)
✓ HTTPS enabled automatically on Render
✓ Use strong passwords for Postgres (Render generates them)
✓ Monitor Render logs for errors and anomalies
✓ Set up backups for Postgres (Render has automated backups on paid plans)
✓ Restrict Chef role access to database management only
✓ Review all migrations and test locally first

=== NEXT STEPS ===

1. Read RENDER_DEPLOYMENT_GUIDE.md in backend/ for detailed UI instructions
2. Run deploy.bat script from backend/ folder for interactive setup
3. Test migration locally before pushing to GitHub
4. Once backend is live, update blue_trinity_agent.py to call your API endpoints instead of local files
5. Consider adding a React frontend to blue_trinity/ for user-friendly access

=== FILE STRUCTURE AFTER DEPLOYMENT ===

trinity/
├── backend/
│   ├── Dockerfile                         (NEW)
│   ├── render.yaml                        (UPDATED)
│   ├── config.py                          (UPDATED)
│   ├── migrate_sqlite_to_postgres.py      (NEW)
│   ├── RENDER_DEPLOYMENT_GUIDE.md         (NEW)
│   ├── deploy.bat                         (NEW)
│   ├── requirements.txt                   (UPDATED)
│   ├── app/
│   │   ├── __init__.py                    (UPDATED - health check)
│   │   ├── chef.py                        (NEW - admin routes)
│   │   └── ...
│   └── ...
├── blue trinity/
│   ├── blue_trinity_agent.py              (NEW)
│   ├── README.md                          (NEW)
│   └── (future: React frontend)
├── red_trinity/
│   ├── parrot_backend/                    (keep local)
│   ├── android_app/                       (optional Java 21 upgrade)
│   └── ...
└── app2/
    └── (optional Java 21 upgrade)

=== SUPPORT & TROUBLESHOOTING ===

Backend Deployment Issues:
- Check RENDER_DEPLOYMENT_GUIDE.md → TROUBLESHOOTING section
- Verify DATABASE_URL is set correctly in Render env vars
- Check Render Logs tab for build/runtime errors
- Test health endpoint: curl https://your-service-url.onrender.com/health

Migration Issues:
- Run locally first: python migrate_sqlite_to_postgres.py
- Ensure psycopg2-binary is installed
- Check Postgres password special characters (URL encode if needed)
- Verify both databases are accessible

Blue Trinity Issues:
- Ensure watchdog is installed: pip install watchdog
- Check DRIVE_PATH points to correct Google Drive folder
- Test with manager --once first
- Review log output for path/permission errors

Questions? Open an issue on GitHub or review the deployment guide.

Created: December 3, 2025
Status: READY FOR DEPLOYMENT
