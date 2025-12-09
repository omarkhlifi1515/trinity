# 🚀 Render Deployment Guide for Trinity HRM

This guide will help you deploy your Trinity HRM application to Render.

## Prerequisites

- A Render account (sign up at https://render.com)
- A database (PostgreSQL or MySQL) - Render provides managed databases
- GitHub repository with your code

## Step 1: Prepare Your Repository

1. Make sure all your code is committed and pushed to GitHub
2. Ensure you have a `render.yaml` file in the root directory (already created)

## Step 2: Database Setup

✅ **Your database is already created!** Use these credentials:

**Internal Database URL** (Use this for better performance):
```
postgresql://rinity_hrm_user:yv46EXgzxbAK3vBRod9SifxmHtS08pQX@dpg-d4s6qmmmcj7s73fhs4vg-a/rinity_hrm
```

**External Database URL**:
```
postgresql://rinity_hrm_user:yv46EXgzxbAK3vBRod9SifxmHtS08pQX@dpg-d4s6qmmmcj7s73fhs4vg-a.frankfurt-postgres.render.com/rinity_hrm
```

**Connection Details**:
- Host (Internal): `dpg-d4s6qmmmcj7s73fhs4vg-a`
- Host (External): `dpg-d4s6qmmmcj7s73fhs4vg-a.frankfurt-postgres.render.com`
- Database: `rinity_hrm`
- Username: `rinity_hrm_user`
- Password: `yv46EXgzxbAK3vBRod9SifxmHtS08pQX`
- Port: `5432`

## Step 3: Deploy the Web Service

### Option A: Using render.yaml (Recommended)

1. Go to Render Dashboard
2. Click "New +" → "Blueprint"
3. Connect your GitHub repository
4. Render will automatically detect `render.yaml` and create the service
5. The service will use Docker (as specified in render.yaml)

**Note**: Render doesn't natively support PHP runtime, so we use Docker. The Dockerfile is already configured in your project.

### Option B: Manual Setup

1. Go to Render Dashboard
2. Click "New +" → "Web Service"
3. Connect your GitHub repository
4. Configure the service:
   - **Name**: `trinity-hrm`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `Dockerfile` (should auto-detect)
   - **Plan**: Choose based on your needs (Free tier available)

## Step 4: Configure Environment Variables

In your Render service dashboard, go to "Environment" and add these variables:

### Required Variables

```env
APP_NAME=Trinity HRM
APP_ENV=production
APP_KEY=base64:YOUR_APP_KEY_HERE
APP_DEBUG=false
APP_URL=https://your-app-name.onrender.com
APP_TIMEZONE=UTC

# PostgreSQL Database Configuration
DB_CONNECTION=pgsql
DB_HOST=dpg-d4s6qmmmcj7s73fhs4vg-a
DB_PORT=5432
DB_DATABASE=rinity_hrm
DB_USERNAME=rinity_hrm_user
DB_PASSWORD=yv46EXgzxbAK3vBRod9SifxmHtS08pQX

SESSION_DRIVER=database
CACHE_STORE=database
QUEUE_CONNECTION=database

LOG_CHANNEL=stack
LOG_LEVEL=error
```

**Note**: Use the **Internal Database Host** (`dpg-d4s6qmmmcj7s73fhs4vg-a`) for better performance when your web service is in the same region. If you're in a different region, use the external host: `dpg-d4s6qmmmcj7s73fhs4vg-a.frankfurt-postgres.render.com`

### Optional Variables (for ChatGPT Bot)

```env
OPENAI_API_KEY=your_openai_key
FILAMENT_CHATGPT_BOT_ENABLED=true
FILAMENT_CHATGPT_BOT_API_KEY=your_openai_key
FILAMENT_CHATGPT_BOT_MODEL=gpt-4
```

### Generate APP_KEY

Run this command locally or in Render shell:
```bash
php artisan key:generate --show
```

Copy the output and set it as `APP_KEY` in Render environment variables.

## Step 5: Run Migrations

After your first deployment:

1. Go to your service dashboard
2. Click "Shell" tab
3. Run:
   ```bash
   php artisan migrate --force
   php artisan db:seed
   ```

## Step 6: Set Up Storage

For file storage, you have two options:

### Option A: Use Render's Persistent Disk (Recommended for small files)
- Add a persistent disk in Render dashboard
- Update `FILESYSTEM_DISK=local` in environment variables

### Option B: Use AWS S3 (Recommended for production)
- Set up AWS S3 bucket
- Add these environment variables:
  ```env
  FILESYSTEM_DISK=s3
  AWS_ACCESS_KEY_ID=your_key
  AWS_SECRET_ACCESS_KEY=your_secret
  AWS_DEFAULT_REGION=us-east-1
  AWS_BUCKET=your_bucket_name
  ```

## Step 7: Configure Custom Domain (Optional)

1. In your service dashboard, go to "Settings"
2. Click "Custom Domains"
3. Add your domain and follow DNS configuration instructions

## Troubleshooting

### Build Fails
- Check build logs in Render dashboard
- Ensure all dependencies are in `composer.json` and `package.json`
- Verify PHP version compatibility (8.2+)
- Check Dockerfile is present and correctly configured
- Ensure Docker build completes successfully

### Database Connection Issues
- Verify database credentials in environment variables
- Check if database is accessible from your service
- Use Internal Database URL for better performance

### Assets Not Loading
- Ensure `npm run build` completes successfully
- Check that `VITE_APP_URL` matches your `APP_URL`
- Clear cache: `php artisan cache:clear`

### 500 Errors
- Check logs in Render dashboard
- Verify `APP_DEBUG=false` in production
- Ensure all required environment variables are set

## Performance Optimization

1. **Enable Caching**: Already configured in build command
2. **Use CDN**: Consider using Cloudflare for static assets
3. **Database Indexing**: Ensure proper indexes on frequently queried columns
4. **Queue Workers**: Set up a separate worker service for background jobs

## Monitoring

- Use Render's built-in metrics dashboard
- Set up error tracking (Sentry, Bugsnag, etc.)
- Monitor database performance

## Support

For issues specific to:
- **Render**: Check Render documentation or support
- **Laravel**: Check Laravel documentation
- **Filament**: Check Filament documentation

---

**Note**: The free tier on Render spins down after 15 minutes of inactivity. Consider upgrading to a paid plan for production use.

