# Trinity HRM - Testing Guide

## 🌐 Web App Testing (http://localhost:3000)

### Test 1: Admin Account
1. **Sign Up / Login**
   - Email: `admin@gmail.com`
   - Password: `admins`

2. **Verify Admin Features**
   - ✅ Should see "Admin" link in sidebar (with shield icon)
   - ✅ Should see role badge showing "Administrator"
   - ✅ Should see all navigation items:
     - Dashboard
     - Employees
     - Departments
     - Tasks
     - Attendance
     - Leaves
     - Messages
     - Admin

3. **Test Admin Panel** (`/dashboard/admin`)
   - ✅ Should see list of all users
   - ✅ Should be able to change user roles
   - ✅ Role dropdown should have: Employee, Manager, Admin

4. **Test Employee Management** (`/dashboard/employees`)
   - ✅ Should see "Add Employee" button
   - ✅ Should be able to add new employees

5. **Test Task Management** (`/dashboard/tasks`)
   - ✅ Should see "Create Task" button
   - ✅ Should be able to create tasks

6. **Test Leave Management** (`/dashboard/leaves`)
   - ✅ Should see "Request Leave" button
   - ✅ Should see "Approve" and "Reject" buttons for pending leaves
   - ✅ Should be able to approve/reject leave requests

### Test 2: Regular Employee Account
1. **Sign Up**
   - Email: `employee@test.com`
   - Password: `password123`

2. **Verify Employee Features**
   - ❌ Should NOT see "Admin" link in sidebar
   - ✅ Should see role badge showing "Employee"
   - ✅ Should see limited navigation:
     - Dashboard
     - Tasks (view only)
     - Attendance
     - Leaves
     - Messages
   - ❌ Should NOT see Employees or Departments links

3. **Test Restrictions**
   - ❌ Cannot access `/dashboard/admin` (should redirect)
   - ❌ Cannot see "Add Employee" button
   - ❌ Cannot see "Create Task" button
   - ✅ Can see "Request Leave" button
   - ❌ Cannot see "Approve/Reject" buttons on leaves

### Test 3: Manager/Chef Account
1. **Create Manager Account**
   - Sign up as `manager@test.com` / `password123`
   - Login as `admin@gmail.com`
   - Go to `/dashboard/admin`
   - Change `manager@test.com` role to "Manager"
   - Logout and login as `manager@test.com`

2. **Verify Manager Features**
   - ❌ Should NOT see "Admin" link
   - ✅ Should see role badge showing "Manager"
   - ✅ Should see navigation:
     - Dashboard
     - Employees (view only)
     - Departments
     - Tasks
     - Attendance
     - Leaves
     - Messages
   - ✅ Should see "Create Task" button
   - ✅ Should see "Approve/Reject" buttons on leaves
   - ❌ Should NOT see "Add Employee" button

### Test 4: Leave Approval Workflow
1. **As Employee** (`employee@test.com`)
   - Go to `/dashboard/leaves`
   - Click "Request Leave"
   - Fill in leave details
   - Submit request
   - Status should be "Pending"

2. **As Manager/Admin** (`manager@test.com` or `admin@gmail.com`)
   - Go to `/dashboard/leaves`
   - Should see the pending leave request
   - Click "Approve" or "Reject"
   - Status should update accordingly

3. **As Employee** (refresh page)
   - Should see updated leave status

## 📱 Mobile Kotlin App Testing

### Prerequisites
1. Open Android Studio
2. Open project: `mobile-kotlin`
3. Sync Gradle files

### Test 1: Admin Account
1. **Sign Up / Login**
   - Email: `admin@gmail.com`
   - Password: `admins`

2. **Verify Admin Features**
   - Should see all navigation items
   - Should see "Add Employee" button
   - Should see "Add Task" button
   - Should see approve/reject buttons on leaves

### Test 2: Regular Employee
1. **Sign Up**
   - Email: `mobile-employee@test.com`
   - Password: `password123`

2. **Verify Restrictions**
   - Should NOT see "Add Employee" button
   - Should NOT see "Add Task" button
   - Should NOT see approve/reject buttons on leaves

## 🐛 Known Issues & Fixes

### Issue 1: "generateUUID is not defined"
**Fix:** Already handled - using `crypto.randomUUID()` instead

### Issue 2: Leave approval not working
**Fix:** Updated API to properly parse request body once

### Issue 3: Property name mismatches (endDate vs end_date)
**Fix:** Updated to use snake_case as defined in Supabase schema

### Issue 4: Role not loading on page refresh
**Fix:** Updated dashboard and sidebar to fetch user profile from Firestore

## 📝 Test Checklist

### Web App
- [ ] Admin can login with admin@gmail.com
- [ ] Admin sees Admin link in sidebar
- [ ] Admin can access /dashboard/admin
- [ ] Admin can assign roles to users
- [ ] Employee cannot see Admin link
- [ ] Employee cannot access /dashboard/admin
- [ ] Manager can approve leaves
- [ ] Manager can create tasks
- [ ] Manager cannot add employees
- [ ] Leave approval workflow works
- [ ] All buttons are functional
- [ ] All pages load without errors

### Mobile Kotlin App
- [ ] Admin can login
- [ ] Admin sees all features
- [ ] Employee has restricted access
- [ ] Role is persisted after app restart
- [ ] Leave approval works
- [ ] All screens load properly

### Mobile React Native App (if exists)
- [ ] Same tests as Kotlin app

## 🚀 Next Steps After Testing

1. **If tests pass:**
   - Commit changes to Git
   - Update documentation
   - Deploy to production

2. **If tests fail:**
   - Check browser console for errors
   - Check server logs
   - Verify Firebase/Firestore configuration
   - Verify Supabase connection

## 📞 Support

If you encounter any issues:
1. Check the browser console (F12)
2. Check the terminal for server errors
3. Verify environment variables are set
4. Ensure Firebase and Supabase are properly configured
