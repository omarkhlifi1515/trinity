package com.example.mobiletrinity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletrinity.data.Task

@Composable
fun TaskCard(
    task: Task,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onStatusChange: (Task, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1a1f2e) // Dark card background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title + Priority Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                PriorityBadge(task.priority)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = task.description,
                fontSize = 14.sp,
                color = Color(0xb0bec5),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status + Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status: ${task.status}",
                    fontSize = 12.sp,
                    color = Color(0x80deea),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    fontSize = 12.sp,
                    color = Color(0xf48fb1)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusDropdown(task.status) { newStatus ->
                    onStatusChange(task, newStatus)
                }
                
                IconButton(onClick = { onEdit(task) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xa78bfa))
                }
                
                IconButton(onClick = { onDelete(task) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xff6b6b))
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val bgColor = when (priority.lowercase()) {
        "high" -> Color(0xffb74d4d)
        "medium" -> Color(0xffa78bfa)
        else -> Color(0xff80deea)
    }
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = priority,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusDropdown(currentStatus: String, onStatusSelected: (String) -> Unit) {
    var expanded by androidx.compose.runtime.mutableStateOf(false)
    val statusOptions = listOf("todo", "in_progress", "completed")
    
    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x38bdf8))
        ) {
            Text(text = currentStatus, color = Color.White, fontSize = 12.sp)
        }
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            statusOptions.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status, color = Color.White) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
