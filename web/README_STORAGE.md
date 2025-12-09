# Storage Options

This app supports multiple storage options for user data:

## 🌐 Option 1: JSONBin.io (RECOMMENDED - FREE & EASY)

**Best for:** Quick setup, free cloud storage

- ✅ 100% FREE
- ✅ No OAuth setup needed
- ✅ Just need an API key
- ✅ 10,000 requests/month free

**Setup:** See `JSONBIN_SETUP.md`

## 💾 Option 2: Local File Storage (DEFAULT)

**Best for:** Development, offline use

- ✅ No setup needed
- ✅ Works immediately
- ✅ Data stored in `data/users.json`
- ❌ Not synced across devices

**Setup:** Nothing needed! Works by default.

## 📁 Option 3: Google Drive (Advanced)

**Best for:** If you want to use your Google Drive folder

- ✅ Uses your Google Drive
- ✅ Data synced to cloud
- ❌ Requires OAuth setup
- ❌ More complex setup

**Setup:** See `GOOGLE_DRIVE_SETUP.md`

## 🎯 Which Should I Use?

- **Just want it to work?** → Use **Local File Storage** (default)
- **Want free cloud storage?** → Use **JSONBin.io** (recommended)
- **Want to use Google Drive?** → Use **Google Drive** (advanced)

The app automatically chooses the best available option:
1. JSONBin.io (if API key is set)
2. Google Drive (if configured)
3. Local file storage (fallback)

