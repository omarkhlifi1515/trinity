# JSONBin.io Setup (FREE & EASY!)

JSONBin.io is a **completely free** JSON storage API - perfect for storing user data without any complex setup!

## ✅ Why JSONBin.io?

- ✅ **100% FREE** - No credit card needed
- ✅ **No OAuth** - Just an API key
- ✅ **Easy setup** - 2 minutes
- ✅ **10,000 requests/month** - More than enough for small apps
- ✅ **Unlimited bins** - Store as much data as you need

## 🚀 Quick Setup (2 minutes)

### Step 1: Get Your Free API Key

1. Go to [https://jsonbin.io/](https://jsonbin.io/)
2. Click **"Sign Up"** (or "Login" if you have an account)
3. Sign up with:
   - Email
   - Or GitHub (easiest!)
4. Once logged in, go to **"API Keys"** section
5. Copy your **"Master Key"** (it looks like: `$2b$10$...`)

### Step 2: Add API Key to Your App

Create or edit `.env.local` file in the `web` folder:

```env
JSONBIN_API_KEY=your_master_key_here
```

**Example:**
```env
JSONBIN_API_KEY=$2b$10$abcdefghijklmnopqrstuvwxyz1234567890
```

### Step 3: Restart Your Dev Server

```bash
npm run dev
```

That's it! 🎉

## 📝 How It Works

- When you sign up/login, user data is automatically saved to JSONBin.io
- Data is stored securely in the cloud
- The app automatically creates a "bin" (storage container) on first use
- If JSONBin is unavailable, it falls back to local file storage

## 🔒 Security

- Your API key is stored in `.env.local` (not committed to git)
- Data is stored privately (only accessible with your API key)
- API key is server-side only (never exposed to browser)

## 📊 Free Tier Limits

- **10,000 requests/month** - Perfect for small apps
- **Unlimited bins** - Store as much data as you need
- **No expiration** - Free forever!

## 🆘 Troubleshooting

### "JSONBin API key not set"
- Make sure you created `.env.local` file
- Check that `JSONBIN_API_KEY` is set correctly
- Restart your dev server after adding the key

### "Failed to create bin"
- Check your API key is correct
- Make sure you're logged into JSONBin.io
- Try regenerating your API key

### Still having issues?
- The app will automatically fall back to local storage (`data/users.json`)
- Your data will still be saved locally if JSONBin fails

## 🎯 Alternative: Use Local Storage Only

If you don't want to use JSONBin, just don't set the `JSONBIN_API_KEY`. The app will automatically use local file storage instead.

## 📚 More Info

- JSONBin.io Docs: [https://jsonbin.io/api-reference](https://jsonbin.io/api-reference)
- Free tier details: [https://jsonbin.io/pricing](https://jsonbin.io/pricing)

