# Role-Based Access Control (RBAC) Implementation

## ✅ What Was Implemented

All three apps (Web, React Native, Kotlin) now have role-based access control:

### Roles

1. **Admin** (`admin@gmail.com`)
   - ✅ Can add employees
   - ✅ Can add tasks
   - ✅ Can manage departments
   - ✅ Full access to all features

2. **Department Head** (`department_head` role)
   - ✅ Can add tasks to employees in their department
   - ✅ Can view tasks, leaves, messages, attendance
   - ❌ Cannot add employees (admin only)

3. **Employee** (default role)
   - ✅ Can view tasks
   - ✅ Can request leaves
   - ✅ Can send/receive messages
   - ✅ Can check attendance
   - ❌ Cannot add tasks or employees

## 🔧 Implementation Details

### React Native App (`mobile/`)

**Files Updated:**
- `mobile/lib/auth.ts` - Added `role` and `department` fields to User interface
- `mobile/lib/roles.ts` - Role checking helper functions
- `mobile/app/(tabs)/dashboard.tsx` - Shows role, hides features based on role
- `mobile/app/(tabs)/employees.tsx` - Shows "Add Employee" button only for admins
- `mobile/app/(tabs)/tasks.tsx` - Shows "Add Task" button only for admins/department heads

**Role Assignment:**
- `admin@gmail.com` → automatically assigned `admin` role
- Other users → `employee` role by default

### Kotlin App (`mobile-kotlin/`)

**Files Updated:**
- `JsonBinClient.kt` - Added `UserRole` enum and `role`/`department` fields
- `LocalAuth.kt` - Assigns roles during signup
- `RoleHelper.kt` - Role checking helper functions
- `DashboardScreen.kt` - Shows role, hides navigation items based on role
- `EmployeesScreen.kt` - Shows "Add Employee" button only for admins
- `TasksScreen.kt` - Shows "Add Task" button only for admins/department heads

**Role Assignment:**
- `admin@gmail.com` → automatically assigned `ADMIN` role
- Other users → `EMPLOYEE` role by default

### Web App (`web/`)

**Files Updated:**
- `web/lib/auth/local-auth.ts` - Added `role` and `department` fields to User interface
- `web/lib/auth/roles.ts` - Role checking helper functions

**Role Assignment:**
- `admin@gmail.com` → automatically assigned `admin` role
- Other users → `employee` role by default

## 🎯 Role Permissions Summary

| Feature | Admin | Department Head | Employee |
|---------|-------|----------------|----------|
| Add Employees | ✅ | ❌ | ❌ |
| Add Tasks | ✅ | ✅ (to department) | ❌ |
| View Tasks | ✅ | ✅ | ✅ |
| Request Leaves | ✅ | ✅ | ✅ |
| Send Messages | ✅ | ✅ | ✅ |
| Check Attendance | ✅ | ✅ | ✅ |
| Manage Departments | ✅ | ✅ (own dept) | ❌ |

## 📝 How to Assign Department Head Role

Currently, only `admin@gmail.com` gets admin role automatically. To assign department head role:

### React Native / Web:
```typescript
// In signup or user creation, set role manually:
const user: User = {
  id: Date.now().toString(),
  email: 'manager@example.com',
  password: hashPassword(password),
  role: 'department_head',
  department: 'Engineering', // Set department name
  createdAt: new Date().toISOString(),
}
```

### Kotlin:
```kotlin
val newUser = JsonBinClient.User(
    id = System.currentTimeMillis().toString(),
    email = "manager@example.com",
    password = hashPassword(password),
    role = JsonBinClient.UserRole.DEPARTMENT_HEAD,
    department = "Engineering",
    createdAt = java.time.Instant.now().toString()
)
```

## 🧪 Testing

1. **Test Admin:**
   - Sign up/login as `admin@gmail.com`
   - Should see "Add Employee" button
   - Should see "Add Task" button
   - Should see "Departments" in navigation

2. **Test Employee:**
   - Sign up/login as regular user (e.g., `user@example.com`)
   - Should NOT see "Add Employee" button
   - Should NOT see "Add Task" button
   - Should see Tasks, Leaves, Messages, Attendance

3. **Test Department Head:**
   - Create user with `department_head` role
   - Should see "Add Task" button (can assign to department)
   - Should NOT see "Add Employee" button

## 🔄 Shared Database

All three apps share the same JSONBin.io database, so:
- Roles assigned in one app are visible in all apps
- User created as admin in web app → admin in mobile apps too
- Consistent role checking across all platforms

---

**Role-based access control is now implemented across all three apps! 🎉**

