# Supabase Configuration Check

## Quick Verification Steps

### 1. Check Web App Configuration

Open `web/.env` and verify:
```
NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key-here
```

**Important:** After changing `.env`, restart the Next.js dev server!

### 2. Check Kotlin App Configuration

Open `mobile-kotlin/app/src/main/java/com/trinity/hrm/MainActivity.kt` and verify:
```kotlin
val supabaseUrl = "https://your-project.supabase.co"  // NOT "YOUR_SUPABASE_URL"
val supabaseAnonKey = "your-anon-key-here"  // NOT "YOUR_SUPABASE_ANON_KEY"
```

### 3. Test Connection

**Web App:**
1. Open browser console
2. Look for: `✅ Loaded users from Supabase` (not warnings)
3. If you see warnings, Supabase is not configured

**Kotlin App:**
1. Check Logcat
2. Look for: `⚠️ Supabase credentials not configured` (should NOT appear)
3. If it appears, credentials are not set

### 4. Verify Database

Run in Supabase SQL Editor:
```sql
SELECT COUNT(*) as employee_count FROM employees;
SELECT COUNT(*) as task_count FROM tasks;
SELECT COUNT(*) as leave_count FROM leaves;
SELECT COUNT(*) as message_count FROM messages;
```

If all return 0, no data has been synced yet.

### 5. Test Sync

1. **Add data in web app** → Check Supabase dashboard → Should appear
2. **Add data in Kotlin app** → Check Supabase dashboard → Should appear
3. **Refresh web app** → Should see Kotlin data
4. **Refresh Kotlin app** → Should see web data

## Common Issues

### Issue: "Supabase environment variables not set"
**Solution:** Add credentials to `web/.env` and restart dev server

### Issue: "Supabase credentials not configured" (Kotlin)
**Solution:** Update `MainActivity.kt` with real credentials

### Issue: Data not syncing
**Solution:** 
- Verify both apps use the SAME Supabase project
- Check RLS policies allow access (Part 3 should have `USING (true)`)
- Check browser console / Logcat for errors

### Issue: Tables don't exist
**Solution:** Run all 4 schema parts in Supabase SQL Editor

