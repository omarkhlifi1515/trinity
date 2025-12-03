# HR Portal - Quick Setup (3 Steps)

## Step 1: Configure Google Drive Path

Edit `config.py` and set your Google Drive path:

```python
GOOGLE_DRIVE_DB_PATH = r'C:/Users/YOUR_USERNAME/Google Drive/Project/hr_data.db'
```

**Replace `YOUR_USERNAME` with your Windows username!**

Example:
```python
GOOGLE_DRIVE_DB_PATH = r'C:/Users/msi/Google Drive/Project/hr_data.db'
```

## Step 2: Initialize Database

Run this command:

```bash
python init_db.py
```

This creates:
- ✅ Database file in Google Drive
- ✅ All tables (including Employee table)
- ✅ Default admin user (chef/chef)
- ✅ Default employee record

## Step 3: Start Application

```bash
python run.py
# OR
python application.py
```

## Access HR Portal

1. **Login** as chef (username: `chef`, password: `chef`)
2. **Navigate** to "HR Dashboard" in sidebar
3. **Add employees** using the form
4. **Manage** employee status, roles, and contact info

## Employee Table Structure

| Column | Type | Description |
|--------|------|-------------|
| ID | Integer | Auto-increment primary key |
| Name | String(100) | Employee full name |
| Role | String(20) | 'Chef' or 'Employee' |
| Status | String(20) | 'Active' or 'Absent' |
| Contact_Info | String(200) | Email, phone, etc. |
| Created_At | DateTime | Record creation time |
| Updated_At | DateTime | Last update time |

## Features

- ✅ **Add Employee**: Full name, role, status, contact info
- ✅ **Edit Employee**: Update any field
- ✅ **Delete Employee**: Remove from database
- ✅ **Toggle Status**: Quick Active/Absent toggle
- ✅ **Search**: Filter employees by name, role, status
- ✅ **Statistics**: Dashboard with counts and metrics
- ✅ **Profile View**: Employees can view their own profile

## Google Drive Sync

The database file (`hr_data.db`) will:
- ✅ Sync automatically to Google Drive cloud
- ✅ Be accessible from any device with Google Drive
- ✅ Be backed up automatically
- ✅ Sync changes in real-time

## Routes

### Chef (Admin) Routes:
- `/hr/dashboard` - HR Portal dashboard
- `/hr/employees` - Manage all employees
- `/hr/employee/add` - Add new employee
- `/hr/employee/<id>/edit` - Edit employee
- `/hr/employee/<id>/delete` - Delete employee
- `/hr/employee/<id>/toggle-status` - Toggle status (API)

### Employee Routes:
- `/profile` - View own employee profile

## That's It!

Your HR Portal is ready! 🎉

The database will sync to Google Drive automatically, and you can manage all employees from the web interface.

