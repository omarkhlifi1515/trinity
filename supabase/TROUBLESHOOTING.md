# Supabase Schema Troubleshooting

## Error: "column employee_id does not exist"

This error means the tables from Part 1 either:
1. Weren't created successfully
2. Were created with different column names
3. Are in a different schema

### Solution Steps:

#### Step 1: Verify Tables Exist
Run `check-tables.sql` in Supabase SQL Editor. This will show you:
- Which tables exist
- What columns each table has

#### Step 2: Check Part 1 Results
1. Go to Supabase Dashboard → **Table Editor**
2. Look for these tables:
   - `users`
   - `employees`
   - `departments`
   - `tasks`
   - `leaves` ← Check this one!
   - `messages`
   - `attendance` ← Check this one!

#### Step 3: If Tables Don't Exist
- Re-run `schema-part1-tables.sql`
- Make sure you see "Success" message
- Check for any error messages

#### Step 4: If Tables Exist But Columns Are Different
- Check the actual column names in Table Editor
- The `leaves` table should have: `employee_id`
- The `attendance` table should have: `employee_id`

#### Step 5: Use Safe Version
- Use `schema-part2-indexes-safe.sql` instead
- This version checks if columns exist before creating indexes
- It will skip missing columns gracefully

## Common Issues

### Issue: "relation already exists"
**Solution**: This is OK! The table/index already exists. You can continue.

### Issue: "permission denied"
**Solution**: Make sure you're using the SQL Editor (not a restricted user)

### Issue: Tables exist but columns are wrong
**Solution**: 
1. Drop the tables: `DROP TABLE IF EXISTS leaves, attendance CASCADE;`
2. Re-run Part 1
3. Then run Part 2

### Issue: Part 1 ran but tables are missing
**Solution**:
1. Check the SQL Editor output for errors
2. Make sure you ran the ENTIRE Part 1 file
3. Check if you're in the correct database/schema

## Quick Fix

If you're stuck, try this order:
1. ✅ Run `check-tables.sql` - see what exists
2. ✅ If tables missing → Run Part 1 again
3. ✅ If tables exist → Run `schema-part2-indexes-safe.sql`
4. ✅ Continue with Part 3 and Part 4

The safe version will only create indexes for columns that actually exist!

