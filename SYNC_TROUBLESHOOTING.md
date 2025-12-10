# Data Sync Troubleshooting Guide

## Problem: Data Not Syncing Between Web and Kotlin Apps

### Step 1: Verify Supabase Configuration

#### Web App:
1. Check `web/.env` file exists and has:
   ```
   NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
   NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key-here
   ```
2. **Restart Next.js dev server** after changing `.env`
3. Check browser console for Supabase warnings

#### Kotlin App:
1. Open `mobile-kotlin/app/src/main/java/com/trinity/hrm/MainActivity.kt`
2. Verify credentials are set (NOT "YOUR_SUPABASE_URL"):
   ```kotlin
   val supabaseUrl = "https://your-project.supabase.co"
   val supabaseAnonKey = "your-anon-key-here"
   ```
3. Rebuild the app after changing credentials

### Step 2: Verify Database Schema

Run in Supabase SQL Editor:
```sql
-- Check if tables exist
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('employees', 'tasks', 'leaves', 'messages', 'attendance', 'departments');
```

All 6 tables should exist. If not, run the schema parts again.

### Step 3: Check Data in Supabase

1. Go to Supabase Dashboard → **Table Editor**
2. Check each table:
   - `employees` - Should show employees
   - `tasks` - Should show tasks
   - `leaves` - Should show leave requests
   - `messages` - Should show messages
   - `attendance` - Should show attendance records
   - `departments` - Should show departments

### Step 4: Test Sync Manually

1. **In Web App:**
   - Add an employee
   - Check Supabase Table Editor → `employees` table
   - Does it appear? ✅ If yes, web app is writing to Supabase
   - ❌ If no, web app is using local storage

2. **In Kotlin App:**
   - Add a message
   - Check Supabase Table Editor → `messages` table
   - Does it appear? ✅ If yes, Kotlin app is writing to Supabase
   - ❌ If no, Kotlin app is using local storage

3. **Cross-Check:**
   - Data added in web → Should appear in Kotlin after refresh
   - Data added in Kotlin → Should appear in web after refresh

### Step 5: Check for Errors

#### Web App Console:
- Look for: `⚠️ Supabase environment variables not set`
- Look for: `Error fetching employees:` or similar
- Check Network tab → Are Supabase requests being made?

#### Kotlin App Logcat:
- Look for: `⚠️ Supabase credentials not configured`
- Look for: `Error fetching employees:` or similar
- Check if `SupabaseClient.isInitialized()` returns true

### Step 6: Verify Both Apps Use Same Project

**Critical:** Both apps MUST use the same Supabase project!

1. Check web `.env`: `NEXT_PUBLIC_SUPABASE_URL=...`
2. Check Kotlin `MainActivity.kt`: `val supabaseUrl = "..."`
3. They should match exactly!

### Step 7: Check RLS Policies

If RLS is blocking access:
1. Go to Supabase Dashboard → **Authentication** → **Policies**
2. Or run in SQL Editor:
   ```sql
   SELECT * FROM pg_policies WHERE tablename IN ('employees', 'tasks', 'leaves', 'messages', 'attendance');
   ```
3. All policies should have `USING (true)` (allows all) if you ran Part 3

### Step 8: Force Refresh

**Web App:**
- Hard refresh: Ctrl+Shift+R (Windows) or Cmd+Shift+R (Mac)
- Clear browser cache
- Restart dev server

**Kotlin App:**
- Force stop the app
- Clear app data
- Rebuild and reinstall

## Quick Fix Checklist

- [ ] Web `.env` has correct Supabase credentials
- [ ] Web dev server restarted after `.env` change
- [ ] Kotlin `MainActivity.kt` has correct credentials (not placeholders)
- [ ] Kotlin app rebuilt after credential change
- [ ] Both apps use the SAME Supabase project URL
- [ ] All 4 schema parts run successfully
- [ ] Tables exist in Supabase dashboard
- [ ] RLS policies allow access (`USING (true)`)
- [ ] No errors in browser console / Logcat

## Still Not Working?

1. **Check Supabase Dashboard:**
   - Go to **Table Editor**
   - Manually add a row
   - See if it appears in both apps

2. **Test Direct Connection:**
   - Web: Open browser console → Type: `supabase.from('employees').select('*')`
   - Should return data from Supabase

3. **Check Network:**
   - Web: Network tab → Filter "supabase" → See if requests are made
   - Kotlin: Logcat → Filter "Supabase" → See if requests are made

4. **Verify Credentials:**
   - Double-check URL and key are correct
   - No extra spaces or quotes
   - Key starts with `eyJ...`

If all checks pass but still not syncing, the issue might be:
- Network/firewall blocking Supabase
- Supabase project paused (free tier)
- Rate limiting

