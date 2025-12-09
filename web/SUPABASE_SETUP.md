# Supabase Setup Guide

## Step 1: Get Your Supabase Credentials

1. Go to your Supabase project dashboard: https://supabase.com/dashboard
2. Navigate to **Settings** → **API**
3. Copy the following:
   - **Project URL**: `https://nghwpwajcoofbgvsevgf.supabase.co`
   - **anon/public key**: This is a JWT token that starts with `eyJ...` (NOT the publishable key)

⚠️ **IMPORTANT**: The anon key is different from the publishable key. Make sure you're copying the **anon/public** key from the API settings.

## Step 2: Set Environment Variables

Create a `.env.local` file in the `web` directory:

```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your_anon_key_here
```

Replace `your_anon_key_here` with the actual anon key from Step 1.

## Step 3: Run the SQL Schema

1. Go to your Supabase project dashboard
2. Navigate to **SQL Editor**
3. Click **New Query**
4. Copy and paste the contents of `supabase-schema.sql`
5. Click **Run** or press `Ctrl+Enter`

This will create all the necessary tables, policies, and triggers for the HRM system.

## Step 4: Verify Tables Created

After running the SQL, verify these tables exist:
- `profiles`
- `departments`
- `employees`
- `tasks`
- `attendance`
- `leaves`
- `messages`

## Step 5: Create Your First Admin User

1. Go to **Authentication** → **Users** in Supabase dashboard
2. Click **Add User** → **Create New User**
3. Enter email and password
4. After creating the user, go to **SQL Editor** and run:

```sql
-- Update the user's role to admin (replace 'user-email@example.com' with your email)
UPDATE public.profiles 
SET role = 'admin', full_name = 'Admin User'
WHERE email = 'user-email@example.com';
```

## Step 6: Test the Application

1. Start the development server:
   ```bash
   npm run dev
   ```

2. Go to `http://localhost:3000`
3. Sign up with a new account or login with your admin account
4. You should be redirected to `/dashboard`

## Troubleshooting

### Login keeps redirecting to login page

1. **Check your anon key**: Make sure you're using the correct anon key (starts with `eyJ`)
2. **Check environment variables**: Restart your dev server after adding `.env.local`
3. **Check browser console**: Look for any errors related to Supabase
4. **Check Supabase logs**: Go to **Logs** → **Auth Logs** in Supabase dashboard

### "Missing Supabase environment variables" error

- Make sure `.env.local` exists in the `web` directory
- Make sure variable names start with `NEXT_PUBLIC_`
- Restart your dev server after creating/modifying `.env.local`

### RLS (Row Level Security) errors

- Make sure you ran the complete SQL schema
- Check that RLS policies are enabled in **Authentication** → **Policies**

## Database Schema Overview

- **profiles**: User profiles linked to auth.users
- **departments**: Company departments
- **employees**: Detailed employee information
- **tasks**: Task assignments and tracking
- **attendance**: Daily attendance records
- **leaves**: Leave requests and approvals
- **messages**: Internal messaging system

All tables have Row Level Security (RLS) enabled with appropriate policies for different user roles.

