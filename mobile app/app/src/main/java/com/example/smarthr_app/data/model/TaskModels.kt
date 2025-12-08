package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

enum class TaskStatus(val value: String) {
    NOT_STARTED("not_started"),
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    FINISHED("finished");

    companion object {
        fun fromString(value: String?): TaskStatus? {
            return when (value?.lowercase()) {
                "not_started", "pending" -> PENDING
                "in_progress", "in progress" -> IN_PROGRESS
                "completed", "finished" -> COMPLETED
                else -> null
            }
        }
    }
}

enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    companion object {
        fun fromString(value: String?): TaskPriority? {
            return when (value?.lowercase()) {
                "low" -> LOW
                "medium" -> MEDIUM
                "high" -> HIGH
                "urgent" -> URGENT
                else -> null
            }
        }
    }
}

data class TaskResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val title: String, // Laravel column is 'name'
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String, // 'pending', 'in_progress', 'completed'
    @SerializedName("priority") val priority: String, // 'low', 'medium', 'high'
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("author") val author: UserInfo? = null,
    @SerializedName("employees") val employees: List<UserInfo>? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("task_status") val taskStatus: String? = null, // Alternative status field
    @SerializedName("image_url") val imageUrl: String? = null,
    // Relationships (if loaded by Laravel)
    @SerializedName("assignee") val assignee: UserInfo? = null
) {
    // Helper to get status enum
    fun getStatusEnum(): TaskStatus? = TaskStatus.fromString(taskStatus ?: status)
    
    // Helper to get priority enum
    fun getPriorityEnum(): TaskPriority? = TaskPriority.fromString(priority)
}

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
