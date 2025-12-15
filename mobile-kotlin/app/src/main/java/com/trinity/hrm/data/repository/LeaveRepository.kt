package com.trinity.hrm.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trinity.hrm.data.model.Leave
import com.trinity.hrm.data.model.LeaveStatus
import com.trinity.hrm.data.model.LeaveType
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import java.util.Date

class LeaveRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("leaves")

    suspend fun getLeavesForUser(userId: String): Result<List<Leave>> {
        return try {
            val snapshot = collection
                .whereEqualTo("employeeId", userId)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Leave(
                    id = doc.id,
                    employeeId = data["employeeId"] as? String ?: "",
                    type = try {
                        LeaveType.valueOf((data["type"] as? String)?.uppercase() ?: "VACATION")
                    } catch(e: Exception) { LeaveType.VACATION },
                    // Convert Timestamp to String for UI compatibility
                    startDate = (data["startDate"] as? Timestamp)?.toDate()?.toString() ?: "",
                    endDate = (data["endDate"] as? Timestamp)?.toDate()?.toString() ?: "",
                    reason = data["reason"] as? String,
                    status = try {
                        LeaveStatus.valueOf((data["status"] as? String)?.uppercase() ?: "PENDING")
                    } catch(e: Exception) { LeaveStatus.PENDING },
                    approvedBy = data["approvedBy"] as? String,
                    createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.toString() ?: ""
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestLeave(type: LeaveType, startDate: Date, endDate: Date, reason: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))

        val request = hashMapOf(
            "employeeId" to user.uid,
            "type" to type.name.lowercase(),
            "startDate" to Timestamp(startDate),
            "endDate" to Timestamp(endDate),
            "reason" to reason,
            "status" to "pending",
            "createdAt" to Timestamp.now()
        )

        collection.add(request).await()
        return Result.success(Unit)
    }
}
