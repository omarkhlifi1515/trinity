# How to Get Your Supabase API Key - STEP BY STEP

## Step 1: Go to Supabase Dashboard

1. Open your browser
2. Go to: **https://supabase.com/dashboard**
3. Log in with your Supabase account

## Step 2: Select Your Project

1. You should see your project: **nghwpwajcoofbgvsevgf** (or similar)
2. Click on it to open the project

## Step 3: Navigate to API Settings

1. Look at the left sidebar
2. Click on **"Settings"** (gear icon at the bottom)
3. Click on **"API"** in the settings menu

## Step 4: Find Your API Keys

You'll see a section called **"Project API keys"** with several keys:

### What You Need:

1. **Project URL**: 
   - Should be: `https://nghwpwajcoofbgvsevgf.supabase.co`
   - Copy this entire URL

2. **anon public key**:
   - This is the IMPORTANT one!
   - It's a LONG string that starts with `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - It's NOT the one that says "publishable" or starts with "sb_"
   - Click the "eye" icon to reveal it, then click "Copy"

## Step 5: Create .env.local File

1. Go to your project folder: `trinity-get-2/web/`
2. Create a new file named: `.env.local`
3. Paste this content:

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=paste_your_anon_key_here
```

4. Replace `paste_your_anon_key_here` with the actual anon key you copied

## Step 6: Verify Your File

Your `.env.local` should look like this (with your actual key):

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5naHdwd2FqY29vZmJndnNldmdmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3M... (very long)
```

## Step 7: Restart Your Server

1. Stop your dev server (press Ctrl+C in terminal)
2. Start it again: `npm run dev`

## Step 8: Test

1. Go to http://localhost:3000
2. Try to sign up with a new account
3. Check browser console (F12) for any errors

## Visual Guide

```
Supabase Dashboard
├── Your Project (nghwpwajcoofbgvsevgf)
    └── Settings (gear icon)
        └── API
            └── Project API keys
                ├── Project URL: https://nghwpwajcoofbgvsevgf.supabase.co
                └── anon public: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... ← COPY THIS
```

## Still Having Issues?

If you can't find the API settings:
1. Make sure you're logged into Supabase
2. Make sure you're in the correct project
3. The API settings are always in: Settings → API

If the key doesn't work:
1. Make sure you copied the ENTIRE key (it's very long)
2. Make sure it starts with `eyJ`
3. Make sure there are no extra spaces

