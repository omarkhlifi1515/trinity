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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trinity.hrm.data.model.Leave
import com.trinity.hrm.data.storage.DataStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeavesScreen() {
    val context = LocalContext.current
    val currentUser = remember { mutableStateOf<com.trinity.hrm.data.remote.ApiClient.User?>(null) }
    val leaves = remember { mutableStateOf<List<Leave>>(emptyList()) }
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
            leaves.value = DataStorage.getLeaves()
        }
    }
    
    // Auto-refresh to sync with web app
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // 10 seconds
            coroutineScope.launch {
                leaves.value = DataStorage.getLeaves()
            }
        }
    }
    
    val currentUserId = currentUser.value?.id ?: ""
    val canApprove = true // RoleHelper.canAddTasks(currentUser.value) 
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Leaves")
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${leaves.value.size}",
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
            FloatingActionButton(
                onClick = { showAddDialog.value = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Request Leave")
            }
        }
    ) { paddingValues ->
        if (leaves.value.isEmpty()) {
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
                                Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "No Leave Requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap the + button to submit your first leave request",
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
                items(leaves.value) { leave ->
                    LeaveCard(
                        leave = leave,
                        canApprove = canApprove && leave.status == com.trinity.hrm.data.model.LeaveStatus.PENDING,
                        onApprove = {
                            coroutineScope.launch {
                                if (DataStorage.approveLeave(leave.id, currentUserId)) {
                                    refreshTrigger.value++
                                }
                            }
                        },
                        onReject = {
                            coroutineScope.launch {
                                if (DataStorage.rejectLeave(leave.id, currentUserId)) {
                                    refreshTrigger.value++
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showAddDialog.value) {
        AddLeaveDialog(
            currentUserId = currentUserId,
            onDismiss = { showAddDialog.value = false },
            onAdd = { leave ->
                coroutineScope.launch {
                    if (DataStorage.addLeave(leave)) {
                        refreshTrigger.value++
                    }
                }
            }
        )
    }
}

@Composable
fun LeaveCard(
    leave: Leave,
    canApprove: Boolean = false,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    val statusColor = when (leave.status) {
        com.trinity.hrm.data.model.LeaveStatus.APPROVED -> androidx.compose.ui.graphics.Color(0xFF10B981)
        com.trinity.hrm.data.model.LeaveStatus.REJECTED -> androidx.compose.ui.graphics.Color(0xFFDC2626)
        com.trinity.hrm.data.model.LeaveStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
        com.trinity.hrm.data.model.LeaveStatus.CANCELLED -> androidx.compose.ui.graphics.Color(0xFF6B7280)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leave.type.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = leave.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${leave.startDate} - ${leave.endDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (leave.reason != null) {
                Text(
                    text = leave.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Approval buttons for Admin/Dept Head
            if (canApprove) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFFDC2626)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                }
            }
        }
    }
}


