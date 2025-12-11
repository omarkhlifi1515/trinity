# Trinity HRM - Complete Role-Based Access Control Implementation Plan

## Overview
Implement a comprehensive role-based access control (RBAC) system across all three apps (Web, Mobile Kotlin, Mobile React Native) with:
- **Admin** (admin@gmail.com / password: admins) - Full control
- **Chef/Manager** - Can approve leaves, manage team
- **Employee** - Can request leaves, view tasks

## Architecture

### Authentication & Data Flow
```
Firebase Authentication (Shared)
    ↓
User Login → Check Firestore for user profile
    ↓
Firestore User Profile {
    uid: string
    email: string
    role: 'admin' | 'chef' | 'employee'
    department: string?
    createdAt: timestamp
}
    ↓
Supabase (Data Storage)
    - employees
    - tasks
    - leaves (with approval workflow)
    - messages
    - attendance
    - departments
```

## Implementation Tasks

### Phase 1: Firestore User Profile System (Web)

#### 1.1 Create Firestore User Service
**File:** `web/lib/firebase/users.ts`
- Create user profile on signup
- Fetch user profile on login
- Update user role (admin only)
- Assign roles based on email (admin@gmail.com → admin)

#### 1.2 Update Authentication Flow
**Files:**
- `web/lib/firebase/auth.ts` - Add Firestore profile creation
- `web/components/auth/SignupPage.tsx` - Create profile on signup
- `web/components/auth/LoginPage.tsx` - Fetch profile on login
- `web/app/dashboard/page.tsx` - Load user profile with role

#### 1.3 Create Role Management System
**File:** `web/lib/firebase/roles.ts`
- `isAdmin(user)` - Check if admin
- `isChef(user)` - Check if chef/manager
- `canApproveLeaves(user)` - Admin or chef
- `canAddEmployees(user)` - Admin only
- `canAssignRoles(user)` - Admin only

### Phase 2: Admin Panel for Role Assignment (Web)

#### 2.1 Create Admin Settings Page
**File:** `web/app/dashboard/admin/page.tsx`
- List all users
- Assign roles (admin, chef, employee)
- Assign departments
- Only accessible by admin@gmail.com

#### 2.2 Update Navigation
**File:** `web/app/dashboard/layout.tsx`
- Add "Admin" link (visible only to admin)
- Show current user role in sidebar

### Phase 3: Leave Approval Workflow

#### 3.1 Update Leave Data Model
**File:** `web/lib/storage/supabase-storage.ts`
```typescript
interface Leave {
  id: string
  employee_id: string
  employee_name: string  // NEW
  type: 'sick' | 'vacation' | 'personal' | 'emergency'
  start_date: string
  end_date: string
  reason: string
  status: 'pending' | 'approved' | 'rejected'
  approved_by?: string
  approved_by_name?: string  // NEW
  created_at: string
  updated_at?: string
}
```

#### 3.2 Update Leaves Page
**File:** `web/components/leaves/LeavesContent.tsx`
- Employees: Can create leave requests
- Chefs/Admins: Can approve/reject leaves
- Show approval buttons only for chefs/admins
- Show status badges (pending, approved, rejected)

#### 3.3 Create Leave Approval API
**File:** `web/app/api/leaves/approve/route.ts`
- POST endpoint to approve leave
- Check if user is chef or admin
- Update leave status in Supabase

### Phase 4: Fix All Web Pages

#### 4.1 Dashboard Page
**File:** `web/components/dashboard/DashboardContent.tsx`
- Show user role badge
- Display role-specific stats
- Show quick actions based on role

#### 4.2 Tasks Page
**File:** `web/components/tasks/TasksContent.tsx`
- ✅ Already has role check for "Create Task" button
- Add task assignment (assign to employees)
- Show assigned tasks for employees

#### 4.3 Employees Page
**File:** `web/components/employees/EmployeesContent.tsx`
- ✅ Already has role check for "Add Employee" button
- Add role badge in employee list
- Add "Edit Role" button (admin only)

