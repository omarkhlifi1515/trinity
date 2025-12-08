package com.example.smarthr_app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Request/Response Models
data class N8nChatRequest(val message: String)
data class N8nChatResponse(val response: String) // Ensure your n8n workflow returns JSON: { "response": "..." }

// 2. Interface
interface N8nApi {
    // Replace with your specific n8n webhook path
    @POST("webhook-test/trinity-chat")
    suspend fun sendMessage(@Body request: N8nChatRequest): N8nChatResponse
}

// 3. Separate Retrofit Instance for n8n
object N8nApiClient {
    // Replace with your actual n8n Cloud or Self-hosted URL
    private const val BASE_URL = "https://lasmih.app.n8n.cloud/"

    val api: N8nApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(N8nApi::class.java)
    }
}
