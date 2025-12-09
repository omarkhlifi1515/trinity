package com.trinity.hrm.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val from: String, // User ID
    val to: String, // User ID or "all" for broadcast
    val subject: String,
    val content: String,
    val read: Boolean = false,
    val createdAt: String
)

