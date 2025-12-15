package com.trinity.hrm.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trinity.hrm.data.model.Attendance
import com.trinity.hrm.data.model.AttendanceStatus
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AttendanceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("attendance")

    suspend fun getAttendanceForUser(userId: String): Result<List<Attendance>> {
        return try {
            val snapshot = collection
                .whereEqualTo("employeeId", userId)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                // Manual mapping as an example, or use toObject if classes match perfectly
                Attendance(
                    id = doc.id,
                    employeeId = data["employeeId"] as? String ?: "",
                    date = data["date"] as? String ?: "",
                    checkIn = data["checkIn"] as? String,
                    checkOut = data["checkOut"] as? String,
                    status = try {
                        AttendanceStatus.valueOf((data["status"] as? String)?.uppercase() ?: "PRESENT")
                    } catch (e: Exception) { AttendanceStatus.PRESENT },
                    notes = data["notes"] as? String
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIn(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        val now = LocalDate.now()
        val time = LocalTime.now()
        val dateStr = now.format(DateTimeFormatter.ISO_DATE)
        val timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"))

        // Check if already checked in
        val existing = collection
            .whereEqualTo("employeeId", user.uid)
            .whereEqualTo("date", dateStr)
            .get().await()

        if (!existing.isEmpty) return Result.failure(Exception("Already checked in today"))

        val record = hashMapOf(
            "employeeId" to user.uid,
            "employeeName" to (user.displayName ?: user.email ?: "Unknown"),
            "date" to dateStr,
            "checkIn" to timeStr,
            "status" to "present",
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        collection.add(record).await()
        return Result.success(Unit)
    }

    suspend fun checkOut(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        val now = LocalDate.now()
        val dateStr = now.format(DateTimeFormatter.ISO_DATE)
        val timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

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
