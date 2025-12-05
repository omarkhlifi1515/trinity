package com.example.mobiletrinity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.mobiletrinity.api.StatusUpdateRequest
import com.example.mobiletrinity.api.TaskRequest
import com.example.mobiletrinity.network.RetrofitClient
import com.example.mobiletrinity.data.*
import com.example.mobiletrinity.ui.screens.TaskListScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: TaskRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Dual-Core Network Clients
        val webApiService = RetrofitClient.webApi
        val agentApiService = RetrofitClient.agentApi
        
        // Initialize Room Database
        val database = TaskDatabase.getInstance(this)
        val taskDao = database.taskDao()
        
        // Initialize Repository
        repository = TaskRepository(
            apiService = webApiService,
            taskDao = taskDao
        )
        
        setContent {
            var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var agentStatus by remember { mutableStateOf<String>("Disconnected") }
            var agentStatusColor by remember { mutableStateOf<Color>(Color.Red) }
            var totalUsers by remember { mutableStateOf<Int>(0) }
            var systemHealth by remember { mutableStateOf<String>("Unknown") }
            
            // Fetch system status from BOTH services on app startup
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    try {
                        // ============ CHECK AGENT STATUS (Agent API) ============
                        val agentHealth = agentApiService.checkHealth()
                        agentStatus = if (agentHealth.connected) "Connected" else "Disconnected"
                        agentStatusColor = if (agentHealth.connected) Color.Green else Color.Red
                        
                        // ============ FETCH WEB SYSTEM STATS (Web API) ============
                        val stats = webApiService.getStats()
                        totalUsers = stats.totalUsers
                        systemHealth = stats.systemHealth
                        
                        // ============ SYNC TASKS FROM WEB API ============
                        val remoteTasks = webApiService.getTasks()
                        tasks = remoteTasks.map { taskDto ->
                            Task(
                                id = taskDto.id,
                                title = taskDto.title,
                                description = taskDto.description,
                                priority = taskDto.priority,
                                status = taskDto.status,
                                dueDate = taskDto.dueDate,
                                createdAt = taskDto.createdAt
                            )
                        }
                        isLoading = false
                    } catch (e: Exception) {
                        println("Error fetching system status: ${e.message}")
                        agentStatus = "Error"
                        agentStatusColor = Color.Yellow
                        isLoading = false
                    }
                }
            }
            
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x0f172a))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // System Status Bar
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x1e1e1e))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Agent Status", fontSize = 12.sp, color = Color.Gray)
                                    Text(agentStatus, fontSize = 14.sp, color = agentStatusColor)
                                }
                                Column {
                                    Text("Total Users", fontSize = 12.sp, color = Color.Gray)
                                    Text("$totalUsers", fontSize = 14.sp, color = Color.Cyan)
                                }
                                Column {
                                    Text("System Health", fontSize = 12.sp, color = Color.Gray)
                                    Text(systemHealth, fontSize = 14.sp, color = Color.Green)
                                }
                            }
                        }
                        
                        // Task List
                        TaskListScreen(
                            tasks = tasks,
                            isLoading = isLoading,
                            onTaskCreate = { title, desc, priority, _ ->
                                lifecycleScope.launch {
                                    try {
                                        // Create task via WEB API
                                        val taskRequest = TaskRequest(
                                            title = title,
                                            description = desc,
                                            priority = priority
                                        )
                                        webApiService.createTask(taskRequest)
                                        // Refresh task list
                                        val updatedTasks = webApiService.getTasks()
                                        tasks = updatedTasks.map { taskDto ->
                                            Task(
                                                id = taskDto.id,
                                                title = taskDto.title,
                                                description = taskDto.description,
                                                priority = taskDto.priority,
                                                status = taskDto.status,
                                                dueDate = taskDto.dueDate,
                                                createdAt = taskDto.createdAt
                                            )
                                        }
                                    } catch (e: Exception) {
                                        println("Error creating task: ${e.message}")
                                    }
                                }
                            },
                            onTaskEdit = { /* Placeholder */ },
                            onTaskDelete = { task ->
                                lifecycleScope.launch {
                                    try {
                                        webApiService.deleteTask(task.id)
                                        // Refresh task list
                                        val updatedTasks = webApiService.getTasks()
                                        tasks = updatedTasks.map { taskDto ->
                                            Task(
                                                id = taskDto.id,
                                                title = taskDto.title,
                                                description = taskDto.description,
                                                priority = taskDto.priority,
                                                status = taskDto.status,
                                                dueDate = taskDto.dueDate,
                                                createdAt = taskDto.createdAt
                                            )
                                        }
                                    } catch (e: Exception) {
                                        println("Error deleting task: ${e.message}")
                                    }
                                }
                            },
                            onTaskStatusChange = { task, newStatus ->
                                lifecycleScope.launch {
                                    try {
                                        val request = StatusUpdateRequest(task.id, newStatus)
                                        webApiService.updateTaskStatus(task.id, request)
                                        // Refresh task list
                                        val updatedTasks = webApiService.getTasks()
                                        tasks = updatedTasks.map { taskDto ->
                                            Task(
                                                id = taskDto.id,
                                                title = taskDto.title,
                                                description = taskDto.description,
                                                priority = taskDto.priority,
                                                status = taskDto.status,
                                                dueDate = taskDto.dueDate,
                                                createdAt = taskDto.createdAt
                                            )
                                        }
                                    } catch (e: Exception) {
                                        println("Error updating task: ${e.message}")
                                    }
                                }
                            },
                            onRefresh = {
                                lifecycleScope.launch {
                                    try {
                                        val updatedTasks = webApiService.getTasks()
                                        tasks = updatedTasks.map { taskDto ->
                                            Task(
                                                id = taskDto.id,
                                                title = taskDto.title,
                                                description = taskDto.description,
                                                priority = taskDto.priority,
                                                status = taskDto.status,
                                                dueDate = taskDto.dueDate,
                                                createdAt = taskDto.createdAt
                                            )
                                        }
                                    } catch (e: Exception) {
                                        println("Error refreshing tasks: ${e.message}")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
