# Troubleshooting JSONBin API Key Issues

## Problem: "⚠️ JSONBin API key not set. Using local storage."

### Solution 1: Restart Dev Server
**Most common fix!** Next.js only loads `.env` on startup.

1. **Stop the dev server** (Ctrl+C)
2. **Start it again:**
   ```bash
   npm run dev
   ```

### Solution 2: Check .env Format
Make sure your `.env` file looks exactly like this:

```env
JSONBIN_API_KEY=$2a$10$XtgiWhpdzGwCmy0M915kdu9zNMfZi41jHYYGbimNLgjSSBmpFdJKq
AUTH_SECRET=your-secret-key-min-32-chars-long
```

**Common mistakes:**
- ❌ Extra spaces: `JSONBIN_API_KEY = value` (should be no spaces around `=`)
- ❌ Quotes: `JSONBIN_API_KEY="value"` (quotes not needed)
- ❌ Wrong file: Make sure it's `.env` in the `web/` folder

### Solution 3: Verify File Location
The `.env` file must be in the `web/` directory:

```
trinity/
└── web/
    ├── .env    ← Must be here!
    ├── package.json
    └── ...
```

### Solution 4: Check API Key Format
Your JSONBin Master Key should:
- Start with `$2a$10$` or `$2b$10$`
- Be about 60 characters long
- Have no spaces or line breaks

### Solution 5: Test Environment Variable
Add this temporarily to see if it's loading:

**In `web/lib/storage/jsonbin.ts`, the debug logging is already added!**
Check the server console for: `✅ JSONBin API key loaded: ...`

### Solution 6: Clear Next.js Cache
Sometimes Next.js caches environment variables:

```bash
# Delete .next folder
rm -rf .next

# Or on Windows:
rmdir /s .next

# Then restart
npm run dev
```

### Solution 7: Check Server vs Client
The warning might appear in the browser console, but the API key only works on the server.

- ✅ Server-side (API routes): Can access `process.env.JSONBIN_API_KEY`
- ❌ Client-side (browser): Cannot access `process.env.JSONBIN_API_KEY`

If you see the warning in browser console, it's normal - the server still has access.

## Still Not Working?

1. **Verify API key is valid:**
   - Go to https://jsonbin.io/app/dashboard
   - Check your Master Key matches what's in `.env`

2. **Check for typos:**
   - Variable name must be exactly: `JSONBIN_API_KEY`
   - No typos, no extra characters

3. **Try a fresh start:**
   ```bash
   # Stop server
   # Delete .next folder
   # Restart
   npm run dev
   ```

## Quick Test

After restarting, you should see in the server console (not browser):
- ✅ `✅ Loaded X users from JSONBin` (if you have users)
- ✅ `✅ Created new JSONBin: ...` (on first use)
- ❌ `⚠️ JSONBin API key not set` (means it's not loading)

If you still see the warning after restarting, check the file format and location!

