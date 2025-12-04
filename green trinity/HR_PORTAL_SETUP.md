# HR Portal Setup Guide

## Overview
The HR Portal is now integrated into your TrinitySim application with Google Drive database synchronization.

## Features
- ✅ SQLite database stored in Google Drive (syncs to cloud)
- ✅ Employee management (Add, Edit, Delete, View)
- ✅ Role-based access (Chef = Admin, Employee = View only)
- ✅ Status tracking (Active/Absent)
- ✅ Contact information management
- ✅ HR Dashboard with statistics

## Step 1: Configure Google Drive Path

### Option 1: Edit config.py directly
Open `config.py` and update the `GOOGLE_DRIVE_DB_PATH`:

```python
GOOGLE_DRIVE_DB_PATH = r'C:/Users/YourName/Google Drive/Project/hr_data.db'
```

**Important:** 
- Use forward slashes `/` or raw string `r'...'`
- Replace `YourName` with your actual Windows username
- The folder `Project` will be created automatically if it doesn't exist

### Option 2: Use Environment Variable
Set environment variable before running:
```bash
# Windows PowerShell
$env:GOOGLE_DRIVE_DB_PATH="C:/Users/YourName/Google Drive/Project/hr_data.db"

# Windows CMD
set GOOGLE_DRIVE_DB_PATH=C:/Users/YourName/Google Drive/Project/hr_data.db

# Linux/Mac
export GOOGLE_DRIVE_DB_PATH="/path/to/Google Drive/Project/hr_data.db"
```

## Step 2: Initialize Database

Run the initialization script:

```bash
python init_db.py
```

This will:
- Create the database file in your Google Drive folder
- Set up all tables (including Employee table)
- Create default roles (CHEF, USER)
- Create default admin user (chef/chef)
- Create default employee record

## Step 3: Verify Database Location

After running `init_db.py`, check:
1. The database file `hr_data.db` should appear in your Google Drive folder
2. Google Drive should start syncing it to the cloud
3. You'll see a confirmation message with the path

## Step 4: Start the Application

```bash
python run.py
# OR
python application.py
```

## Database Structure

### Employee Table
```sql
CREATE TABLE employee (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'Employee',  -- 'Chef' or 'Employee'
    status VARCHAR(20) NOT NULL DEFAULT 'Active',  -- 'Active' or 'Absent'
    contact_info VARCHAR(200),
    created_at DATETIME,
    updated_at DATETIME,
    user_id INTEGER REFERENCES user(id)
);
```

## Usage

### For Chefs (Admins):

1. **HR Dashboard**: `/hr/dashboard`
   - View statistics
   - See recent employees
   - Quick actions

2. **Manage Employees**: `/hr/employees`
   - View all employees
   - Add new employees
   - Edit employee details
   - Delete employees
   - Toggle status (Active/Absent)

3. **Add Employee**: `/hr/employee/add`
   - Form to add new employee

4. **Edit Employee**: `/hr/employee/<id>/edit`
   - Edit existing employee

### For Employees:

1. **View Profile**: `/profile`
   - View their own employee record
   - See status and contact info

## API Endpoints

### Toggle Employee Status
```bash
POST /hr/employee/<id>/toggle-status
```
Returns JSON:
```json
{
  "success": true,
  "status": "Active",
  "message": "Employee status updated to Active"
}
```

## Google Drive Sync

### How It Works:
1. Database file (`hr_data.db`) is stored in your Google Drive folder
2. Google Drive automatically syncs it to the cloud
3. Any changes are synced in real-time
4. Accessible from any device with Google Drive

### Important Notes:
- ⚠️ **Don't edit the database file directly** while the app is running
- ⚠️ **Close the app** before manually moving/copying the database
- ✅ **Backup**: Google Drive automatically backs up the database
- ✅ **Multi-device**: Access the same database from multiple computers

## Troubleshooting

### Database not found?
- Check the path in `config.py`
- Ensure Google Drive folder exists
- Check file permissions

### Can't create database?
- Ensure you have write permissions to Google Drive folder
- Check if Google Drive is running and syncing
- Try creating the folder manually first

### Database sync issues?
- Ensure Google Drive desktop app is running
- Check Google Drive sync status
- Wait for sync to complete before accessing from another device

## Security Notes

- The database contains sensitive employee information
- Ensure Google Drive folder has proper access controls
- Use strong SECRET_KEY in production
- Consider encrypting the database for additional security

## Example Configuration

```python
# config.py
GOOGLE_DRIVE_DB_PATH = r'C:/Users/JohnDoe/Google Drive/Company/HR/hr_data.db'
```

This will create:
- Database: `hr_data.db` in `C:/Users/JohnDoe/Google Drive/Company/HR/`
- Automatically synced to Google Drive cloud
- Accessible from any device

## Next Steps

1. ✅ Configure Google Drive path
2. ✅ Run `python init_db.py`
3. ✅ Start application
4. ✅ Login as chef (chef/chef)
5. ✅ Navigate to HR Portal
6. ✅ Add your first employee!

Happy managing! 🎯

