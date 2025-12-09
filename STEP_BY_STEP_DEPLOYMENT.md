# 🚀 Step-by-Step Render Deployment Guide

Follow these steps in order to deploy your Trinity HRM application to Render.

---

## 📋 Prerequisites Checklist

Before starting, make sure you have:
- [ ] GitHub account
- [ ] Render account (sign up at https://render.com)
- [ ] Your code pushed to a GitHub repository
- [ ] Your PostgreSQL database already created on Render

---

## Step 1: Prepare Your Code Repository 📦

### 1.1 Verify Files Are Ready

Make sure these files exist in your repository root:
- ✅ `Dockerfile` (already created)
- ✅ `render.yaml` (already created)
- ✅ `docker-entrypoint.sh` (already created)
- ✅ `composer.json`
- ✅ `package.json`

### 1.2 Commit and Push to GitHub

```bash
# If you haven't already, commit all changes
git add .
git commit -m "Prepare for Render deployment"
git push origin main
```

**✅ Checkpoint**: Your code should be on GitHub.

---

## Step 2: Create Blueprint on Render 🎨

### 2.1 Go to Render Dashboard

1. Log in to https://dashboard.render.com
2. Click **"New +"** button (top right)
3. Select **"Blueprint"**

### 2.2 Connect Your Repository

1. Click **"Connect account"** or **"Connect repository"**
2. Select **GitHub**
3. Authorize Render to access your GitHub
4. Select your repository: `trinity` (or your repo name)
5. Click **"Connect"**

### 2.3 Create Blueprint

1. Render will detect your `render.yaml` file
2. Review the services it will create:
   - ✅ Web Service: `trinity-hrm`
3. Click **"Apply"** or **"Create Blueprint"**

**✅ Checkpoint**: Render is now creating your service. Wait 5-10 minutes for the build.

---

## Step 3: Wait for Initial Build ⏳

### 3.1 Monitor Build Progress

1. You'll be redirected to your service dashboard
2. Watch the **"Events"** or **"Logs"** tab
3. The build will:
   - Pull your code
   - Build Docker image
   - Install dependencies
   - Build frontend assets

### 3.2 Build Should Complete Successfully

Look for:
```
✅ Build successful
✅ Service is live
```

**⚠️ If build fails**: Check the error logs and see troubleshooting section.

**✅ Checkpoint**: Build completed. Service URL is available.

---

## Step 4: Get Your Service URL 🌐

### 4.1 Find Your URL

1. In your service dashboard
2. Look for **"URL"** or **"Service URL"**
3. It will look like: `https://trinity-hrm-xxxx.onrender.com`
4. **Copy this URL** - you'll need it!

**✅ Checkpoint**: You have your service URL.

---

## Step 5: Set Environment Variables 🔧

### 5.1 Open Environment Tab

1. In your service dashboard
2. Click **"Environment"** tab (left sidebar)
3. Click **"Add Environment Variable"**

### 5.2 Add Critical Variables First

Add these one by one:

#### APP_KEY (Generate First!)

1. Click **"Shell"** tab
2. Run: `php artisan key:generate --show`
3. Copy the output (starts with `base64:`)
4. Go back to **"Environment"** tab
5. Add:
   - **Key**: `APP_KEY`
   - **Value**: (paste the copied value)
   - Click **"Save Changes"**

#### APP_URL

1. Add new variable:
   - **Key**: `APP_URL`
   - **Value**: `https://your-actual-service-url.onrender.com` (use your URL from Step 4)
   - Click **"Save Changes"**

### 5.3 Add Database Variables

Add these database variables:

```env
DB_CONNECTION=pgsql
DB_HOST=dpg-d4s6qmmmcj7s73fhs4vg-a
DB_PORT=5432
DB_DATABASE=rinity_hrm
DB_USERNAME=rinity_hrm_user
DB_PASSWORD=yv46EXgzxbAK3vBRod9SifxmHtS08pQX
```

**Add each one separately:**
1. Click **"Add Environment Variable"**
2. Enter Key and Value
3. Click **"Save Changes"**
4. Repeat for each variable

### 5.4 Add Application Variables

Add these:

```env
APP_NAME=Trinity HRM
APP_ENV=production
APP_DEBUG=false
SESSION_DRIVER=database
CACHE_STORE=database
QUEUE_CONNECTION=database
LOG_LEVEL=error
```

### 5.5 Save All Changes

After adding all variables:
- Render will automatically **redeploy** your service
- Wait for the redeploy to complete (2-3 minutes)

**✅ Checkpoint**: All environment variables are set. Service is redeploying.

---

## Step 6: Run Database Migrations 🗄️

### 6.1 Open Shell

1. In your service dashboard
2. Click **"Shell"** tab
3. Wait for shell to connect

### 6.2 Run Migrations

Type and run:

```bash
php artisan migrate --force
```

**Expected output:**
```
Migration table created successfully.
Migrating: 2024_01_01_000001_create_users_table
Migrated:  2024_01_01_000001_create_users_table
...
```

### 6.3 (Optional) Run Seeders

If you have seeders:

```bash
php artisan db:seed
```

**✅ Checkpoint**: Database is set up with all tables.

---

## Step 7: Test Your Application ✅

### 7.1 Visit Your Service URL

1. Open your service URL in a browser
2. You should see:
   - ✅ Laravel welcome page, OR
   - ✅ Your application homepage, OR
   - ✅ Login page

### 7.2 Test Database Connection

In Shell, run:

```bash
php artisan tinker
```

Then type:
```php
DB::connection()->getPdo();
```

**Expected**: Should return a PDO object (not an error)

Type `exit` to leave tinker.

### 7.3 Check Logs

1. Go to **"Logs"** tab
2. Look for any errors
3. Should see normal application logs

**✅ Checkpoint**: Application is working!

---

## Step 8: Verify Everything Works 🎉

### 8.1 Final Checklist

- [ ] Service URL is accessible
- [ ] No 500 errors
- [ ] Database connection works
- [ ] Migrations completed
- [ ] Logs show no critical errors
- [ ] Can access admin panel (if applicable)

### 8.2 Test Key Features

- [ ] Login works (if you have authentication)
- [ ] Database queries work
- [ ] Static assets load correctly
- [ ] API endpoints work (if applicable)

**✅ Checkpoint**: Everything is working!

---

## 🐛 Troubleshooting Common Issues

### Build Failed

**Problem**: Build fails during Docker build

**Solutions**:
- Check Dockerfile is correct
- Verify all dependencies are in composer.json
- Check build logs for specific error
- Ensure PHP 8.3 is used

### 500 Error After Deployment

**Problem**: Service shows 500 Internal Server Error

**Solutions**:
- ✅ Check `APP_KEY` is set
- ✅ Check `APP_URL` matches your actual URL
- ✅ Check database credentials are correct
- ✅ Check logs for specific error message
- ✅ Verify migrations ran successfully

### Database Connection Error

**Problem**: Can't connect to database

**Solutions**:
- ✅ Verify database service is running
- ✅ Check database credentials match exactly
- ✅ Use internal host: `dpg-d4s6qmmmcj7s73fhs4vg-a`
- ✅ Verify database name: `rinity_hrm`
- ✅ Check password has no extra spaces

### Assets Not Loading

**Problem**: CSS/JS files return 404

**Solutions**:
- ✅ Check build completed successfully
- ✅ Verify `npm run build` ran without errors
- ✅ Check browser console for specific 404s
- ✅ Clear browser cache

### Service Keeps Restarting

**Problem**: Service shows "Unavailable" or keeps restarting

**Solutions**:
- ✅ Check logs for crash errors
- ✅ Verify entrypoint script is executable
- ✅ Check PORT environment variable is set
- ✅ Verify all required env vars are set

---

## 📞 Getting Help

If you're stuck:

1. **Check Logs**: Always check the Logs tab first
2. **Check Build Logs**: Look at the build output
3. **Render Docs**: https://render.com/docs
4. **Laravel Docs**: https://laravel.com/docs

---

## 🎯 Quick Reference

### Your Database Credentials
```
Host: dpg-d4s6qmmmcj7s73fhs4vg-a
Port: 5432
Database: rinity_hrm
Username: rinity_hrm_user
Password: yv46EXgzxbAK3vBRod9SifxmHtS08pQX
```

### Important Commands

```bash
# Generate APP_KEY
php artisan key:generate --show

# Run migrations
php artisan migrate --force

# Run seeders
php artisan db:seed

# Test database
php artisan tinker
>>> DB::connection()->getPdo();
```

---

## ✅ Success!

Once all steps are complete, your Trinity HRM application is live on Render! 🎉

**Next Steps**:
- Set up custom domain (optional)
- Configure email settings
- Set up monitoring
- Update mobile app with your Render URL

---

**Need to update something?** Just push to GitHub and Render will auto-deploy (if auto-deploy is enabled).

