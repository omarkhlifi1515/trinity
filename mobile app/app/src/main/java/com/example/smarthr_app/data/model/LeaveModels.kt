package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

data class LeaveRequestDto(
    @SerializedName("leave_type") val leaveType: String, // 'sick', 'annual', etc.
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("reason") val reason: String
)

data class EmployeeLeaveResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("leave_type") val leaveType: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("days") val days: Int?,
    @SerializedName("status") val status: String, // 'pending', 'approved', 'rejected'
    @SerializedName("reason") val reason: String?,
    @SerializedName("rejection_reason") val rejectionReason: String? = null,
    @SerializedName("created_at") val createdAt: String?
)

// For HR to view requests (includes employee info)
data class HRLeaveResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("employee") val employee: UserInfo,
    @SerializedName("leave_type") val leaveType: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("status") val status: String
)
