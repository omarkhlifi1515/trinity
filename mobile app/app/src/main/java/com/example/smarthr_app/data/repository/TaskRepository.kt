package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a comment fetched from the database.
 * Make sure the @SerializedName values match your Supabase column names exactly.
 */
data class Comment(
    val id: Int, // Or String, depending on if your DB uses UUID or Integer IDs

    @SerializedName("task_id")
    val taskId: Int,

    @SerializedName("user_id")
    val userId: String, // Supabase Auth User IDs are usually UUID Strings

    val content: String,

    @SerializedName("created_at")
    val createdAt: String,

    // Optional: If your backend query joins the user table to get the name
    @SerializedName("user_name")
    val userName: String? = "Unknown"
)

/**
 * Represents the data sent to the backend to create a new comment.
 */
data class CreateCommentRequest(
    @SerializedName("task_id")
    val taskId: Int,

    val content: String
)
