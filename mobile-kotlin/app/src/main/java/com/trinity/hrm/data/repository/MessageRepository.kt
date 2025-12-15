package com.trinity.hrm.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trinity.hrm.data.model.Message
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class MessageRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("messages")

    suspend fun getMessages(): Result<List<Message>> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            // Simplified query: get messages where receiver is current user OR 'all'
            // Firestore doesn't support logical OR directly in one query well without composite indexes
            // We'll fetch directed messages first for simplicity in this demo
            
            val snapshot = collection
                 // Note: Ideally need complex query or multiple queries. 
                 // For MVP, just fetching messages to this user.
                .whereEqualTo("receiverId", user.uid)
                .get()
                .await()
                
            val broadcastSnapshot = collection
                .whereEqualTo("receiverId", "all")
                .get()
                .await()

            val allDocs = snapshot.documents + broadcastSnapshot.documents
            
            val list = allDocs.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Message(
                    id = doc.id,
                    from = data["senderName"] as? String ?: data["senderEmail"] as? String ?: "Unknown",
                    to = "Me",
                    subject = data["subject"] as? String ?: "No Subject",
                    content = data["content"] as? String ?: "",
                    read = data["read"] as? Boolean ?: false,
                    createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.toString() ?: ""
                )
            }.distinctBy { it.id } // remove dupes if any
            
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(toUserId: String, subject: String, content: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))

        val msg = hashMapOf(
            "senderId" to user.uid,
            "senderEmail" to (user.email ?: ""),
            "senderName" to (user.displayName ?: ""),
            "receiverId" to toUserId,
            "subject" to subject,
            "content" to content,
            "read" to false,
            "createdAt" to Timestamp.now()
        )

        collection.add(msg).await()
        return Result.success(Unit)
    }
}
