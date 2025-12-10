# Quick Setup for Local Development

## Step 1: Install Dependencies
```bash
cd web
npm install
```

## Step 2: Create Environment File
Create `web/.env` with:
```env
JSONBIN_API_KEY=your_jsonbin_master_key_here
AUTH_SECRET=your-secret-key-min-32-chars-long
```

## Step 3: Get JSONBin API Key
1. Go to https://jsonbin.io/
2. Sign up (free)
3. Get your **Master Key** from dashboard
4. Paste it in `.env` as `JSONBIN_API_KEY`

## Step 4: Run
```bash
npm run dev
```

Open http://localhost:3000

## That's It! 🎉

The app will:
- ✅ Automatically create a JSONBin bin on first use
- ✅ Store all data in JSONBin.io cloud
- ✅ Share data with mobile apps (React Native & Kotlin)
- ✅ Work completely offline with local fallback

## Notes

- **No database needed** - JSONBin.io handles everything
- **Free tier** - 10,000 requests/month
- **Shared data** - All three apps use the same database
- **Auto-sync** - Data syncs automatically to cloud

## Troubleshooting

**"JSONBin API key not set"**
- Make sure `.env` exists in `web/` folder
- Check the key is correct (no extra spaces)
- Restart dev server after changing `.env`

**"Failed to create bin"**
- Check internet connection
- Verify API key is valid
- Check browser console for errors

