# ✅ What to Do After Creating Blueprint on Render

After you create the blueprint and Render deploys your service, follow these steps:

## Step 1: Wait for Initial Deployment ⏳

- Render will automatically build and deploy your service
- This can take 5-10 minutes
- Watch the build logs in your Render dashboard
- Wait for the build to complete successfully

## Step 2: Get Your Service URL 🌐

1. Go to your web service dashboard
2. Find your service URL (e.g., `https://trinity-hrm.onrender.com`)
3. **Copy this URL** - you'll need it for `APP_URL`

## Step 3: Set Environment Variables 🔧

**Go to your web service → "Environment" tab**

### Critical Variables (Must Set):

```env
APP_KEY=base64:YOUR_GENERATED_KEY
APP_URL=https://your-actual-service-url.onrender.com
```

**Generate APP_KEY:**
1. Click on "Shell" tab in your Render service
2. Run: `php artisan key:generate --show`
3. Copy the output (starts with `base64:`)
4. Go back to "Environment" tab
5. Add `APP_KEY` with the copied value

### Database Variables (Copy these exactly):

```env
DB_CONNECTION=pgsql
DB_HOST=dpg-d4s6qmmmcj7s73fhs4vg-a
DB_PORT=5432
DB_DATABASE=rinity_hrm
DB_USERNAME=rinity_hrm_user
DB_PASSWORD=yv46EXgzxbAK3vBRod9SifxmHtS08pQX
```

### Other Required Variables:

```env
APP_NAME=Trinity HRM
APP_ENV=production
APP_DEBUG=false
SESSION_DRIVER=database
CACHE_STORE=database
QUEUE_CONNECTION=database
LOG_LEVEL=error
```

**After adding variables, Render will automatically redeploy!**

## Step 4: Run Database Migrations 🗄️

1. Go to your service → "Shell" tab
2. Run these commands:

```bash
php artisan migrate --force
```

(Optional) If you have seeders:
```bash
php artisan db:seed
```

## Step 5: Verify Everything Works ✅

1. **Check Health**: Visit your service URL
   - Should see Laravel welcome page or your app
   - No 500 errors

2. **Test Database Connection** (in Shell):
   ```bash
   php artisan tinker
   >>> DB::connection()->getPdo();
   ```
   Should return a PDO object (not an error)

3. **Check Logs**:
   - Go to "Logs" tab
   - Look for any errors
   - Should see normal application logs

## Step 6: Update Mobile App (If Needed) 📱

If you're using the mobile app:

1. Update `mobile app/app/build.gradle.kts`
2. Change `BASE_URL` to your Render URL:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://your-service.onrender.com/api/\"")
   ```
3. Rebuild the mobile app

## 🐛 Troubleshooting

### Build Failed?
- Check build logs for errors
- Verify Dockerfile is correct
- Check for missing dependencies

### 500 Error After Deployment?
- ✅ Check `APP_KEY` is set
- ✅ Check `APP_URL` matches your actual URL
- ✅ Check database credentials are correct
- ✅ Check logs for specific error messages

### Database Connection Error?
- ✅ Verify database is running (check database service)
- ✅ Use internal host: `dpg-d4s6qmmmcj7s73fhs4vg-a`
- ✅ Check password is correct (no extra spaces)
- ✅ Verify database exists: `rinity_hrm`

### Assets Not Loading?
- ✅ Check build completed successfully
- ✅ Verify `npm run build` ran without errors
- ✅ Check browser console for 404 errors

## 📋 Quick Checklist

After blueprint creation:

- [ ] Build completed successfully
- [ ] Got service URL
- [ ] Generated and set `APP_KEY`
- [ ] Set `APP_URL` with actual URL
- [ ] Set all database variables
- [ ] Set other required environment variables
- [ ] Service redeployed after env vars
- [ ] Ran migrations
- [ ] Tested service URL (works!)
- [ ] Verified database connection
- [ ] Checked logs (no critical errors)

## 🎉 You're Done!

Once all steps are complete, your app should be live and accessible!

---

**Need the full environment variables list?** See `RENDER_ENV_SETUP.md`

**Need detailed deployment guide?** See `RENDER_DEPLOYMENT.md`

**Quick reference?** See `QUICK_START_RENDER.md`

