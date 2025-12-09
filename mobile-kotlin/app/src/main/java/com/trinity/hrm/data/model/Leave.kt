package com.trinity.hrm.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Leave(
    val id: String,
    val employeeId: String,
    val type: LeaveType,
    val startDate: String,
    val endDate: String,
    val reason: String? = null,
    val status: LeaveStatus = LeaveStatus.PENDING,
    val approvedBy: String? = null,
    val createdAt: String
)

@Serializable
enum class LeaveType {
    SICK,
    VACATION,
    PERSONAL,
    EMERGENCY,
    OTHER
}

@Serializable
enum class LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}

