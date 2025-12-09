# JSONBin.io Setup for Render Deployment

## 🎯 Why JSONBin.io is Required

On Render, your app's file system is **ephemeral** - it resets on every redeploy. This means:
- ❌ Local file storage (`data/users.json`) gets wiped on redeploy
- ✅ JSONBin.io stores data in the cloud, persisting across redeploys
- ✅ **Required for production** to keep user data safe

## 🚀 Quick Setup (5 minutes)

### Step 1: Get Your JSONBin.io API Key

1. **Go to JSONBin.io**
   - Visit [https://jsonbin.io/app/dashboard](https://jsonbin.io/app/dashboard)
   - Sign up or log in (free account works!)

2. **Get Your Master Key**
   - Click on **"API Keys"** in the sidebar
   - Find your **"Master Key"** (looks like: `$2b$10$...`)
   - Click **"Copy"** to copy it

   **Example format:**
   ```
   $2b$10$hcncicE/yq1JtZDx/2CK9uR8tyncPU8gOhbsA9oTUD/Kw1euZE0x2
   ```

### Step 2: Add to Render Environment Variables

1. **Go to Render Dashboard**
   - Open your service: `trinity-get-2-web`
   - Click **"Environment"** tab

2. **Add JSONBIN_API_KEY**
   - Click **"Add Environment Variable"**
   - **Key**: `JSONBIN_API_KEY`
   - **Value**: Paste your Master Key (the one you copied)
   - Click **"Save Changes"**

3. **Optional: Set JSONBIN_BIN_ID**
   - Leave empty for auto-create (recommended)
   - Or set manually if you have an existing bin ID

### Step 3: Redeploy (if already deployed)

- Render will auto-redeploy when you save environment variables
- Or click **"Manual Deploy"** → **"Deploy latest commit"**

## ✅ Verify It's Working

After deployment:

1. **Sign up a new user** on your Render app
2. **Check JSONBin.io Dashboard**
   - Go to [jsonbin.io/app/dashboard](https://jsonbin.io/app/dashboard)
   - Click **"My Bins"**
   - You should see a bin named **"Trinity HRM Users"**
   - Click it to see your user data

3. **Test Persistence**
   - Create a user
   - Redeploy your app (or wait for auto-deploy)
   - Log in again - your user should still exist! ✅

## 🔍 Troubleshooting

### "Invalid X-Master-Key provided"

**Problem:** API key format is wrong

**Solution:**
1. Make sure you copied the **Master Key**, not Access Key
2. Check for extra spaces (trim whitespace)
3. Key should be ~60 characters, starts with `$2b$10$` or `$2a$10$`
4. Regenerate key in JSONBin.io if needed

### "Failed to create bin"

**Problem:** API key doesn't have permission

**Solution:**
1. Verify you're using the **Master Key** (not Access Key)
2. Check your JSONBin.io account is active
3. Try regenerating the API key

### Data Still Resets

**Problem:** JSONBIN_API_KEY not set correctly

**Solution:**
1. Check Render dashboard → Environment → `JSONBIN_API_KEY` is set
2. Check logs for: `✅ Created new JSONBin:` or `✅ Loaded users from JSONBin`
3. If you see `✅ Saved users to local file`, JSONBin is not working
4. Verify API key format is correct

## 📝 Environment Variables Summary

**In Render Dashboard → Environment:**

| Variable | Value | Required |
|----------|-------|----------|
| `NODE_ENV` | `production` | ✅ Yes |
| `AUTH_SECRET` | `<random-32-chars>` | ✅ Yes |
| `JSONBIN_API_KEY` | `<your-master-key>` | ✅ **YES - Required!** |
| `JSONBIN_BIN_ID` | `<leave-empty>` | ❌ Optional |

## 🎉 Success!

Once `JSONBIN_API_KEY` is set:
- ✅ User data persists across redeploys
- ✅ Data stored securely in JSONBin.io cloud
- ✅ Free tier: 10,000 requests/month (plenty for small apps)
- ✅ No database setup needed!

## 📚 More Info

- **JSONBin.io Docs**: [https://jsonbin.io/api-reference](https://jsonbin.io/api-reference)
- **Free Tier**: [https://jsonbin.io/pricing](https://jsonbin.io/pricing)
- **Get API Key**: [https://jsonbin.io/app/dashboard](https://jsonbin.io/app/dashboard)

---

**Set JSONBIN_API_KEY in Render → Your data will persist! 🎯**

