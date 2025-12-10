# Supabase Sync Setup Guide

To enable data sync between web and Kotlin apps, you need to configure Supabase credentials in both apps.

## Step 1: Get Your Supabase Credentials

1. Go to your Supabase project dashboard: https://supabase.com/dashboard
2. Select your project
3. Go to **Settings** → **API**
4. Copy:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **anon/public key** (starts with `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`)

## Step 2: Configure Web App

1. Open `web/.env` file (create it if it doesn't exist)
2. Add these lines:
   ```
   NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
   NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key-here
   ```
3. Replace with your actual values
4. **Restart the Next.js dev server** (important!)

## Step 3: Configure Kotlin App

1. Open `mobile-kotlin/app/src/main/java/com/trinity/hrm/MainActivity.kt`
2. Find these lines (around line 24-25):
   ```kotlin
   val supabaseUrl = "YOUR_SUPABASE_URL"
   val supabaseAnonKey = "YOUR_SUPABASE_ANON_KEY"
   ```
3. Replace with your actual values:
   ```kotlin
   val supabaseUrl = "https://your-project.supabase.co"
   val supabaseAnonKey = "your-anon-key-here"
   ```
4. Rebuild the app

## Step 4: Verify Database Schema

Make sure you've run all 4 parts of the schema:
1. ✅ `schema-part1-tables.sql` - Creates tables
2. ✅ `schema-part2-indexes-safe.sql` - Creates indexes
3. ✅ `schema-part3-rls.sql` - Sets up security
4. ✅ `schema-part4-triggers.sql` - Creates triggers

## Step 5: Test Sync

1. **In Web App:**
   - Add an employee
   - Create a task
   - Request a leave

2. **In Kotlin App:**
   - Open the app
   - Check if the data appears (should sync automatically)

3. **In Kotlin App:**
   - Add a message
   - Mark attendance

4. **In Web App:**
   - Check if the message and attendance appear

## Troubleshooting

### Web App Not Syncing
- ✅ Check `web/.env` has correct credentials
- ✅ Restart Next.js dev server after changing `.env`
- ✅ Check browser console for Supabase errors
- ✅ Verify tables exist in Supabase dashboard

### Kotlin App Not Syncing
- ✅ Check `MainActivity.kt` has correct credentials (not "YOUR_SUPABASE_URL")
- ✅ Rebuild the app after changing credentials
- ✅ Check Logcat for Supabase errors
- ✅ Verify SupabaseClient.isInitialized() returns true

### Data Not Appearing
- ✅ Check Supabase dashboard → Table Editor → See if data exists
- ✅ Verify RLS policies allow access (Part 3 should have set `USING (true)`)
- ✅ Check both apps are using the same Supabase project

### Still Using Local Storage
- ✅ Web: Check console for "⚠️ Supabase environment variables not set"
- ✅ Kotlin: Check Logcat for "⚠️ Supabase credentials not configured"
- ✅ Both apps will use local storage if Supabase isn't configured

## Quick Check

Run this in Supabase SQL Editor to see if data exists:
```sql
SELECT COUNT(*) FROM employees;
SELECT COUNT(*) FROM tasks;
SELECT COUNT(*) FROM leaves;
SELECT COUNT(*) FROM messages;
```

If counts are > 0, data is in Supabase and should sync!

