# Firebase Setup Instructions for Trinity HRM Mobile App

## 🔥 Firebase Migration Complete!

The Trinity HRM Android app now uses **Firebase** instead of Supabase for authentication and data storage.

---

## 📋 Setup Steps

### 1. Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Enter project name: `trinity-hrm` (or your preferred name)
4. Disable Google Analytics (optional)
5. Click **"Create project"**

### 2. Add Android App to Firebase

1. In Firebase Console, click the **Android icon** to add an Android app
2. Enter the package name: `com.trinity.hrm`
3. Enter app nickname: `Trinity HRM Mobile`
4. Leave SHA-1 blank for now (optional for auth)
5. Click **"Register app"**

### 3. Download google-services.json

1. Firebase will generate a `google-services.json` file
2. Click **"Download google-services.json"**
3. **IMPORTANT:** Replace the template file at:
   ```
   mobile-kotlin/app/google-services.json
   ```
   with the downloaded file

### 4. Enable Authentication

1. In Firebase Console, go to **Build → Authentication**
2. Click **"Get started"**
3. Click on **"Email/Password"** provider
4. **Enable** the Email/Password sign-in method
5. Click **"Save"**

### 5. Enable Firestore Database (Optional)

1. In Firebase Console, go to **Build → Firestore Database**
2. Click **"Create database"**
3. Choose **"Start in test mode"** (for development)
4. Select a location (choose closest to you)
5. Click **"Enable"**

### 6. Build and Run

```bash
cd mobile-kotlin
./gradlew :app:assembleDebug
```

Or run directly from Android Studio!

---

## ✅ What's Changed

### Removed:
- ❌ Supabase Kotlin SDK dependencies
- ❌ SupabaseClient.kt
- ❌ All Supabase-related code

### Added:
- ✅ Firebase Authentication SDK
- ✅ Firebase Firestore SDK
- ✅ FirebaseClient.kt for centralized Firebase access
- ✅ Real authentication (no more mocks!)

### Updated:
- ✅ ApiClient.kt - Now uses Firebase Auth
- ✅ MainActivity.kt - Initializes Firebase
- ✅ build.gradle.kts - Firebase dependencies

---

## 🎯 Features Now Working

- ✅ **Email/Password Login** - Real authentication
- ✅ **User Signup** - Create new accounts
- ✅ **Logout** - Sign out users
- ✅ **Session Persistence** - Users stay logged in
- ✅ **getCurrentUser()** - Check auth status

---

## 🔧 Troubleshooting

### Build fails with "google-services.json not found"
- Make sure you downloaded the file from Firebase Console
- Place it in `mobile-kotlin/app/google-services.json`

### Authentication fails
- Check that Email/Password is enabled in Firebase Console
- Verify your internet connection
- Check Firebase Console logs for errors

### App crashes on startup
- Make sure `google-services.json` is properly configured
- Check logcat for Firebase initialization errors

---

## 📱 Testing

1. Run the app
2. Go to Signup screen
3. Create a new account with email/password
4. You should be logged in automatically
5. Check Firebase Console → Authentication → Users to see your account!

---

## 🌐 Next Steps (Optional)

### Sync with Web App
If you want to share authentication between mobile and web:

1. Add Firebase to your Next.js web app:
   ```bash
   npm install firebase
   ```

2. Use the same Firebase project for both apps
3. Users can log in on mobile and web with the same credentials!

---

## 🎉 You're All Set!

Firebase is much more stable than Supabase Kotlin SDK 2.3.0. Enjoy your working authentication! 🚀
