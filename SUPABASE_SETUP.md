# Supabase Setup Guide for Trinity HRM

## 🚀 Quick Setup Steps

### 1. Create Supabase Project

1. Go to [https://supabase.com](https://supabase.com)
2. Sign up / Log in
3. Click "New Project"
4. Fill in:
   - **Name**: Trinity HRM
   - **Database Password**: (choose a strong password)
   - **Region**: (choose closest to you)
5. Click "Create new project"
6. Wait for project to be created (~2 minutes)

### 2. Get Your Credentials

1. Go to **Settings** → **API**
2. Copy:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **anon/public key** (starts with `eyJ...`)

### 3. Set Up Database Schema

1. Go to **SQL Editor** in Supabase dashboard
2. Open `supabase/schema.sql` from this project
3. Copy the entire SQL file
4. Paste into SQL Editor
5. Click **Run** (or press Ctrl+Enter)
6. Wait for success message

### 4. Configure Web App

1. Open `web/.env` file
2. Add these lines:
```env
NEXT_PUBLIC_SUPABASE_URL=https://xxxxx.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

3. Restart your Next.js dev server:
```bash
cd web
npm run dev
```

### 5. Configure Kotlin App

1. In your Kotlin app, you need to set credentials programmatically or via BuildConfig
2. Update `MainActivity.kt`:
```kotlin
// In onCreate, before setContent:
SupabaseClient.setCredentials(
    url = "https://xxxxx.supabase.co",
    anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
)
SupabaseClient.initialize(this)
```

Or better, use BuildConfig:
1. Add to `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"https://xxxxx.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"")
    }
}
```

2. Then in code:
```kotlin
SupabaseClient.setCredentials(
    url = BuildConfig.SUPABASE_URL,
    anonKey = BuildConfig.SUPABASE_ANON_KEY
)
```

### 6. Enable Realtime (Important!)

1. Go to **Database** → **Replication** in Supabase dashboard
2. Enable replication for these tables:
   - ✅ `messages`
   - ✅ `leaves`
   - ✅ `tasks`
   - ✅ `employees`
   - ✅ `attendance`

### 7. Test the Setup

**Web App:**
1. Start dev server: `cd web && npm run dev`
2. Login and check if data loads
3. Send a message - it should appear instantly

**Kotlin App:**
1. Build and run the app
2. Login and check if data loads
3. Send a message from web app - it should appear in Kotlin app instantly!

## ✅ What You Get

- ⚡ **Real-time sync** - No more polling delays!
- 🔒 **Better security** - Row-level security policies
- 📊 **Better queries** - SQL filtering at database level
- 🚀 **Better performance** - Only fetch what you need
- 📱 **Cross-app sync** - Changes appear instantly in all apps

## 🐛 Troubleshooting

### "SupabaseClient not initialized"
- Make sure you called `SupabaseClient.initialize(context)` in MainActivity
- Check that credentials are set correctly

### "Row Level Security policy violation"
- Check that you're logged in
- Verify RLS policies are set up correctly
- Check user role in `auth.users.raw_user_meta_data`

### "Realtime not working"
- Make sure Realtime is enabled for the tables
- Check that you're subscribed to the channel
- Verify Supabase project is active (not paused)

### "Connection timeout"
- Check your internet connection
- Verify Supabase URL is correct
- Check if Supabase project is paused (free tier pauses after inactivity)

## 📚 Next Steps

1. ✅ Database schema is set up
2. ✅ Web app is configured
3. ✅ Kotlin app is configured
4. ⚠️ **Update DataStorage to use SupabaseStorage instead of InMemoryStorage**
5. ⚠️ **Add real-time subscriptions to screens**

See the migration guide for updating your code!

