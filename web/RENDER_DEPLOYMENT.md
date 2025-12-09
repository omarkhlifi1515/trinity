# Deploy to Render - Step by Step Guide

This guide will help you deploy the Trinity HRM web app to Render **with JSONBin.io for persistent data storage**.

## 🚀 Quick Deploy (5 minutes)

### Option 1: Deploy from GitHub (Recommended)

1. **Push your code to GitHub**
   ```bash
   git add .
   git commit -m "Ready for Render deployment"
   git push origin main
   ```

2. **Go to Render Dashboard**
   - Visit [https://dashboard.render.com](https://dashboard.render.com)
   - Sign up or log in

3. **Create New Web Service**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Select the `trinity-get-2` repository

4. **Configure Service**
   - **Name**: `trinity-get-2-web`
   - **Environment**: `Node`
   - **Build Command**: `cd web && npm install && npm run build`
   - **Start Command**: `cd web && npm start`
   - **Plan**: Free (or choose paid plan)

5. **Set Environment Variables**
   Click "Advanced" → "Add Environment Variable" and add:
   
   **REQUIRED:**
   ```
   NODE_ENV=production
   AUTH_SECRET=<generate-a-secure-random-string>
   JSONBIN_API_KEY=<your-jsonbin-api-key-REQUIRED>
   ```
   
   **OPTIONAL:**
   ```
   JSONBIN_BIN_ID=<your-bin-id-or-leave-empty-for-auto-create>
   ```
   
   **⚠️ IMPORTANT:** JSONBIN_API_KEY is REQUIRED for data persistence on Render!
   Without it, data will reset on every redeploy.
   
   **See `JSONBIN_SETUP_FOR_RENDER.md` for detailed JSONBin.io setup!**

6. **Deploy**
   - Click "Create Web Service"
   - Render will automatically build and deploy
   - Wait 5-10 minutes for first deployment

7. **Get Your URL**
   - Once deployed, you'll get a URL like: `https://trinity-get-2-web.onrender.com`
   - Share this URL with your team!

### Option 2: Deploy using Render Blueprint

1. **Push code to GitHub** (same as above)

2. **Go to Render Dashboard**
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Select `trinity-get-2` repository

3. **Render will auto-detect `render.yaml`**
   - It will read the configuration automatically
   - Review the settings and click "Apply"

4. **Set Environment Variables** (same as Option 1)
   - **⚠️ Don't forget to set `JSONBIN_API_KEY`!**

5. **Deploy**
   - Click "Create Blueprint"
   - Render will deploy automatically

## 🔧 Environment Variables

### Required Variables

| Variable | Description | How to Get |
|----------|-------------|------------|
| `AUTH_SECRET` | Secret key for JWT tokens | Generate: `openssl rand -base64 32` |
| `NODE_ENV` | Environment mode | Set to `production` |
| `JSONBIN_API_KEY` | **REQUIRED** - JSONBin.io API key for data persistence | Get from [jsonbin.io](https://jsonbin.io/app/dashboard) → API Keys → Master Key |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JSONBIN_BIN_ID` | JSONBin.io bin ID | Auto-created on first use if not set |

## 📝 Important Notes

### 1. **Data Storage - JSONBin.io REQUIRED**
- **✅ With JSONBin.io**: Data stored in cloud, persists across redeploys (REQUIRED for production)
- **❌ Without JSONBin.io**: Data stored locally, RESETS on every redeploy (NOT recommended)
- **⚠️ IMPORTANT**: Set `JSONBIN_API_KEY` environment variable for persistent data storage
- **📖 Setup Guide**: See `JSONBIN_SETUP_FOR_RENDER.md` for step-by-step instructions

### 2. **Free Tier Limits**
- **Render Free Tier**:
  - 750 hours/month
  - Spins down after 15 minutes of inactivity
  - First request after spin-down takes ~30 seconds
- **JSONBin.io Free Tier**:
  - 10,000 requests/month
  - Unlimited bins
  - Perfect for small apps!

### 3. **Build Time**
- First build: ~5-10 minutes
- Subsequent builds: ~3-5 minutes

### 4. **Port Configuration**
- Render automatically sets `PORT` environment variable
- Next.js will use this automatically
- No need to configure manually

## 🐛 Troubleshooting

### Build Fails

**Error: "Module not found"**
- Make sure all dependencies are in `package.json`
- Run `npm install` locally to verify

**Error: "Build command failed"**
- Check build logs in Render dashboard
- Verify `npm run build` works locally

### App Doesn't Start

**Error: "Port already in use"**
- Render sets PORT automatically, don't override it
- Remove any hardcoded port in your code

**Error: "AUTH_SECRET missing"**
- Make sure you set `AUTH_SECRET` in environment variables
- Generate a new one if needed

### Data Not Persisting

**Data resets on redeploy**
- ⚠️ **You MUST set `JSONBIN_API_KEY` environment variable**
- Without it, data is stored locally and gets wiped on redeploy
- See `JSONBIN_SETUP_FOR_RENDER.md` for setup instructions
- Verify in logs: Should see `✅ Loaded users from JSONBin`

**Check logs for:**
- `✅ Loaded users from JSONBin` = Working! ✅
- `✅ Saved users to local file` = JSONBin not configured ❌

## 🔒 Security Checklist

- [ ] Set strong `AUTH_SECRET` (32+ characters)
- [ ] Use HTTPS (automatic on Render)
- [ ] Don't commit `.env.local` to git
- [ ] **Set `JSONBIN_API_KEY` for persistent data storage**
- [ ] Review environment variables before deploying

## 📊 Monitoring

After deployment, you can:
- View logs in Render dashboard
- Monitor uptime and performance
- Set up alerts for downtime
- View build history

## 🎉 Success!

Once deployed, your app will be available at:
```
https://your-app-name.onrender.com
```

**Test your deployment:**
1. Visit the URL
2. Sign up for a new account
3. Test login/logout
4. Create an employee
5. Create a task
6. Mark attendance
7. **Verify data persists** - Redeploy and check data is still there!

## 📚 JSONBin.io Setup

**⚠️ CRITICAL:** Set up JSONBin.io for data persistence!

See `JSONBIN_SETUP_FOR_RENDER.md` for:
- Step-by-step API key setup
- How to verify it's working
- Troubleshooting guide
- Free tier details

## 🆘 Need Help?

- Render Docs: [https://render.com/docs](https://render.com/docs)
- Render Support: [https://render.com/support](https://render.com/support)
- JSONBin.io Setup: See `JSONBIN_SETUP_FOR_RENDER.md`
- Check build logs in Render dashboard

---

**Happy Deploying! 🚀**

**Remember: Set JSONBIN_API_KEY for persistent data!**
