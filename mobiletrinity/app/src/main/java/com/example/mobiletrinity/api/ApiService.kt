package com.example.mobiletrinity.api

import retrofit2.http.*

interface ApiService {
    @GET("tasks")
    suspend fun getTasks(): List<TaskResponse>

    @POST("tasks")
    suspend fun createTask(@Body request: TaskRequest): TaskResponse

    @PATCH("tasks/{id}/status")
    suspend fun updateTaskStatus(@Path("id") id: Int, @Body request: StatusUpdateRequest): TaskResponse

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int)

    @GET("stats")
    suspend fun getStats(): StatsResponse
}

interface AgentApiService {
    @GET("health")
    suspend fun checkHealth(): AgentHealthResponse
}
