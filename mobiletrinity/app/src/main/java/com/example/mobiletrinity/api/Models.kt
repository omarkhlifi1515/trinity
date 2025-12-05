package com.example.mobiletrinity.api

data class TaskRequest(
    val title: String,
    val description: String,
    val priority: String,
    val dueDate: String? = null
)

data class StatusUpdateRequest(
    val id: Int,
    val status: String
)

data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val dueDate: String,
    val createdAt: String
)

data class TaskListResponse(
    val tasks: List<TaskResponse>
)

data class StatsResponse(
    val totalUsers: Int,
    val systemHealth: String
)

data class AgentHealthResponse(
    val connected: Boolean
)
