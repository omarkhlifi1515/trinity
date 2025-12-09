package com.trinity.hrm.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Attendance(
    val id: String,
    val employeeId: String,
    val date: String,
    val checkIn: String? = null,
    val checkOut: String? = null,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val notes: String? = null
)

@Serializable
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    HALF_DAY,
    ON_LEAVE
}

