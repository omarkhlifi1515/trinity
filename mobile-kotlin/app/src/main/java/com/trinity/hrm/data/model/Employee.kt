package com.trinity.hrm.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val department: String? = null,
    val position: String? = null,
    val hireDate: String? = null,
    val createdAt: String
)

