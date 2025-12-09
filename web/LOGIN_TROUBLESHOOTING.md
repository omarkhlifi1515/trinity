# Login Redirect Loop - Troubleshooting Guide

If you're experiencing a login redirect loop (keeps returning to login page), follow these steps:

## Step 1: Verify Your Supabase Anon Key

⚠️ **CRITICAL**: The key you provided (`sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx`) looks like a **publishable key**, not the **anon key**.

### How to get the correct anon key:

1. Go to https://supabase.com/dashboard
2. Select your project
3. Go to **Settings** → **API**
4. Look for **Project API keys**
5. Copy the **`anon` `public`** key (NOT the publishable key)
   - The anon key is a JWT token that starts with `eyJ...`
   - It's much longer than the publishable key

### Update your `.env.local`:

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (your actual anon key)
```

## Step 2: Restart Your Dev Server

After updating `.env.local`:
```bash
# Stop the server (Ctrl+C)
# Then restart
npm run dev
```

## Step 3: Clear Browser Cookies

1. Open browser DevTools (F12)
2. Go to **Application** tab → **Cookies**
3. Delete all cookies for `localhost:3000`
4. Refresh the page

## Step 4: Check Browser Console

Open browser console (F12) and look for errors:
- If you see "Missing Supabase environment variables" → Check Step 1
- If you see CORS errors → Check Supabase project settings
- If you see auth errors → Check Supabase Auth settings

## Step 5: Verify Supabase Auth Settings

1. Go to Supabase Dashboard → **Authentication** → **Settings**
2. Check **Site URL**: Should be `http://localhost:3000` (for dev)
3. Check **Redirect URLs**: Should include `http://localhost:3000/**`
4. Make sure **Email Auth** is enabled

## Step 6: Test Authentication Directly

In browser console, test if Supabase is working:

```javascript
// Check if Supabase client is initialized
const { createClient } = require('@supabase/supabase-js')
const supabase = createClient(
  'https://nghwpwajcoofbgvsevgf.supabase.co',
  'YOUR_ANON_KEY_HERE'
)

// Try to sign in
supabase.auth.signInWithPassword({
  email: 'test@example.com',
  password: 'test123456'
}).then(console.log)
```

## Common Issues

### Issue: "Missing Supabase environment variables"
**Solution**: Make sure `.env.local` exists in the `web` directory and has correct variable names starting with `NEXT_PUBLIC_`

### Issue: Login works but redirects back to login
**Solution**: 
1. Check middleware.ts exists (it should)
2. Verify anon key is correct (JWT format)
3. Clear cookies and try again

### Issue: "Invalid API key"
**Solution**: You're using the wrong key. Get the anon key from Supabase dashboard (starts with `eyJ`)

### Issue: CORS errors
**Solution**: Add your domain to Supabase project settings → Authentication → URL Configuration

## Still Not Working?

1. Check Supabase logs: Dashboard → **Logs** → **Auth Logs**
2. Check Next.js logs in terminal
3. Verify middleware.ts is being executed (add console.log temporarily)
4. Make sure you've run the SQL schema (`supabase-schema.sql`)

