# ⚡ Quick Start Guide - Render Deployment

This is a quick reference guide to get your Trinity HRM app running on Render.

## 🎯 Your Database Credentials

**Internal Database URL** (Use this - better performance):
```
postgresql://rinity_hrm_user:yv46EXgzxbAK3vBRod9SifxmHtS08pQX@dpg-d4s6qmmmcj7s73fhs4vg-a/rinity_hrm
```

**Connection Details**:
- **DB_CONNECTION**: `pgsql`
- **DB_HOST**: `dpg-d4s6qmmmcj7s73fhs4vg-a` (internal) or `dpg-d4s6qmmmcj7s73fhs4vg-a.frankfurt-postgres.render.com` (external)
- **DB_PORT**: `5432`
- **DB_DATABASE**: `rinity_hrm`
- **DB_USERNAME**: `rinity_hrm_user`
- **DB_PASSWORD**: `yv46EXgzxbAK3vBRod9SifxmHtS08pQX`

## 📝 Environment Variables to Set in Render

Go to your Render service → Environment tab → Add these:

### Essential Variables

```env
APP_NAME=Trinity HRM
APP_ENV=production
APP_KEY=base64:YOUR_GENERATED_KEY_HERE
APP_DEBUG=false
APP_URL=https://your-service-name.onrender.com

DB_CONNECTION=pgsql
DB_HOST=dpg-d4s6qmmmcj7s73fhs4vg-a
DB_PORT=5432
DB_DATABASE=rinity_hrm
DB_USERNAME=rinity_hrm_user
DB_PASSWORD=yv46EXgzxbAK3vBRod9SifxmHtS08pQX

SESSION_DRIVER=database
CACHE_STORE=database
QUEUE_CONNECTION=database
LOG_LEVEL=error
```

### Generate APP_KEY

In Render shell or locally, run:
```bash
php artisan key:generate --show
```

Copy the output and set it as `APP_KEY` in Render environment variables.

## 🚀 Deployment Steps

1. **Push code to GitHub** (if not already done)

2. **Create Web Service on Render**:
   - Go to Render Dashboard
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Render will detect `render.yaml` and create the service

3. **Set Environment Variables**:
   - Go to your service → Environment tab
   - Add all variables from above
   - **Important**: Generate and set `APP_KEY`
   - **Important**: Update `APP_URL` with your actual Render URL

4. **Deploy**:
   - Render will automatically build and deploy
   - Watch the build logs for any errors

5. **Run Migrations**:
   - Go to your service → Shell tab
   - Run: `php artisan migrate --force`
   - (Optional) Run: `php artisan db:seed`

6. **Test**:
   - Visit your Render URL
   - Should see your Laravel app!

## ✅ Verification Checklist

- [ ] Code pushed to GitHub
- [ ] Web service created on Render
- [ ] All environment variables set
- [ ] APP_KEY generated and set
- [ ] APP_URL updated with actual Render URL
- [ ] Database credentials correct
- [ ] Build completed successfully
- [ ] Migrations run
- [ ] App accessible via URL

## 🐛 Common Issues

**Build fails**: Check Dockerfile and build logs
**Database connection error**: Verify DB credentials and host (use internal host)
**500 error**: Check APP_KEY is set, check logs
**Assets not loading**: Ensure `npm run build` completed successfully

## 📚 More Details

- Full deployment guide: `RENDER_DEPLOYMENT.md`
- Environment variables: `RENDER_ENV_SETUP.md`
- Mobile app setup: `mobile app/MOBILE_APP_SETUP.md`

---

**Need help?** Check the logs in Render dashboard or review the detailed guides.

