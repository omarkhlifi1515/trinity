package com.example.mobiletrinity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrinity.data.Task
import com.example.mobiletrinity.ui.components.TaskCard

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    isLoading: Boolean,
    onTaskCreate: (String, String, String, String) -> Unit,
    onTaskEdit: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onTaskStatusChange: (Task, String) -> Unit,
    onRefresh: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("all") }
    
    val filteredTasks = when (selectedFilter) {
        "completed" -> tasks.filter { it.status == "completed" }
        "in_progress" -> tasks.filter { it.status == "in_progress" }
        "todo" -> tasks.filter { it.status == "todo" }
        else -> tasks
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x0f172a)), // Dark background
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xa78bfa), // Purple
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Task", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0x0f172a))
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1e293b)) // Slate-800
                    .padding(16.dp)
            ) {
                Text(
                    text = "Trinity Tasks",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "AI-Powered Workplace OS",
                    fontSize = 12.sp,
                    color = Color(0x80deea), // Cyan glow
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Filter Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all", "todo", "in_progress", "completed").forEach { status ->
                    FilterButton(
                        label = status.replace("_", " ").replaceFirstChar { it.uppercase() },
                        isSelected = selectedFilter == status,
                        onClick = { selectedFilter = status }
                    )
                }
            }
            
            // Task List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xa78bfa))
                }
            } else if (filteredTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tasks found",
                        color = Color(0xb0bec5),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskCard(
                            task = task,
                            onEdit = onTaskEdit,
                            onDelete = onTaskDelete,
                            onStatusChange = onTaskStatusChange
                        )
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, priority, dueDate ->
                onTaskCreate(title, desc, priority, dueDate)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun FilterButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xa78bfa) else Color(0x1e293b)
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun CreateTaskDialog(onDismiss: () -> Unit, onCreate: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    var dueDate by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0x1e293b),
        title = {
            Text("Create New Task", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = Color(0x80deea)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = Color(0x80deea)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White)
                )
                
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)", color = Color(0x80deea)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, description, priority, dueDate) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xa78bfa))
            ) {
                Text("Create", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1e293b))
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
