# QUICK FIX - Get Your API Key Now

## The Problem
Your app needs a Supabase API key to work. You need to get it from Supabase and create a `.env.local` file.

## Step-by-Step Fix (5 minutes)

### Step 1: Get Your API Key

1. **Open this link**: https://supabase.com/dashboard/project/nghwpwajcoofbgvsevgf/settings/api
   - (This goes directly to your project's API settings)

2. **Find the "Project API keys" section**

3. **Copy the "anon public" key**:
   - It's a LONG string starting with `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - Click the "eye" icon to reveal it
   - Click "Copy" button
   - **IMPORTANT**: It's NOT the "publishable" key that starts with `sb_`

### Step 2: Create .env.local File

1. **Go to your project folder**: `trinity-get-2/web/`

2. **Create a new file** named exactly: `.env.local`
   - Make sure it starts with a dot (.)
   - No spaces in the filename

3. **Paste this content** (replace `YOUR_KEY_HERE` with the key you copied):

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=YOUR_KEY_HERE
```

### Step 3: Restart Server

1. **Stop your server**: Press `Ctrl+C` in the terminal
2. **Start it again**: Run `npm run dev`

### Step 4: Test

1. Go to http://localhost:3000
2. The login page should now show if there are any config errors
3. Try signing up with a new account

## Visual Example

Your `.env.local` file should look like this:

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5naHdwd2FqY29vZmJndnNldmdmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ1Njg3ODAsImV4cCI6MjA1MDE0NDc4MH0.abc123xyz789verylongkeycontinues...
```

## Still Not Working?

1. **Check the file location**: Must be in `trinity-get-2/web/.env.local`
2. **Check the key**: Must start with `eyJ` and be very long
3. **Check browser console**: Press F12, look for error messages
4. **Restart server**: Always restart after creating/editing `.env.local`

## Direct Link to Get Key

👉 **https://supabase.com/dashboard/project/nghwpwajcoofbgvsevgf/settings/api**

Scroll down to "Project API keys" and copy the "anon public" key.

