# Mobile Apps Standalone Setup

## ✅ What Changed

Both mobile apps (React Native and Kotlin) now work **independently** - they don't need to connect to the web app!

All three apps share the **same JSONBin.io database** for data:
- ✅ Web app (Next.js)
- ✅ React Native app
- ✅ Kotlin app (Android)

## 🔧 Setup Instructions

### React Native App (`mobile/`)

1. **Create `.env` file** in `mobile/` directory:
   ```
   EXPO_PUBLIC_JSONBIN_API_KEY=$2a$10$XtgiWhpdzGwCmy0M915kdu9zNMfZi41jHYYGbimNLgjSSBmpFdJKq
   ```

2. **Restart Expo dev server:**
   ```bash
   cd mobile
   npm start
   ```

3. **That's it!** The app now uses JSONBin.io directly.

### Kotlin App (`mobile-kotlin/`)

1. **Update API Key** in `JsonBinClient.kt`:
   - File: `mobile-kotlin/app/src/main/java/com/trinity/hrm/data/remote/JsonBinClient.kt`
   - Line 15: Update `API_KEY` constant with your JSONBin master key
   - Or use BuildConfig to set it dynamically

2. **Sync Gradle** in Android Studio

3. **Build and run** - The app now uses JSONBin.io directly!

## 📊 Shared Database

All three apps use the **same JSONBin.io bin**:
- Same API key: `$2a$10$XtgiWhpdzGwCmy0M915kdu9zNMfZi41jHYYGbimNLgjSSBmpFdJKq`
- Same bin ID: Auto-created on first use, then shared
- Users created in one app are visible in all apps!

## 🎯 How It Works

### React Native App
- Uses `mobile/lib/auth.ts` for authentication
- Uses `mobile/lib/jsonbin.ts` for JSONBin.io access
- Stores session in AsyncStorage
- No web app connection needed!

### Kotlin App
- Uses `LocalAuth.kt` for authentication
- Uses `JsonBinClient.kt` for JSONBin.io access
- Stores session in SharedPreferences
- No web app connection needed!

### Web App
- Uses `web/lib/auth/local-auth.ts` for authentication
- Uses `web/lib/storage/jsonbin.ts` for JSONBin.io access
- Stores session in cookies
- Works independently!

## 🔑 API Key Setup

### For React Native:
Set in `.env` file:
```
EXPO_PUBLIC_JSONBIN_API_KEY=your-master-key-here
```

### For Kotlin:
Update `JsonBinClient.kt`:
```kotlin
private const val API_KEY = "your-master-key-here"
```

### For Web:
Set in `.env.local`:
```
JSONBIN_API_KEY=your-master-key-here
```

## ✅ Benefits

1. **Independent Apps** - Each app works standalone
2. **Shared Data** - All apps see the same users/data
3. **No Server Needed** - Mobile apps don't need web app running
4. **Offline Support** - Apps cache data locally
5. **Easy Deployment** - Deploy each app separately

## 🧪 Testing

1. **Create user in web app** → Should appear in mobile apps
2. **Create user in React Native** → Should appear in web and Kotlin apps
3. **Create user in Kotlin** → Should appear in web and React Native apps

All three apps share the same database! 🎉

---

**All apps are now standalone but share the same JSONBin.io database!**

