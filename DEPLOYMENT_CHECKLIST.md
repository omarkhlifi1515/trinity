# ✅ Render Deployment Checklist

## Before Deploying

- [ ] Code is pushed to GitHub
- [ ] All tests pass locally (`npm run build` works)
- [ ] Environment variables documented
- [ ] `.env.local` is NOT committed to git

## Deployment Steps

### 1. Push to GitHub
```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

### 2. Create Render Account
- [ ] Sign up at [render.com](https://render.com)
- [ ] Connect GitHub account

### 3. Deploy Service

**Option A: Manual Setup**
- [ ] Click "New +" → "Web Service"
- [ ] Connect repository: `trinity-get-2`
- [ ] Set name: `trinity-get-2-web`
- [ ] Set build command: `cd web && npm install && npm run build`
- [ ] Set start command: `cd web && npm start`
- [ ] Choose plan: Free

**Option B: Blueprint (render.yaml)**
- [ ] Click "New +" → "Blueprint"
- [ ] Connect repository: `trinity-get-2`
- [ ] Review settings → Apply

### 4. Set Environment Variables

**Required:**
- [ ] `NODE_ENV` = `production`
- [ ] `AUTH_SECRET` = `<generate-random-32-chars>`

**REQUIRED (for persistent data):**
- [ ] `JSONBIN_API_KEY` = `<your-jsonbin-master-key>` ⚠️ **REQUIRED!**
- [ ] `JSONBIN_BIN_ID` = `<leave-empty-for-auto-create>`

**⚠️ Without JSONBIN_API_KEY, data resets on every redeploy!**
See `JSONBIN_SETUP_FOR_RENDER.md` for detailed setup.

### 5. Deploy
- [ ] Click "Create Web Service" or "Create Blueprint"
- [ ] Wait for build to complete (5-10 minutes)
- [ ] Check build logs for errors

### 6. Verify Deployment
- [ ] App loads at the URL
- [ ] Can sign up
- [ ] Can log in
- [ ] Dashboard works
- [ ] All pages accessible

## Post-Deployment

- [ ] Test all features
- [ ] Check logs for errors
- [ ] Set up monitoring/alerts
- [ ] Share URL with team
- [ ] Document the deployment URL

## Troubleshooting

If build fails:
- [ ] Check build logs
- [ ] Verify `npm run build` works locally
- [ ] Check environment variables are set

If app doesn't start:
- [ ] Check start logs
- [ ] Verify `AUTH_SECRET` is set
- [ ] Check PORT is not hardcoded

If data doesn't persist:
- [ ] Set `JSONBIN_API_KEY` for cloud storage
- [ ] Or use Render PostgreSQL database

---

**Ready to deploy? Follow `RENDER_DEPLOYMENT.md` for detailed steps!**

