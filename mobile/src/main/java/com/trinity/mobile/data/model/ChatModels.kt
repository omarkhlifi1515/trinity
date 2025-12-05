package com.trinity.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Models used to call the Python backend `/chat` endpoint.
 * Retrofit + Gson will serialize/deserialize these classes.
 */
data class ChatRequest(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("message") val message: String,
)

data class ChatResponse(
    @SerializedName("reply") val reply: String,
)

/**
 * Local UI message model used by the app to render chat bubbles.
 * `role` is either "user" or "assistant".
 */
data class Message(
    val id: String? = null,
    val userId: Int? = null,
    val role: String,
    val message: String,
)
