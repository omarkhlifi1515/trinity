# 🚀 Quick Deploy to Render

## 1. Push to GitHub
```bash
git add .
git commit -m "Ready for Render"
git push origin main
```

## 2. Deploy on Render

### Via Dashboard:
1. Go to [render.com](https://render.com)
2. Click "New +" → "Web Service"
3. Connect GitHub repo: `trinity-get-2`
4. Configure:
   - **Name**: `trinity-get-2-web`
   - **Build Command**: `cd web && npm install && npm run build`
   - **Start Command**: `cd web && npm start`
5. Add Environment Variables:
   ```
   AUTH_SECRET=<generate-random-32-chars>
   NODE_ENV=production
   ```
6. Click "Create Web Service"
7. Wait 5-10 minutes ⏳

### Via Blueprint (render.yaml):
1. Go to [render.com](https://render.com)
2. Click "New +" → "Blueprint"
3. Connect GitHub repo: `trinity-get-2`
4. Review settings → Click "Apply"
5. Add Environment Variables (same as above)
6. Deploy! 🎉

## 3. Generate AUTH_SECRET

**Mac/Linux:**
```bash
openssl rand -base64 32
```

**Windows (PowerShell):**
```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
```

**Online:**
Visit: https://generate-secret.vercel.app/32

## 4. REQUIRED: JSONBin.io Setup (for persistent data)

**⚠️ IMPORTANT:** Without JSONBin.io, data resets on every redeploy!

1. **Get API key from [jsonbin.io](https://jsonbin.io)**
   - Sign up/login
   - Go to API Keys → Copy Master Key

2. **Add to Render environment variables:**
   ```
   JSONBIN_API_KEY=your-master-key-here
   ```
   
   **See `JSONBIN_SETUP_FOR_RENDER.md` for detailed instructions!**

## ✅ Done!

Your app will be live at: `https://your-app-name.onrender.com`

**Note**: Free tier spins down after 15 min inactivity. First request takes ~30 seconds.

