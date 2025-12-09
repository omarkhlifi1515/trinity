# Debugging Login Issues

If login keeps redirecting back to the login page, check these:

## 1. Verify Environment Variables

Make sure you have a `.env.local` file in the `web` directory with:

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your_actual_anon_key_here
```

**IMPORTANT**: The anon key should start with `eyJ` (it's a JWT token), NOT `sb_publishable_...`

To get the correct key:
1. Go to Supabase Dashboard → Settings → API
2. Copy the `anon` `public` key (not the publishable key)

## 2. Check Browser Console

Open browser DevTools (F12) and check:
- Are there any errors in the Console?
- Are cookies being set? (Application → Cookies → localhost:3000)
- Look for `sb-` prefixed cookies

## 3. Check Network Tab

In DevTools → Network:
- Look for requests to `/dashboard`
- Check the response status (should be 200, not 302 redirect)
- Check if cookies are being sent in the request headers

## 4. Verify User Exists in Supabase

1. Go to Supabase Dashboard → Authentication → Users
2. Check if your user exists
3. If not, create one manually or sign up first

## 5. Test Session Directly

In browser console, run:

```javascript
// Check if Supabase client is working
const { createClient } = require('@supabase/supabase-js')
const supabase = createClient(
  'https://nghwpwajcoofbgvsevgf.supabase.co',
  'YOUR_ANON_KEY'
)

// Check current session
supabase.auth.getSession().then(console.log)

// Try to get user
supabase.auth.getUser().then(console.log)
```

## 6. Clear Everything and Retry

1. Clear browser cookies for localhost:3000
2. Clear browser cache
3. Restart your dev server: `npm run dev`
4. Try logging in again

## 7. Check Supabase Auth Settings

In Supabase Dashboard → Authentication → URL Configuration:
- Site URL: `http://localhost:3000` (for dev)
- Redirect URLs: Should include `http://localhost:3000/**`

## Common Issues

### Issue: "Invalid API key"
- **Solution**: You're using the wrong key. Get the anon key (starts with `eyJ`)

### Issue: Cookies not being set
- **Solution**: Check browser settings, disable ad blockers, check SameSite cookie settings

### Issue: Session exists but middleware doesn't see it
- **Solution**: Restart dev server, clear cookies, check middleware.ts is in the root

### Issue: User doesn't exist in database
- **Solution**: Sign up first, or create user manually in Supabase dashboard

