package com.example.mobiletrinity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.example.mobiletrinity.api.ApiService
import com.example.mobiletrinity.data.*
import com.example.mobiletrinity.ui.screens.TaskListScreen
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    
    private lateinit var apiService: ApiService
    private lateinit var repository: TaskRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(System.getenv("AGENT_SERVER_URL") ?: "http://localhost:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ApiService::class.java)
        
        // Initialize Room Database
        val database = TaskDatabase.getInstance(this)
        val taskDao = database.taskDao()
        
        // Initialize Repository
        repository = TaskRepository(apiService, taskDao)
        
        setContent {
            var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    repository.syncTasks().onSuccess {
                        repository.getTasksFlow().collect { fetchedTasks ->
                            tasks = fetchedTasks
                            isLoading = false
                        }
                    }.onFailure {
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
                    TaskListScreen(
                        tasks = tasks,
                        isLoading = isLoading,
                        onTaskCreate = { title, desc, priority, dueDate ->
                            lifecycleScope.launch {
                                repository.createTask(title, desc, priority, dueDate)
                                    .onSuccess {
                                        repository.syncTasks()
                                    }
                            }
                        },
                        onTaskEdit = { /* Placeholder for edit functionality */ },
                        onTaskDelete = { task ->
                            lifecycleScope.launch {
                                repository.deleteTask(task.id)
                                    .onSuccess {
                                        repository.syncTasks()
                                    }
                            }
                        },
                        onTaskStatusChange = { task, newStatus ->
                            lifecycleScope.launch {
                                repository.updateTaskStatus(task.id, newStatus)
                                    .onSuccess {
                                        repository.syncTasks()
                                    }
                            }
                        },
                        onRefresh = {
                            lifecycleScope.launch {
                                repository.syncTasks()
                            }
                        }
                    )
                }
            }
        }
    }
}
