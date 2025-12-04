package com.example.mobiletrinity.api

import retrofit2.http.*

data class TaskRequest(
    val title: String,
    val description: String,
    val priority: String,
    val dueDate: String
)

data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val dueDate: String,
    val createdAt: String
)

data class NotificationRequest(
    val userId: Int,
    val message: String,
    val type: String
)

data class StatusUpdateRequest(
    val taskId: Int,
    val status: String
)

interface ApiService {
    
    @GET("/api/tasks")
    suspend fun getTasks(): List<TaskResponse>
    
    @POST("/api/tasks")
    suspend fun createTask(@Body task: TaskRequest): TaskResponse
    
    @GET("/api/tasks/{id}")
    suspend fun getTask(@Path("id") id: Int): TaskResponse
    
    @PUT("/api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: TaskRequest): TaskResponse
    
    @DELETE("/api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int)
    
    @POST("/api/tasks/{id}/status")
    suspend fun updateTaskStatus(@Path("id") id: Int, @Body update: StatusUpdateRequest): TaskResponse
    
    @POST("/api/notifications")
    suspend fun sendNotification(@Body notification: NotificationRequest)
}
