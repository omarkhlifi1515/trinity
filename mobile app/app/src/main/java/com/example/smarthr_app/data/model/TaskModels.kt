package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val title: String, // Laravel column is 'name'
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String, // 'pending', 'in_progress', 'completed'
    @SerializedName("priority") val priority: String, // 'low', 'medium', 'high'
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    // Relationships (if loaded by Laravel)
    @SerializedName("assignee") val assignee: UserInfo? = null
)

data class TaskRequest(
    @SerializedName("name") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("assignee_id") val assigneeId: Int?
)

// Used for updating status (Drag and Drop in Kanban)
data class UpdateTaskStatusRequest(
    @SerializedName("status") val status: String
)

// Simplified response for lists
data class TaskFullDetailResponse(
    @SerializedName("data") val task: TaskResponse
)
