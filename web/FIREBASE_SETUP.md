# Firebase Setup for Trinity HRM Web App

## 🔥 Firebase Integration Complete!

The Trinity HRM Next.js web app now uses **Firebase** for authentication, sharing the same Firebase project with the mobile app!

---

## ✅ What's Done

1. ✅ **Firebase SDK installed** - `firebase` package added
2. ✅ **Firebase config created** - Using same project as mobile app
3. ✅ **Login/Signup updated** - Now using Firebase Auth
4. ✅ **Middleware simplified** - Firebase uses client-side auth
5. ✅ **AuthGuard component** - Protects dashboard routes

---

## 📋 Final Setup Steps

### 1. Add Web App to Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **trinity-6d93d**
3. Click the **Web icon** (</>) to add a web app
4. Enter app nickname: `Trinity HRM Web`
5. **Don't** check "Firebase Hosting" (we're using Vercel/local)
6. Click **Register app**

### 2. Copy Web App Config

Firebase will show you a config object like this:

```javascript
const firebaseConfig = {
  apiKey: "AIzaSy...",
  authDomain: "trinity-6d93d.firebaseapp.com",
  projectId: "trinity-6d93d",
  storageBucket: "trinity-6d93d.firebasestorage.app",
  messagingSenderId: "938399326576",
  appId: "1:938399326576:web:XXXXX" // ← Copy this!
};
```

### 3. Update Firebase Config

Update the `appId` in `web/lib/firebase/config.ts`:

```typescript
const firebaseConfig = {
  // ... other fields are already correct
  appId: "1:938399326576:web:YOUR_ACTUAL_WEB_APP_ID" // Replace this!
};
```

### 4. Run the Web App

```bash
cd web
npm run dev
```

Visit http://localhost:3000 and try logging in!

---

## 🎯 Shared Authentication

### Same Firebase Project = Shared Users!

- ✅ **Create account on mobile** → Can log in on web
- ✅ **Create account on web** → Can log in on mobile
- ✅ **Same user database** for both platforms
- ✅ **Synchronized authentication** state

### Test It:

1. **On Mobile App:**
   - Sign up with: `test@example.com`
   - Password: `password123`

2. **On Web App:**
   - Log in with same credentials
   - It works! 🎉

---

## 🔧 What Changed

### Removed:
- ❌ Supabase dependencies (`@supabase/ssr`, `@supabase/supabase-js`)
- ❌ Supabase client files
- ❌ Complex server-side auth middleware

### Added:
- ✅ Firebase SDK (`firebase`)
- ✅ `lib/firebase/config.ts` - Firebase configuration
- ✅ `lib/firebase/auth.ts` - Authentication client
- ✅ `components/auth/AuthGuard.tsx` - Route protection

### Updated:
- ✅ `components/auth/LoginPage.tsx` - Firebase login
- ✅ `components/auth/SignupPage.tsx` - Firebase signup
- ✅ `middleware.ts` - Simplified for client-side auth

---

## 🚀 Next Steps

### Optional Enhancements:

1. **Add Firestore for Data Storage:**
   ```typescript
   import { db } from '@/lib/firebase/config';
   import { collection, addDoc } from 'firebase/firestore';
   
   // Save employee data
   await addDoc(collection(db, 'employees'), {
     name: 'John Doe',
     email: 'john@example.com'
   });
   ```

2. **Add Profile Pictures:**
   - Use Firebase Storage
   - Upload user avatars

3. **Add Email Verification:**
   ```typescript
   import { sendEmailVerification } from 'firebase/auth';
   await sendEmailVerification(user);
   ```

---

## 🎉 You're All Set!

Both your **mobile app** and **web app** now use Firebase with shared authentication! 🚀

Users can seamlessly switch between platforms with the same login credentials.
