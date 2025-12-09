# Deployment Guide for Render

## 📋 Prerequisites

- GitHub account
- Render account (free tier works)
- Your code pushed to GitHub

## 🚀 Deployment Steps

### Step 1: Prepare Your Code

1. **Make sure all files are committed:**
   ```bash
   git status
   git add .
   git commit -m "Ready for Render deployment"
   ```

2. **Push to GitHub:**
   ```bash
   git push origin main
   ```

### Step 2: Deploy on Render

#### Option A: Using Render Dashboard (Easiest)

1. **Go to Render Dashboard**
   - Visit [https://dashboard.render.com](https://dashboard.render.com)
   - Sign up or log in with GitHub

2. **Create New Web Service**
   - Click the **"New +"** button
   - Select **"Web Service"**
   - Connect your GitHub account if not already connected
   - Select your repository: `trinity-get-2`

3. **Configure the Service**
   - **Name**: `trinity-get-2-web` (or any name you prefer)
   - **Environment**: `Node`
   - **Region**: Choose closest to your users
   - **Branch**: `main` (or your default branch)
   - **Root Directory**: Leave empty (or set to `web` if deploying from root)
   - **Build Command**: `cd web && npm install && npm run build`
   - **Start Command**: `cd web && npm start`
   - **Plan**: `Free` (or choose a paid plan)

4. **Set Environment Variables**
   Click **"Advanced"** → **"Add Environment Variable"**:
   
   | Key | Value | Notes |
   |-----|-------|-------|
   | `NODE_ENV` | `production` | Required |
   | `AUTH_SECRET` | `<generate-random-string>` | Required - see below |
   | `JSONBIN_API_KEY` | `<your-key>` | Optional - for cloud storage |
   | `JSONBIN_BIN_ID` | `<your-bin-id>` | Optional - auto-created if not set |

   **Generate AUTH_SECRET:**
   ```bash
   # Mac/Linux:
   openssl rand -base64 32
   
   # Windows PowerShell:
   -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
   
   # Or use online: https://generate-secret.vercel.app/32
   ```

5. **Deploy**
   - Click **"Create Web Service"**
   - Render will start building (takes 5-10 minutes first time)
   - Watch the build logs for any errors

6. **Get Your URL**
   - Once deployed, you'll see: `https://trinity-get-2-web.onrender.com`
   - Click to open your live app!

#### Option B: Using Render Blueprint (render.yaml)

1. **Push code with render.yaml** (already created)

2. **Go to Render Dashboard**
   - Click **"New +"** → **"Blueprint"**
   - Connect GitHub and select `trinity-get-2`

3. **Render auto-detects render.yaml**
   - Review the configuration
   - Click **"Apply"**

4. **Set Environment Variables** (same as Option A)

5. **Deploy**
   - Click **"Create Blueprint"**
   - Render deploys automatically

## 🔧 Environment Variables Explained

### Required Variables

**`AUTH_SECRET`**
- Used for JWT token signing
- Must be at least 32 characters
- Keep it secret! Don't commit to git
- Generate: `openssl rand -base64 32`

**`NODE_ENV`**
- Set to `production` for production builds
- Enables optimizations

### Optional Variables

**`JSONBIN_API_KEY`**
- For cloud data storage
- Get from [jsonbin.io](https://jsonbin.io)
- If not set, uses local storage (resets on redeploy)

**`JSONBIN_BIN_ID`**
- Auto-created if not provided
- Can be set manually if you have an existing bin

## 📝 Important Notes

### Free Tier Limitations

- **750 hours/month** - Usually enough for small apps
- **Spins down after 15 minutes** of inactivity
- **First request after spin-down** takes ~30 seconds (cold start)
- **Auto-deploys** on git push (if enabled)

### Data Storage

**Without JSONBin.io:**
- Data stored in local file system
- **Resets on every redeploy**
- Good for testing, not production

**With JSONBin.io:**
- Data stored in cloud
- **Persists across redeploys**
- Recommended for production

**For Production:**
- Consider using Render PostgreSQL database
- Or use JSONBin.io (free tier: 10,000 requests/month)

## 🐛 Troubleshooting

### Build Fails

**"Module not found"**
- Check `package.json` has all dependencies
- Run `npm install` locally to verify

**"Build command failed"**
- Check build logs in Render dashboard
- Verify `npm run build` works locally
- Make sure you're in the `web` directory

### App Doesn't Start

**"Port already in use"**
- Render sets PORT automatically
- Don't hardcode port in code
- Next.js uses PORT env var automatically

**"AUTH_SECRET missing"**
- Make sure you set it in environment variables
- Generate a new one if needed

### Data Not Persisting

**Data resets on redeploy**
- You need JSONBin.io or a database
- Set `JSONBIN_API_KEY` environment variable
- Or use Render PostgreSQL

## ✅ Post-Deployment Checklist

- [ ] App loads at the URL
- [ ] Can sign up new account
- [ ] Can log in
- [ ] Can access dashboard
- [ ] Can create employee
- [ ] Can create task
- [ ] Can mark attendance
- [ ] Data persists after refresh

## 🔒 Security Checklist

- [ ] Strong `AUTH_SECRET` set (32+ chars)
- [ ] HTTPS enabled (automatic on Render)
- [ ] `.env.local` not committed to git
- [ ] Environment variables set in Render
- [ ] Using JSONBin.io or database for production

## 📊 Monitoring

After deployment:
- **View logs**: Render dashboard → Your service → Logs
- **Monitor uptime**: Dashboard shows uptime stats
- **Set alerts**: Configure email alerts for downtime
- **View metrics**: See request counts, response times

## 🎉 Success!

Your app is now live! Share the URL with your team.

**Example URL:**
```
https://trinity-get-2-web.onrender.com
```

## 🆘 Need Help?

- **Render Docs**: [https://render.com/docs](https://render.com/docs)
- **Render Support**: [https://render.com/support](https://render.com/support)
- **Check logs**: Render dashboard → Your service → Logs
- **Common issues**: See troubleshooting section above

---

**Happy Deploying! 🚀**

