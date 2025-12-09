# Environment Setup Guide

## Step 1: Create .env.local file

Create a file named `.env.local` in the `web` directory with the following content:

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your_actual_anon_key_here
```

## Step 2: Get Your Anon Key

1. Go to https://supabase.com/dashboard
2. Select your project
3. Navigate to **Settings** → **API**
4. Find the **Project API keys** section
5. Copy the **`anon` `public`** key
   - This key starts with `eyJ` (it's a JWT token)
   - It's NOT the publishable key that starts with `sb_publishable_`

## Step 3: Update .env.local

Replace `your_actual_anon_key_here` with the actual anon key you copied.

## Step 4: Restart Your Dev Server

After creating/updating `.env.local`:

```bash
# Stop the server (Ctrl+C)
npm run dev
```

## Step 5: Clear Browser Data

1. Open browser DevTools (F12)
2. Go to **Application** → **Storage**
3. Click **Clear site data**
4. Or manually delete cookies for `localhost:3000`

## Verification

After setup, you should be able to:
1. Sign up a new account
2. Log in with your credentials
3. Be redirected to `/dashboard`

If you still have issues, check the browser console (F12) for any error messages.

