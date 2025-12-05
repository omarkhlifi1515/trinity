package com.example.mobiletrinity.network

import retrofit2.http.*
import com.google.gson.annotations.SerializedName

// ============ WEB API SERVICE ============
interface WebApiService {
    @GET("api/users")
    suspend fun getUsers(): UserListResponse

    @GET("api/stats")
    suspend fun getStats(): SystemStatsResponse

    @GET("api/tasks")
    suspend fun getTasks(): TaskListResponse

    @POST("api/tasks")
    suspend fun createTask(@Body task: TaskRequest): TaskResponse

    @Headers("X-API-Key: ${com.example.mobiletrinity.Config.API_KEY}")
    @GET("api/health")
    suspend fun checkHealth(): HealthResponse
}

// ============ AGENT API SERVICE ============
interface AgentApiService {
    @GET("health")
    suspend fun checkHealth(): AgentHealthResponse

    @POST("tasks")
    suspend fun submitTask(@Body request: AgentTaskRequest): AgentTaskResponse

    @GET("tasks/{id}")
    suspend fun getTaskStatus(@Path("id") taskId: String): AgentTaskResponse

    @Headers("X-API-KEY: ${com.example.mobiletrinity.Config.API_KEY}")
    @POST("execute")
    suspend fun executeCommand(@Body command: CommandRequest): CommandResponse
}

// ============ DTO MODELS - WEB API ============
data class UserListResponse(
    @SerializedName("users")
    val users: List<UserDto>,
    @SerializedName("total")
    val total: Int
)

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class SystemStatsResponse(
    @SerializedName("total_users")
    val totalUsers: Int,
    @SerializedName("active_tasks")
    val activeTasks: Int,
    @SerializedName("system_health")
    val systemHealth: String,
    @SerializedName("agent_status")
    val agentStatus: String
)

data class TaskListResponse(
    @SerializedName("tasks")
    val tasks: List<TaskResponse>
)

data class TaskRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("priority")
    val priority: String
)

data class TaskResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class HealthResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("timestamp")
    val timestamp: String
)

// ============ DTO MODELS - AGENT API ============
data class AgentHealthResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("agent_version")
    val agentVersion: String,
    @SerializedName("connected")
    val connected: Boolean,
    @SerializedName("timestamp")
    val timestamp: String
)

data class AgentTaskRequest(
    @SerializedName("command")
    val command: String,
    @SerializedName("target")
    val target: String,
    @SerializedName("parameters")
    val parameters: Map<String, String> = emptyMap()
)

data class AgentTaskResponse(
    @SerializedName("task_id")
    val taskId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("result")
    val result: String,
    @SerializedName("timestamp")
    val timestamp: String
)

data class CommandRequest(
    @SerializedName("command")
    val command: String,
    @SerializedName("timeout")
    val timeout: Int = 30
)

data class CommandResponse(
    @SerializedName("output")
    val output: String,
    @SerializedName("exit_code")
    val exitCode: Int,
    @SerializedName("timestamp")
    val timestamp: String
)