#### 4.4 Messages Page
**File:** `web/components/messages/MessagesContent.tsx`
- Fix any broken buttons
- Add message filtering
- Add compose message feature

#### 4.5 Attendance Page
**File:** `web/components/attendance/AttendanceContent.tsx`
- Fix check-in/check-out buttons
- Add attendance history
- Show team attendance (chefs/admins)

#### 4.6 Departments Page
**File:** `web/components/departments/DepartmentsContent.tsx`
- Fix add department button
- Assign department heads
- Show department employees

### Phase 5: Mobile Kotlin App Updates

#### 5.1 Create Firestore User Service
**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/data/firebase/FirebaseUserService.kt`
- Fetch user profile from Firestore
- Store role in local storage
- Sync role on login

#### 5.2 Update Authentication
**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/data/remote/FirebaseClient.kt`
- Create Firestore profile on signup
- Fetch profile on login
- Store role in SharedPreferences

#### 5.3 Update UI Based on Role
**Files:**
- `DashboardScreen.kt` - Show role badge
- `EmployeesScreen.kt` - Hide "Add Employee" for non-admins
- `TasksScreen.kt` - Hide "Add Task" for employees
- `LeavesScreen.kt` - Show approve buttons for chefs/admins

#### 5.4 Implement Leave Approval
**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/ui/screens/LeavesScreen.kt`
- Add approve/reject buttons
- Call Supabase API to update leave status
- Refresh list after approval

### Phase 6: Mobile React Native App Updates (if exists)

#### 6.1 Similar updates as Kotlin app
- Create Firestore user service
- Update authentication flow
- Update UI based on roles
- Implement leave approval

### Phase 7: Testing & Validation

#### 7.1 Test Admin Account
- Email: admin@gmail.com
- Password: admins
- Should have full access to all features
- Can assign roles to other users

#### 7.2 Test Chef Account
- Create user, assign "chef" role
- Can approve/reject leaves
- Can add tasks
- Cannot add employees

#### 7.3 Test Employee Account
- Create user (default role)
- Can request leaves
- Can view tasks
- Cannot approve leaves or add employees

## Database Schema Updates

### Firestore Collections

#### users
```
{
  uid: string (Firebase Auth UID)
  email: string
  role: 'admin' | 'chef' | 'employee'
  department: string?
  displayName: string?
  createdAt: timestamp
  updatedAt: timestamp
}
```

### Supabase Tables (No changes needed, already set up)
- employees
- tasks
- leaves
- messages
- attendance
- departments

## Security Rules

### Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      // Anyone can read their own profile
      allow read: if request.auth != null && request.auth.uid == userId;
      
      // Only admins can write/update roles
      allow write: if request.auth != null && 
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin' ||
         request.auth.uid == userId);
    }
  }
}
```

## Environment Variables

### Web App (.env.local)
```
NEXT_PUBLIC_FIREBASE_API_KEY=AIzaSyBp_Pj4b-AnrbvSaZIIxaNLd394VX4EzjU
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=trinity-6d93d.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=trinity-6d93d
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=trinity-6d93d.firebasestorage.app
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=938399326576
NEXT_PUBLIC_FIREBASE_APP_ID=1:938399326576:web:ced4e3a907735e8cd4afb5

NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx
```

## Deployment Checklist

- [ ] Update Firestore security rules
- [ ] Create admin account (admin@gmail.com)
- [ ] Test all role permissions
- [ ] Test leave approval workflow
- [ ] Test on web app
- [ ] Test on mobile Kotlin app
- [ ] Test on mobile React Native app
- [ ] Update documentation

## Success Criteria

1. ✅ Admin can assign roles to users
2. ✅ Chef can approve/reject leave requests
3. ✅ Employee can only request leaves
4. ✅ All buttons work correctly
5. ✅ All pages display properly
6. ✅ Role-based access control works across all apps
7. ✅ Leave approval workflow is functional
