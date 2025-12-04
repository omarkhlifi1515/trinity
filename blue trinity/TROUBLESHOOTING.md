# Blue Trinity Troubleshooting Guide

## Issue: Shows 0 Active, 0 Absent, 0 Present Today

### Why This Happens

Blue Trinity shows 0 counts when:
1. **Database is empty** - No employees added yet in Green Trinity Local
2. **Database doesn't exist** - Green Trinity Local hasn't created it yet
3. **Wrong database path** - Blue Trinity can't find the database file

### Solution Steps

#### Step 1: Verify Database Exists

Check if the database file exists:
```powershell
# Check if file exists
Test-Path "C:\Users\msi\Documents\GitHub\trinity\green trinity local\local_data.db"
```

**Expected:** Should return `True`

**If False:**
- Start Green Trinity Local app first
- Run `python init_db.py` to create database
- Or just use the app - it will create the database automatically

#### Step 2: Add Employees in Green Trinity Local

1. **Start Green Trinity Local:**
   ```powershell
   cd "green trinity local"
   # Make sure NO POSTGRES_URL env var is set
   python app.py
   ```

2. **Login as admin** (username: `admin`, password: `admin`)

3. **Go to HR Portal:**
   - Click "HR Dashboard" or "Manage Employees"
   - Add some employees
   - Set their status (Active/Absent)

4. **Mark Presence (optional):**
   - Login as a regular user
   - Click "Mark as Present" on dashboard
   - This creates entries in the `presence` table

#### Step 3: Run Blue Trinity Again

```powershell
cd "blue trinity"
python blue_trinity_agent.py manager --once
```

**Expected Output:**
```
[Config] 📁 Database path: C:\Users\msi\Documents\GitHub\trinity\green trinity local\local_data.db
[Config] 📁 Database exists: True
[Config] 📊 Database size: XXXXX bytes
[Manager] 📊 Reading database: ...
[Manager] 📋 Found tables: employee, user, role, presence, ...
[Manager] ✅ Using table: employee
[Manager] 🔍 Executing query on employee...
[Manager] 📈 Found X employee records
[Manager] ✅ Health Summary:
[Manager]    📊 Active Personnel: X
[Manager]    ⚠️  Absent Personnel: Y
[Manager]    ✅ Present Today: Z
```

### Common Issues

#### Issue: "Database not found"

**Cause:** Database path is wrong or database doesn't exist

**Fix:**
1. Check path in `blue_trinity_agent.py` line 46
2. Make sure Green Trinity Local has created the database
3. Verify file exists at that path

#### Issue: "No employee/user table found"

**Cause:** Database exists but has no employee table

**Fix:**
1. Make sure Green Trinity Local is using SQLite (not Postgres)
2. Run `python init_db.py` in Green Trinity Local folder
3. Or just use the HR Portal - it creates tables automatically

#### Issue: "Found 0 employee records"

**Cause:** Database exists but is empty

**Fix:**
1. Add employees in Green Trinity Local HR Portal
2. Go to `/hr/employees` and add some employees
3. Set their status to Active or Absent

#### Issue: "Present Today = 0" even with Active employees

**Cause:** No one has marked presence today

**Fix:**
1. Login as employees in Green Trinity Local
2. Click "Mark as Present" button
3. Or check if `presence` table exists and has today's entries

### Debugging Commands

**Check database directly:**
```powershell
# Using Python
python -c "import sqlite3; conn = sqlite3.connect('green trinity local/local_data.db'); cur = conn.cursor(); cur.execute('SELECT name FROM sqlite_master WHERE type=\"table\"'); print([r[0] for r in cur.fetchall()])"
```

**Check employee count:**
```powershell
python -c "import sqlite3; conn = sqlite3.connect('green trinity local/local_data.db'); cur = conn.cursor(); cur.execute('SELECT COUNT(*) FROM employee'); print(f'Employees: {cur.fetchone()[0]}')"
```

**Check presence entries:**
```powershell
python -c "import sqlite3; conn = sqlite3.connect('green trinity local/local_data.db'); cur = conn.cursor(); cur.execute('SELECT COUNT(*) FROM presence'); print(f'Presence entries: {cur.fetchone()[0]}')"
```

### Quick Test

1. **Start Green Trinity Local** (SQLite mode)
2. **Add 2-3 employees** via HR Portal
3. **Set one as "Absent"**
4. **Mark one as "Present"** (login as that user, click button)
5. **Run Blue Trinity:**
   ```powershell
   python blue_trinity_agent.py manager --once
   ```
6. **Should show:**
   - Active: 2
   - Absent: 1
   - Present Today: 1

### Still Not Working?

Check Blue Trinity output for:
- Database path being used
- Tables found
- Query results
- Any error messages

The enhanced debugging will show exactly what's happening!

