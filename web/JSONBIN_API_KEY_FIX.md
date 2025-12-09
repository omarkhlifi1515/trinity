# JSONBin.io API Key Issue - Fix Guide

## 🔍 Problem

You're seeing this error:
```
Failed to create bin: 401 Unauthorized
Error details: {"message":"Invalid X-Master-Key provided"}
```

## ✅ Solution

The API key format might be incorrect. JSONBin.io API keys should look like this:

**Correct format examples:**
- `$2b$10$...` (bcrypt hash format - 60 characters)
- Or a simple string key from JSONBin.io dashboard

## 🔧 Steps to Fix

### Step 1: Verify Your API Key

1. Go to [JSONBin.io Dashboard](https://jsonbin.io/app/dashboard)
2. Click on **"API Keys"** section
3. Find your **"Master Key"** (it should be a long string)
4. Copy it exactly as shown

### Step 2: Check Your `.env.local` File

Make sure your `.env.local` file in `trinity-get-2/web/` has:

```env
JSONBIN_API_KEY=your_actual_api_key_here
```

**Important:**
- No spaces around the `=` sign
- No quotes around the key
- Copy the key exactly from JSONBin.io dashboard
- The key should be at least 20 characters long

### Step 3: Common Issues

**Issue 1: Wrong Key Type**
- Make sure you're using the **"Master Key"** not the "Access Key"
- Master Key starts with `$2b$10$` or similar

**Issue 2: Extra Spaces**
- Check for leading/trailing spaces
- The code now trims whitespace automatically

**Issue 3: Key Not Loaded**
- Restart your dev server after updating `.env.local`
- Check terminal shows: `- Environments: .env.local`

### Step 4: Test Again

1. Restart server: `npm run dev`
2. Sign up a new user
3. Check terminal for:
   - `✅ Created new JSONBin: [id]` (success!)
   - OR `Failed to create bin: 401` (still wrong key)

## 🆘 Still Not Working?

If you're still getting "Invalid X-Master-Key":

1. **Regenerate API Key**:
   - Go to JSONBin.io dashboard
   - Delete old key
   - Create new Master Key
   - Update `.env.local` with new key

2. **Check Key Format**:
   - Master Key should be ~60 characters
   - Usually starts with `$2b$10$` or `$2a$10$`
   - No special characters except `$`, `/`, `.`, `+`

3. **Verify Account**:
   - Make sure you're logged into JSONBin.io
   - Check your account is active
   - Free tier should work fine

## 📝 Alternative: Use Local Storage

If JSONBin.io continues to have issues, the app will automatically fall back to local file storage (`data/users.json`). Your data will still be saved, just locally instead of in the cloud.

---

**The code has been updated to trim whitespace and validate the API key format automatically.**

