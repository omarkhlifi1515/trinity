package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Int, // Laravel uses Integer IDs by default
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    // Helper to keep app logic working without breaking changes
    @SerializedName("role") val role: String = "Employee",
    @SerializedName("avatar") val avatar: String? = null
)

// Used for lists of users (e.g. in Chat or Task assignment)
data class UserInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = null
)
