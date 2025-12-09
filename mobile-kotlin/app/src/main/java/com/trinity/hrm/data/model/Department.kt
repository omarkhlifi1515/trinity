package com.trinity.hrm.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Department(
    val id: String,
    val name: String,
    val description: String? = null,
    val headId: String? = null, // User ID of department head
    val employeeCount: Int = 0,
    val createdAt: String
)

