# Supabase Schema Setup Instructions

The schema has been split into 4 parts for easier execution. Run them **in order** in your Supabase SQL Editor.

## Step-by-Step Instructions

### Step 1: Create Tables
1. Open your Supabase project dashboard
2. Go to **SQL Editor**
3. Copy and paste the contents of `schema-part1-tables.sql`
4. Click **Run** (or press Ctrl+Enter)
5. Wait for success confirmation

### Step 2: Create Indexes
1. **FIRST**: Run `check-tables.sql` to verify Part 1 was successful
   - This will show you which tables exist and their columns
   - If tables are missing, re-run Part 1

2. In the SQL Editor, create a new query
3. Copy and paste the contents of `schema-part2-indexes-safe.sql` (RECOMMENDED)
   - OR use `schema-part2-indexes-simple.sql` if you're sure tables exist
4. Click **Run**
5. Wait for success confirmation

**If you get an error about columns not existing:**
- Run `check-tables.sql` first to see what actually exists
- Make sure Part 1 ran completely and successfully
- Check that all tables were created (go to Table Editor in Supabase)
- Use `schema-part2-indexes-safe.sql` - it checks for columns before creating indexes

### Step 3: Enable RLS and Create Policies
1. Create another new query
2. Copy and paste the contents of `schema-part3-rls.sql`
3. Click **Run**
4. Wait for success confirmation

### Step 4: Create Triggers
1. Create a final new query
2. Copy and paste the contents of `schema-part4-triggers.sql`
3. Click **Run**
4. Wait for success confirmation

## What Each Part Does

- **Part 1 (Tables)**: Creates all database tables (users, employees, tasks, leaves, messages, attendance, departments)
- **Part 2 (Indexes)**: Creates indexes for better query performance
- **Part 3 (RLS)**: Enables Row Level Security and creates access policies
- **Part 4 (Triggers)**: Creates triggers to auto-update `updated_at` timestamps

## Troubleshooting

If you get an error:
- **"relation already exists"**: The table/index/policy already exists. This is OK, you can continue.
- **"permission denied"**: Make sure you're using the SQL Editor with proper permissions.
- **"syntax error"**: Check that you copied the entire file correctly.

## After Setup

Once all 4 parts are run successfully:
1. ✅ Your database is ready
2. ✅ All tables are created
3. ✅ Security policies are in place
4. ✅ Auto-update triggers are active

You can now use Supabase with your web and Kotlin apps!

