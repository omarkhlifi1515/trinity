# Changed from .env.local to .env

## ✅ What Changed

All documentation and code now references `.env` instead of `.env.local`.

## 📝 What You Need to Do

If you have `.env.local`, you can:

1. **Rename it to `.env`:**
   ```bash
   cd web
   # On Windows PowerShell:
   Rename-Item .env.local .env
   
   # Or copy it:
   Copy-Item .env.local .env
   ```

2. **Or create a new `.env` file** with the same content:
   ```env
   JSONBIN_API_KEY=$2a$10$XtgiWhpdzGwCmy0M915kdu9zNMfZi41jHYYGbimNLgjSSBmpFdJKq
   AUTH_SECRET=your-secret-key-min-32-chars-long
   ```

## ⚠️ Important Note

**Next.js loads both files:**
- `.env.local` (higher priority - loaded first)
- `.env` (lower priority)

If both files exist, `.env.local` will take precedence. So:
- ✅ If you only have `.env.local` → Rename it to `.env`
- ✅ If you have both → Delete `.env.local` to use `.env`
- ✅ If you only have `.env` → You're all set!

## 🚀 After Changing

1. **Restart your dev server:**
   ```bash
   # Stop server (Ctrl+C)
   npm run dev
   ```

2. **Check the console:**
   - You should see: `✅ JSONBin API key loaded: ...`

That's it! Everything now uses `.env` instead of `.env.local`.

