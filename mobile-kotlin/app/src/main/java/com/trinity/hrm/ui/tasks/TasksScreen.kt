package com.trinity.hrm.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trinity.hrm.data.model.Task
import com.trinity.hrm.data.remote.ApiClient
import com.trinity.hrm.data.storage.DataStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen() {
    val context = LocalContext.current
    val currentUser = remember { mutableStateOf<com.trinity.hrm.data.remote.ApiClient.User?>(null) }
    val tasks = remember { mutableStateOf<List<Task>>(emptyList()) }
    val employees = remember { mutableStateOf<List<com.trinity.hrm.data.model.Employee>>(emptyList()) }
    val showAddDialog = remember { mutableStateOf(false) }
    val refreshTrigger = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize DataStorage
    LaunchedEffect(Unit) {
        com.trinity.hrm.data.storage.DataStorage.initialize(context)
    }
    
    LaunchedEffect(refreshTrigger.value) {
        coroutineScope.launch {
            currentUser.value = com.trinity.hrm.data.remote.ApiClient.getCurrentUser()
            // Load data (syncs from cloud automatically)
            tasks.value = DataStorage.getTasks()
            employees.value = DataStorage.getEmployees()
        }
    }
    
    // Auto-refresh to sync with web app
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // 10 seconds
            coroutineScope.launch {
                tasks.value = DataStorage.getTasks()
            }
        }
    }
    
    val canAdd = true // RoleHelper.canAddTasks(currentUser.value)
    val currentUserId = currentUser.value?.id ?: ""
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tasks")
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${tasks.value.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (canAdd) {
                FloatingActionButton(
                    onClick = { showAddDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { paddingValues ->
        if (tasks.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "No Tasks Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (canAdd) "Tap the + button to create your first task" else "No tasks assigned yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks.value) { task ->
                    TaskCard(task = task, employees = employees.value)
                }
            }
        }
    }
    
    if (showAddDialog.value && employees.value.isNotEmpty()) {
        AddTaskDialog(
            employees = employees.value,
            currentUserId = currentUserId,
            onDismiss = { showAddDialog.value = false },
            onAdd = { task ->
                coroutineScope.launch {
                    if (DataStorage.addTask(task)) {
                        refreshTrigger.value++
                    }
                }
            }
        )
    }
}

@Composable
fun TaskCard(task: Task, employees: List<com.trinity.hrm.data.model.Employee>) {
    val employee = employees.find { it.id == task.assignedTo }
    val priorityColor = when (task.priority) {
        com.trinity.hrm.data.model.TaskPriority.URGENT -> androidx.compose.ui.graphics.Color(0xFFDC2626)
        com.trinity.hrm.data.model.TaskPriority.HIGH -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
        com.trinity.hrm.data.model.TaskPriority.MEDIUM -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
        com.trinity.hrm.data.model.TaskPriority.LOW -> androidx.compose.ui.graphics.Color(0xFF10B981)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (task.description != null) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = priorityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = task.priority.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = task.status.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (employee != null) {
                    Text(
                        text = "→ ${employee.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (task.dueDate != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due: ${task.dueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


