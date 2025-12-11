# 🔧 Firebase Web App - Troubleshooting Guide

## Common Issues & Solutions

### ❌ Error: "Cannot find module './vendor-chunks/@firebase.js'"

**Cause:** Next.js build cache is stale after adding Firebase dependencies.

**Solution:**
```bash
# Stop the dev server (Ctrl+C)

# Delete the .next folder
Remove-Item -Path ".next" -Recurse -Force

# Restart dev server
npm run dev
```

---

### ❌ Login works but stays on login page

**Cause:** Dashboard components were using Supabase server-side auth.

**Fixed in:** 
- `app/dashboard/layout.tsx` - Now uses Firebase client-side auth
- `app/dashboard/page.tsx` - Converted to client component
- `components/layout/Sidebar.tsx` - Updated logout to use Firebase

**Solution:** Already fixed! Just refresh the page.

---

### ❌ "Auth session missing" errors

**Cause:** Old Supabase cookies or middleware blocking requests.

**Solution:**
1. Clear browser cookies for localhost:3000
2. Middleware has been simplified for Firebase
3. Try logging in again

---

### ✅ How to Verify Everything Works

1. **Clear Browser Cache:**
   - Open DevTools (F12)
   - Application → Clear storage → Clear site data

2. **Test Login:**
   - Go to http://localhost:3000
   - Log in with your credentials
   - Should redirect to `/dashboard` ✅

3. **Test Signup:**
   - Go to signup page
   - Create new account
   - Should redirect to `/dashboard` ✅

4. **Test Logout:**
   - Click logout in sidebar
   - Should redirect to `/` ✅

---

### 🔍 Debugging Tips

**Check Firebase Auth in Console:**
```javascript
// In browser console
import { auth } from '@/lib/firebase/config'
console.log('Current user:', auth.currentUser)
```

**Check Network Tab:**
- Should see Firebase API calls to `identitytoolkit.googleapis.com`
- Should NOT see Supabase calls

**Check Console Logs:**
- Look for "✅ Login successful! User: [email]"
- Look for "Dashboard layout: User is PRESENT: [email]"

---

### 📝 Quick Reference

**Firebase Config:** `web/lib/firebase/config.ts`  
**Auth Client:** `web/lib/firebase/auth.ts`  
**Login Page:** `components/auth/LoginPage.tsx`  
**Signup Page:** `components/auth/SignupPage.tsx`  
**Dashboard Layout:** `app/dashboard/layout.tsx`  
**Auth Guard:** `components/auth/AuthGuard.tsx`

---

### 🚀 If All Else Fails

**Complete Reset:**
```bash
# Stop server
Ctrl+C

# Clean everything
Remove-Item -Path ".next" -Recurse -Force
Remove-Item -Path "node_modules" -Recurse -Force

# Reinstall
npm install

# Start fresh
npm run dev
```

---

### ✅ Success Indicators

- ✅ No Supabase errors in console
- ✅ Firebase auth calls in Network tab
- ✅ Login redirects to dashboard
- ✅ Dashboard shows user email
- ✅ Logout works properly

---

**Need Help?** Check the main setup guide: `FIREBASE_SETUP.md`
