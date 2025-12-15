# Kotlin Android Implementation Guide

Since you have updated the web application to use Firebase for Attendance, Departments, Leaves, and Messages, you need to update your Kotlin Android app to match.

## 1. Data Models (`data/model/`)

Create or update these data classes to match your Firestore schema.

### `AttendanceRecord.kt`
```kotlin
package com.trinity.hrm.data.model

import com.google.firebase.Timestamp

data class AttendanceRecord(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val date: String = "", // YYYY-MM-DD
    val checkIn: String = "", // HH:MM
    val checkOut: String? = null,
    val status: String = "present",
    val createdAt: Timestamp = Timestamp.now()
)
```

### `LeaveRequest.kt`
```kotlin
package com.trinity.hrm.data.model

import com.google.firebase.Timestamp

data class LeaveRequest(
    val id: String = "",
    val employeeId: String = "",
    val type: String = "vacation",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val reason: String = "",
    val status: String = "pending",
    val createdAt: Timestamp = Timestamp.now()
)
```

### `Message.kt`
```kotlin
package com.trinity.hrm.data.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val subject: String = "",
    val content: String = "",
    val read: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
```

## 2. Repositories (`data/repository/`)

You need repositories to handle Firestore operations.

### `AttendanceRepository.kt`
```kotlin
package com.trinity.hrm.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trinity.hrm.data.model.AttendanceRecord
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("attendance")

    suspend fun checkIn(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        val now = Date()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

        // Check if already checked in
        val existing = collection
            .whereEqualTo("employeeId", user.uid)
            .whereEqualTo("date", dateStr)
            .get().await()

        if (!existing.isEmpty) return Result.failure(Exception("Already checked in"))

        val record = AttendanceRecord(
            employeeId = user.uid,
            employeeName = user.displayName ?: user.email ?: "Unknown",
            date = dateStr,
            checkIn = timeStr,
            status = "present"
        )

        collection.add(record).await()
        return Result.success(Unit)
    }

    suspend fun checkOut(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        val now = Date()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

        val existing = collection
            .whereEqualTo("employeeId", user.uid)
            .whereEqualTo("date", dateStr)
            .get().await()

        if (existing.isEmpty) return Result.failure(Exception("Not checked in today"))

        val docId = existing.documents[0].id
        collection.document(docId).update("checkOut", timeStr).await()
        return Result.success(Unit)
    }
}
```

## 3. UI Implementation
- Use Jetpack Compose to create screens that call these repository functions.
- Ensure you have `google-services.json` in your `app/` folder.
- Add `implementation 'com.google.firebase:firebase-firestore-ktx'` to your dependencies.
