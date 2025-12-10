# Kotlin Supabase Compilation Fixes

## Issues Fixed

1. ✅ **HttpClient type mismatch** - Changed to `Android.create()`
2. ✅ **onConflict unresolved** - Changed to delete + insert for attendance
3. ✅ **channel/on unresolved** - Simplified real-time subscriptions (can be added later)
4. ✅ **String? vs String** - Fixed Message `to` field null handling
5. ✅ **createdAt missing** - Fixed Attendance mapping (model doesn't have createdAt)

## Remaining Issues

The Supabase Kotlin SDK filter syntax might need adjustment. If you still get errors about `eq()` or `Column()`, try:

### Option 1: Use string-based filters
```kotlin
supabase.from("leaves")
    .update(mapOf("status" to "approved")) {
        filter("id", "eq", leaveId)
    }
```

### Option 2: Check SDK version
Make sure you're using the correct Supabase Kotlin SDK version. The API might have changed.

### Option 3: Simplify updates
For now, you can fetch, modify, and re-insert:
```kotlin
val leaves = supabase.from("leaves").select().decodeList<LeaveSupabase>()
val leave = leaves.find { it.id == leaveId }
if (leave != null) {
    supabase.from("leaves")
        .update(leave.copy(status = "approved", approved_by = approverId))
}
```

## Quick Fix

If compilation still fails, temporarily comment out the Supabase calls and use local storage only. The app will work with local storage until Supabase is properly configured.

