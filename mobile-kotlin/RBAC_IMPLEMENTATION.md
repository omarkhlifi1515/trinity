# Mobile Kotlin App - RBAC Implementation Guide

## Overview
This guide explains how to implement role-based access control in the Trinity HRM mobile Kotlin app to match the web app functionality.

## Architecture

```
Firebase Authentication (Shared with Web)
    ↓
User Login → Fetch Firestore User Profile
    ↓
Firestore User Profile {
    uid: string
    email: string
    role: 'admin' | 'chef' | 'employee'
    department: string?
}
    ↓
Store in SharedPreferences
    ↓
Use role to show/hide UI elements
```

## Implementation Steps

### Step 1: Add Firestore Dependency

**File:** `mobile-kotlin/app/build.gradle.kts`

```kotlin
dependencies {
    // Existing Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Add Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // ... other dependencies
}
```

### Step 2: Create Firestore User Service

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/data/firebase/FirestoreUserService.kt`

```kotlin
package com.trinity.hrm.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: String = "employee", // admin, chef, employee
    val department: String? = null,
    val displayName: String? = null
)

class FirestoreUserService {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Create user profile in Firestore
     */
    suspend fun createUserProfile(uid: String, email: String): UserProfile {
        // Determine role based on email
        val role = if (email.lowercase() == "admin@gmail.com") "admin" else "employee"
        
        val profile = UserProfile(
            uid = uid,
            email = email,
            role = role,
            displayName = email.substringBefore("@")
        )

        usersCollection.document(uid).set(profile).await()
        return profile
    }

    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update user role (admin only)
     */
    suspend fun updateUserRole(uid: String, role: String, department: String? = null) {
        val updates = hashMapOf<String, Any>(
            "role" to role
        )
        if (department != null) {
            updates["department"] = department
        }
        usersCollection.document(uid).update(updates).await()
    }
}
```

### Step 3: Update Firebase Client

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/data/remote/FirebaseClient.kt`

```kotlin
package com.trinity.hrm.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.trinity.hrm.data.firebase.FirestoreUserService
import com.trinity.hrm.data.firebase.UserProfile
import kotlinx.coroutines.tasks.await

class FirebaseClient {
    private val auth = FirebaseAuth.getInstance()
    private val firestoreService = FirestoreUserService()

    /**
     * Sign up with email and password
     */
    suspend fun signup(email: String, password: String): Result<UserProfile> {
        return try {
            // Create Firebase auth user
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")

            // Create Firestore profile
            val profile = firestoreService.createUserProfile(firebaseUser.uid, email)
            
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<UserProfile> {
        return try {
            // Sign in with Firebase
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed")

            // Get or create Firestore profile
            var profile = firestoreService.getUserProfile(firebaseUser.uid)
            if (profile == null) {
                // Create profile if it doesn't exist (for existing users)
                profile = firestoreService.createUserProfile(firebaseUser.uid, email)
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Get current user
     */
    fun getCurrentUser() = auth.currentUser
}
```

