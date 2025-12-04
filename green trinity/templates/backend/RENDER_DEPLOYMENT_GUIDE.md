Render Deployment Guide - Trinity Backend (HR Portal + Database)

This guide will walk you through deploying the Trinity Backend to Render with a managed PostgreSQL database.

PREREQUISITES
- GitHub account with your repository pushed
- Render account (free tier available at https://render.com)
- PostgreSQL connection URL (from Render managed DB or Supabase)

STEP 1: Set Up PostgreSQL Database

Option A: Render Managed Postgres (RECOMMENDED)
1. Log in to Render Dashboard (https://dashboard.render.com)
2. Click "New" → "PostgreSQL"
3. Fill in:
   - Name: trinity-db
   - Database: trinity_5mxn
   - User: trinity_5mxn_user
   - Region: oregon (or your preferred region)
   - IPV4 CIDR: Leave blank (allow all)
4. Click "Create Database"
5. Wait 3-5 minutes for provisioning
6. Copy the connection string (starts with "postgresql://")
7. Keep this URL safe — you'll need it in Step 3

Option B: Supabase (FREE ALTERNATIVE)
1. Go to https://supabase.com
2. Sign up and create new project
3. In project settings, copy the "postgres://" connection string
4. Use this URL in Step 3

STEP 2: Migrate Your Local Data

Before deploying, migrate your SQLite data to Postgres (safe, runs locally):

PowerShell:
```powershell
cd 'C:\Users\msi\Desktop\trnity\backend'

# Activate virtual environment
python -m venv .venv
.venv\Scripts\Activate.ps1

# Install dependencies
pip install -r requirements.txt

# Run migration (replace the --pg URL with your actual Postgres URL from Step 1)
python migrate_sqlite_to_postgres.py --sqlite "G:/My Drive/Project/hr_data.db" --pg "postgresql://trinity_5mxn_user:YOUR_PASSWORD@dpg-xyz.oregon-postgres.render.com:5432/trinity_5mxn"
```

Verify the migration succeeded by checking table counts in your Postgres database (you can use pgAdmin or Supabase dashboard).

STEP 3: Create Render Web Service

1. In Render Dashboard, click "New" → "Web Service"
2. Connect your GitHub repository (authorize if needed)
3. Select your trinity repo
4. Fill in deployment settings:
   - Name: trinity-backend
   - Root Directory: backend/
   - Runtime: Docker
   - Build Command: (leave empty — uses Dockerfile)
   - Start Command: (leave empty — uses Dockerfile)
   - Instance Type: Free (or upgraded for production)
   - Region: oregon (match your DB region)
5. Click "Create Web Service"

STEP 4: Configure Environment Variables

1. While the service is deploying, go to its settings page
2. Scroll to "Environment" section
3. Add environment variables:

   - Key: DATABASE_URL
     Value: (paste your Postgres URL from Step 1)
     Example: postgresql://trinity_5mxn_user:password@dpg-abc123.oregon-postgres.render.com:5432/trinity_5mxn

   - Key: SECRET_KEY
     Value: (click "Generate" button to create a secure random key)

   - Key: FLASK_ENV
     Value: production

4. Click "Save Changes"

STEP 5: Deploy

1. Render will automatically rebuild and deploy when you push changes to GitHub, OR
2. Manually trigger deployment:
   - In Service settings, click "Manual Deploy" → "Clear Build Cache & Deploy"

Monitor logs in the "Logs" tab to check for errors.

STEP 6: Test Your Deployment

1. Once deployed, Render will show you the URL (e.g., https://trinity-backend-xxxxx.onrender.com)
2. Test endpoints:
   - Health check: https://trinity-backend-xxxxx.onrender.com/health
   - Login: https://trinity-backend-xxxxx.onrender.com/auth/login
   - Admin panel: https://trinity-backend-xxxxx.onrender.com/admin/dashboard (requires Chef login)

DATABASE MANAGEMENT (CHEF/ADMIN CONTROLS)

Once deployed, Chefs can manage the database via the admin panel:

URL: https://your-service-url.onrender.com/admin/dashboard

Available operations:
- View all employees (list, filter by status/role)
- Edit employee information (name, status, contact info)
- Delete individual employees (/admin/employee/<id>/delete)
- Bulk delete employees (/admin/bulk-delete)
- Export employee data as JSON (/admin/database/export)
- View database statistics (/admin/database/stats)

API Endpoints for Chefs (JSON):
```
GET  /admin/employees?page=1&status=Active          - List employees
GET  /admin/employee/<id>                            - Get employee details
POST /admin/employee/<id>                            - Update employee
POST /admin/employee/<id>/delete                     - Delete employee
POST /admin/bulk-delete                              - Delete multiple
GET  /admin/database/stats                           - Database stats
GET  /admin/database/export                          - Export all data
```

SECURITY CHECKLIST

Before making the app public, ensure:

✓ SECRET_KEY is generated (random, secure) and set in Render env vars
✓ DATABASE_URL is set and points to private Postgres
✓ Only users with "Chef" or "Admin" roles can access /admin/* endpoints
✓ HTTPS is enabled (Render provides automatic TLS)
✓ Backup your Postgres database regularly (Render supports automated backups on paid plans)
✓ Monitor logs for errors and suspicious activity

TROUBLESHOOTING

Issue: "DATABASE_URL is required"
Fix: Ensure DATABASE_URL env var is set in Render service settings. Check the value exactly matches your Postgres connection string.

Issue: Migration fails (psycopg2 error)
Fix: Install psycopg2-binary locally:
```powershell
pip install psycopg2-binary
```

Issue: Deployment fails (Dockerfile not found)
Fix: Ensure your repository structure is:
  backend/
  ├── Dockerfile
  ├── requirements.txt
  ├── app/
  └── ...
Render looks for Dockerfile in the root directory of the service (backend/ folder in this case).

Issue: Admin panel shows "Access denied"
Fix: Ensure your user has the "Chef" or "Admin" role. Log in as a user with these roles.

UPDATING THE CODE

When you push new commits to GitHub (main branch):
1. Render automatically rebuilds and deploys
2. Monitor the deployment in Render Logs tab
3. If it fails, check error messages and fix locally

Manual redeploy:
1. Go to Service settings
2. Click "Manual Deploy" → "Clear Build Cache & Deploy"

ROLLING BACK

If a deployment breaks the app:
1. Go to Service settings → "Deployments"
2. Find the last working deployment
3. Click "Rollback to this deployment"

LOCAL TESTING (Before deploying)

Test locally with Postgres:
```powershell
cd 'C:\Users\msi\Desktop\trnity\backend'
.venv\Scripts\Activate.ps1
$env:DATABASE_URL = 'postgresql://user:pass@localhost:5432/trinity_5mxn'
python run.py
```

Then visit http://localhost:5000 and test all features.

NEXT STEPS

1. Follow Steps 1-6 above to deploy
2. Test admin panel at /admin/dashboard
3. Configure file uploads (currently stored in container — consider adding S3 or Supabase storage for persistence)
4. Set up monitoring and log aggregation (Render provides built-in logs)
5. Enable CORS for frontend apps that call this API

Questions? Check Render docs: https://render.com/docs
