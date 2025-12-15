package com.trinity.hrm.ui.leaves

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.trinity.hrm.data.model.Leave
import com.trinity.hrm.data.model.LeaveType
import com.trinity.hrm.data.repository.LeaveRepository
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeavesScreen() {
    val scope = rememberCoroutineScope()
    val repository = remember { LeaveRepository() }
    val auth = remember { FirebaseAuth.getInstance() }
    
    var leaves by remember { mutableStateOf<List<Leave>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    val userId = auth.currentUser?.uid
    
    fun loadLeaves() {
        if (userId == null) return
        scope.launch {
            isLoading = true
            val result = repository.getLeavesForUser(userId)
            result.onSuccess { list ->
                leaves = list.sortedByDescending { it.createdAt }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadLeaves()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaves") },
                actions = {
                    IconButton(onClick = { loadLeaves() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Request Leave")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leaves) { leave ->
                    LeaveCard(leave)
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddLeaveDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, start, end, reason ->
                scope.launch {
                    repository.requestLeave(type, start, end, reason)
                    loadLeaves()
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun LeaveCard(leave: Leave) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(text = leave.type.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${leave.startDate} - ${leave.endDate}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${leave.status}", style = MaterialTheme.typography.labelMedium)
            if (!leave.reason.isNullOrEmpty()) {
                Text(text = "Reason: ${leave.reason}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AddLeaveDialog(onDismiss: () -> Unit, onConfirm: (LeaveType, Date, Date, String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    // Simplified date selection for MVP - just using current dates or placeholders
    // In a real app, use a DatePicker
    val startDate = Date()
    val endDate = Date() 
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Leave") },
        text = {
            Column {
                Text("Type: Vacation (Default)")
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") }
                )
                Text("Dates: Today (Placeholder)", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(LeaveType.VACATION, startDate, endDate, reason) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