### Step 4: Update SharedPreferences Storage

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/data/storage/UserPreferences.kt`

```kotlin
package com.trinity.hrm.data.storage

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("trinity_prefs", Context.MODE_PRIVATE)

    fun saveUser(uid: String, email: String, role: String, department: String? = null) {
        prefs.edit().apply {
            putString("user_id", uid)
            putString("user_email", email)
            putString("user_role", role)
            putString("user_department", department)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString("user_id", null)
    fun getUserEmail(): String? = prefs.getString("user_email", null)
    fun getUserRole(): String = prefs.getString("user_role", "employee") ?: "employee"
    fun getUserDepartment(): String? = prefs.getString("user_department", null)

    fun isAdmin(): Boolean = getUserRole() == "admin"
    fun isChef(): Boolean = getUserRole() == "chef"
    fun isEmployee(): Boolean = getUserRole() == "employee"

    fun canApproveLeaves(): Boolean = isAdmin() || isChef()
    fun canAddEmployees(): Boolean = isAdmin()
    fun canAddTasks(): Boolean = isAdmin() || isChef()

    fun clearUser() {
        prefs.edit().clear().apply()
    }
}
```

### Step 5: Update Login Screen

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/ui/screens/LoginScreen.kt`

```kotlin
// In your login function
lifecycleScope.launch {
    val result = firebaseClient.login(email, password)
    result.onSuccess { profile ->
        // Save user data with role
        userPreferences.saveUser(
            uid = profile.uid,
            email = profile.email,
            role = profile.role,
            department = profile.department
        )
        
        // Navigate to dashboard
        navigateToDashboard()
    }.onFailure { error ->
        showError(error.message)
    }
}
```

### Step 6: Update Dashboard Screen

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/ui/screens/DashboardScreen.kt`

```kotlin
@Composable
fun DashboardScreen(userPreferences: UserPreferences) {
    val userRole = userPreferences.getUserRole()
    val userEmail = userPreferences.getUserEmail() ?: ""

    Column {
        // User info card
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = userEmail, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                // Role badge
                Surface(
                    color = when (userRole) {
                        "admin" -> Color.Red.copy(alpha = 0.1f)
                        "chef" -> Color.Blue.copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when (userRole) {
                            "admin" -> "Administrator"
                            "chef" -> "Manager"
                            else -> "Employee"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Navigation items
        if (userPreferences.canAddEmployees()) {
            NavigationItem("Employees", Icons.Default.People) { /* navigate */ }
        }
        
        NavigationItem("Tasks", Icons.Default.Assignment) { /* navigate */ }
        NavigationItem("Leaves", Icons.Default.CalendarToday) { /* navigate */ }
        NavigationItem("Messages", Icons.Default.Message) { /* navigate */ }
        NavigationItem("Attendance", Icons.Default.Schedule) { /* navigate */ }
    }
}
```

### Step 7: Update Employees Screen

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/ui/screens/EmployeesScreen.kt`

```kotlin
@Composable
fun EmployeesScreen(userPreferences: UserPreferences) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Employees") })
        },
        floatingActionButton = {
            // Only show Add button for admins
            if (userPreferences.canAddEmployees()) {
                FloatingActionButton(onClick = { /* navigate to add employee */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Employee")
                }
            }
        }
    ) { padding ->
        // Employee list
        EmployeeList(modifier = Modifier.padding(padding))
    }
}
```

### Step 8: Update Tasks Screen

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/ui/screens/TasksScreen.kt`

```kotlin
@Composable
fun TasksScreen(userPreferences: UserPreferences) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tasks") })
        },
        floatingActionButton = {
            // Show Add button for admins and chefs
            if (userPreferences.canAddTasks()) {
                FloatingActionButton(onClick = { /* navigate to add task */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { padding ->
        // Task list
        TaskList(modifier = Modifier.padding(padding))
    }
}
```

### Step 9: Update Leaves Screen

**File:** `mobile-kotlin/app/src/main/java/com/trinity/hrm/ui/screens/LeavesScreen.kt`

```kotlin
@Composable
fun LeaveItem(
    leave: Leave,
    userPreferences: UserPreferences,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = leave.employeeName, style = MaterialTheme.typography.titleMedium)
            Text(text = "${leave.startDate} - ${leave.endDate}")
            Text(text = leave.type.capitalize())
            
            // Status badge
            Text(
                text = leave.status.uppercase(),
                color = when (leave.status) {
                    "approved" -> Color.Green
                    "rejected" -> Color.Red
                    else -> Color.Orange
                }
            )

            // Approve/Reject buttons (only for admins and chefs)
            if (userPreferences.canApproveLeaves() && leave.status == "pending") {
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        onClick = { onApprove(leave.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Approve")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onReject(leave.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}
```

## Testing

### Test 1: Admin Account
```
Email: admin@gmail.com
Password: admins
```
- Should see all navigation items
- Should see "Add Employee" button
- Should see "Add Task" button
- Should see approve/reject buttons on leaves

### Test 2: Employee Account
```
Email: employee@test.com
Password: password123
```
- Should NOT see "Add Employee" button
- Should NOT see "Add Task" button
- Should NOT see approve/reject buttons on leaves

### Test 3: Manager Account
1. Create account as `manager@test.com`
2. Use web app to change role to "chef"
3. Login on mobile
- Should see "Add Task" button
- Should see approve/reject buttons on leaves
- Should NOT see "Add Employee" button

## Deployment Checklist

- [ ] Add Firestore dependency
- [ ] Create FirestoreUserService
- [ ] Update FirebaseClient
- [ ] Update UserPreferences
- [ ] Update LoginScreen
- [ ] Update DashboardScreen
- [ ] Update EmployeesScreen
- [ ] Update TasksScreen
- [ ] Update LeavesScreen
- [ ] Test admin account
- [ ] Test employee account
- [ ] Test manager account
- [ ] Build and test APK

## Notes

- User roles are stored in Firestore and synced across web and mobile
- Roles are cached in SharedPreferences for offline access
- Admin role is automatically assigned to admin@gmail.com
- All role checks should be done both client-side (UI) and server-side (API)
