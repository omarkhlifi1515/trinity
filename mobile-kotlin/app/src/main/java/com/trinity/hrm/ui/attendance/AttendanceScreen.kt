package com.trinity.hrm.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trinity.hrm.data.model.Attendance
import com.trinity.hrm.data.remote.LocalAuth
import com.trinity.hrm.data.storage.DataStorage
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen() {
    val context = LocalContext.current
    val currentUser = remember { mutableStateOf<com.trinity.hrm.data.remote.JsonBinClient.User?>(null) }
    val attendanceList = remember { mutableStateOf<List<Attendance>>(emptyList()) }
    val showMarkDialog = remember { mutableStateOf(false) }
    val refreshTrigger = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize DataStorage
    LaunchedEffect(Unit) {
        com.trinity.hrm.data.storage.DataStorage.initialize(context)
    }
    
    LaunchedEffect(refreshTrigger.value) {
        val localAuth = LocalAuth(context)
        currentUser.value = localAuth.getCurrentUser()
        
        coroutineScope.launch {
            attendanceList.value = DataStorage.getAttendance()
        }
    }
    
    // Auto-refresh to sync with web app
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // 10 seconds
            coroutineScope.launch {
                attendanceList.value = DataStorage.getAttendance()
            }
        }
    }
    
    val currentUserId = currentUser.value?.id ?: ""
    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    val todayAttendance = attendanceList.value.find { it.date == today && it.employeeId == currentUserId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Attendance")
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${attendanceList.value.size}",
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
            if (todayAttendance == null) {
                FloatingActionButton(
                    onClick = { showMarkDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Mark Attendance")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Today's status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (todayAttendance != null)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Today's Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (todayAttendance != null) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "✓ Marked",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (todayAttendance.checkIn != null) {
                            Text(
                                text = "Check-in: ${todayAttendance.checkIn}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (todayAttendance.checkOut != null) {
                            Text(
                                text = "Check-out: ${todayAttendance.checkOut}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(
                            text = "Not marked yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showMarkDialog.value = true },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Attendance")
                        }
                    }
                }
            }
            
            // Attendance history
            if (attendanceList.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = "No Attendance History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your attendance records will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(attendanceList.value.sortedByDescending { it.date }) { attendance ->
                        AttendanceCard(attendance = attendance)
                    }
                }
            }
        }
    }
    
    if (showMarkDialog.value) {
        MarkAttendanceDialog(
            currentUserId = currentUserId,
            onDismiss = { showMarkDialog.value = false },
            onMark = { attendance ->
                coroutineScope.launch {
                    if (DataStorage.markAttendance(attendance)) {
                        refreshTrigger.value++
                    }
                }
            }
        )
    }
}

@Composable
fun AttendanceCard(attendance: Attendance) {
    val statusColor = when (attendance.status) {
        com.trinity.hrm.data.model.AttendanceStatus.PRESENT -> androidx.compose.ui.graphics.Color(0xFF10B981)
        com.trinity.hrm.data.model.AttendanceStatus.ABSENT -> androidx.compose.ui.graphics.Color(0xFFDC2626)
        com.trinity.hrm.data.model.AttendanceStatus.LATE -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
        com.trinity.hrm.data.model.AttendanceStatus.HALF_DAY -> androidx.compose.ui.graphics.Color(0xFF6366F1)
        com.trinity.hrm.data.model.AttendanceStatus.ON_LEAVE -> androidx.compose.ui.graphics.Color(0xFF8B5CF6)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attendance.date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (attendance.checkIn != null) {
                    Text(
                        text = "In: ${attendance.checkIn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (attendance.checkOut != null) {
                    Text(
                        text = "Out: ${attendance.checkOut}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = statusColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = attendance.status.name.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MarkAttendanceDialog(
    currentUserId: String,
    onDismiss: () -> Unit,
    onMark: (Attendance) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(com.trinity.hrm.data.model.AttendanceStatus.PRESENT) }
    var checkInTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val currentTime = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Mark Attendance",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "Date: $today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Status selector
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.trinity.hrm.data.model.AttendanceStatus.values().forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.name.replace("_", " ")) }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = checkInTime,
                    onValueChange = { checkInTime = it },
                    label = { Text("Check-in Time") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(currentTime) },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { checkInTime = currentTime }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Use Current Time")
                        }
                    }
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 3
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val attendance = Attendance(
                                id = System.currentTimeMillis().toString(),
                                employeeId = currentUserId,
                                date = today,
                                checkIn = if (checkInTime.isNotBlank()) checkInTime else currentTime,
                                status = selectedStatus,
                                notes = if (notes.isNotBlank()) notes else null
                            )
                            onMark(attendance)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark")
                    }
                }
            }
        }
    }
}


