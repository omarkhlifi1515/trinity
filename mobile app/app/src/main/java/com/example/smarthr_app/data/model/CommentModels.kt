package com.example.smarthr_app.data.model


import com.google.gson.annotations.SerializedName
import kotlin.jvm.JvmName

data class CommentRequest(
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("content") val content: String,
    @SerializedName("user_id") val userId: Int
)

data class CommentResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("content") val content: String,
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user") val user: UserInfo?, // The user who made the comment
    @SerializedName("author") val author: UserInfo? = null, // Alternative field name
    @SerializedName("text") val text: String? = null, // Alternative field name
    @SerializedName("created_at") val createdAt: String?
) {
    // Helper to get author/user
    @JvmName("getCommentAuthor")
    fun getAuthor(): UserInfo? = author ?: user
    
    // Helper to get text/content
    @JvmName("getCommentText")
    fun getText(): String = text ?: content
}
