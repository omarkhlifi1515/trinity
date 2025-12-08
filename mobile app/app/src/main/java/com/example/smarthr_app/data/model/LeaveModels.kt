package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

enum class LeaveType {
    SICK, ANNUAL, PERSONAL, EMERGENCY, MATERNITY, PATERNITY, UNPAID, OTHER
}

data class LeaveSummary(
    @SerializedName("period") val period: String? = null,
    val totalLeave: Int = 20,
    @SerializedName("available") val available: Int = 20,
    @SerializedName("leaveUsed") val leaveUsed: Int = 0
)

data class LeaveRequestDto(
    @SerializedName("leave_type") val leaveType: String, // 'sick', 'annual', etc.
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("emergency_contact") val emergencyContact: String? = null,
    @SerializedName("leave_description") val leaveDescription: String? = null
)

data class EmployeeLeaveResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("leave_type") val leaveType: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("days") val days: Int?,
    @SerializedName("status") val status: String, // 'pending', 'approved', 'rejected'
    @SerializedName("reason") val reason: String?,
    @SerializedName("leave_description") val leaveDescription: String? = null,
    @SerializedName("emergency_contact") val emergencyContact: String? = null,
    @SerializedName("response_by") val responseBy: String? = null,
    @SerializedName("responded_at") val respondedAt: String? = null,
    @SerializedName("rejection_reason") val rejectionReason: String? = null,
    @SerializedName("created_at") val createdAt: String?
)

// For HR to view requests (includes employee info)
data class HRLeaveResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("employee") val employee: UserInfo,
    @SerializedName("leave_type") val leaveType: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("leave_description") val leaveDescription: String? = null,
    @SerializedName("emergency_contact") val emergencyContact: String? = null,
    @SerializedName("response_by") val responseBy: UserInfo? = null,
    @SerializedName("response_by_name") val responseByName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("responded_at") val respondedAt: String? = null
)
