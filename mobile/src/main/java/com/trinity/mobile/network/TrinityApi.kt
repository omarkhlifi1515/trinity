package com.trinity.mobile.network

import com.trinity.mobile.data.model.ChatRequest
import com.trinity.mobile.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for calling the Trinity Python backend.
 * Base URL should be provided when building Retrofit (e.g. https://api.yourdomain/ or http://10.0.2.2:8000/)
 */
interface TrinityApi {
    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}
