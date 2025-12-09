package com.trinity.hrm.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val assignedTo: String, // Employee ID
    val assignedBy: String, // User ID (admin or department head)
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: String? = null,
    val createdAt: String
)

@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

