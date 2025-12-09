# Setup Instructions - JSONBin.io API Key

## ✅ Step 1: Create `.env.local` File

Create a file named `.env.local` in the `trinity-get-2/web/` folder with this content:

```env
JSONBIN_API_KEY=$2a$10$hcncicE/yq1JtZDx/2CK9uR8tyncPU8gOhbsA9oTUD/Kw1euZE0x2
```

**Important:**
- File must be named exactly: `.env.local` (with the dot at the start)
- Location: `trinity-get-2/web/.env.local`
- No spaces around the `=` sign
- No quotes around the API key

## ✅ Step 2: Restart Your Dev Server

After creating the file, restart your Next.js dev server:

```bash
cd trinity-get-2/web
npm run dev
```

## ✅ Step 3: Test It

1. Go to `http://localhost:3000/signup`
2. Create a new test account
3. Check your terminal - you should see:
   ```
   ✅ Created new JSONBin: [bin-id]
   💾 Saved JSONBin ID to file: [bin-id]
   ✅ Saved users to JSONBin
   ```
4. Check [JSONBin.io Dashboard](https://jsonbin.io/app/dashboard) - you should see a bin named "Trinity HRM Users"

## 🎯 How All 3 Apps Share Data

### Web App
- ✅ Directly uses JSONBin.io API key
- ✅ Stores all user data in JSONBin.io

### Mobile Apps (React Native & Kotlin)
- ✅ Connect through Web App API
- ✅ Automatically see the same users
- ✅ No API key needed in mobile apps

## 📊 Data Flow

```
Mobile Apps → Web App API → JSONBin.io (Shared Storage)
```

All apps share the same user database!

## 🔍 Troubleshooting

### "JSONBin API key not set"
- Make sure `.env.local` exists in `trinity-get-2/web/`
- Check the file name is exactly `.env.local` (not `.env.local.txt`)
- Restart your dev server after creating the file

### "No bin created"
- Sign up a user first (bin is created on first user signup)
- Check terminal for error messages
- Verify API key is correct

### Still not working?
- Check terminal logs for specific error messages
- Verify API key at [JSONBin.io Dashboard](https://jsonbin.io/app/dashboard)
- The app will fall back to local storage if JSONBin fails

---

**Once set up, all 3 apps will share the same user data! 🎉**

