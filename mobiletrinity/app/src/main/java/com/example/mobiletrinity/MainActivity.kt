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
import com.example.mobiletrinity.network.RetrofitClient
import com.example.mobiletrinity.data.*
import com.example.mobiletrinity.ui.screens.TaskListScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: TaskRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Dual-Core Network Clients
        // webApi client: connects to https://trinity-web-04bi.onrender.com/
        // agentApi client: connects to https://trinity-agent.onrender.com/
        val webApiService = RetrofitClient.webApi
        val agentApiService = RetrofitClient.agentApi
        
        // Initialize Room Database
        val database = TaskDatabase.getInstance(this)
        val taskDao = database.taskDao()
        
        // Initialize Repository (uses web API for task CRUD operations)
        repository = TaskRepository(
            apiService = TODO("Remove after refactoring"),
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
                        val taskList = webApiService.getTasks()
                        tasks = taskList.tasks.map { taskDto ->
                            Task(
                                id = taskDto.id,
                                title = taskDto.title,
                                description = "Remote task",
                                priority = "High",
                                dueDate = taskDto.createdAt,
                                status = taskDto.status
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
                            onTaskCreate = { title, desc, priority, dueDate ->
                                lifecycleScope.launch {
                                    try {
                                        // Create task via WEB API
                                        val taskRequest = com.example.mobiletrinity.network.TaskRequest(
                                            title = title,
                                            description = desc,
                                            priority = priority
                                        )
                                        val response = webApiService.createTask(taskRequest)
                                        // Refresh task list
                                        val updatedTasks = webApiService.getTasks()
                                        tasks = updatedTasks.tasks.map { it }
                                    } catch (e: Exception) {
                                        println("Error creating task: ${e.message}")
                                    }
                                }
                            },
                            onTaskEdit = { /* Placeholder */ },
                            onTaskDelete = { task ->
                                lifecycleScope.launch {
                                    // Delete task (implement endpoint as needed)
                                    println("Deleting task: ${task.id}")
                                }
                            },
                            onTaskStatusChange = { task, newStatus ->
                                lifecycleScope.launch {
                                    // Update task status
                                    println("Updated task ${task.id} to $newStatus")
                                }
                            },
                            onRefresh = {
                                lifecycleScope.launch {
                                    try {
                                        val updatedTasks = webApiService.getTasks()
                                        tasks = updatedTasks.tasks.map { it }
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
