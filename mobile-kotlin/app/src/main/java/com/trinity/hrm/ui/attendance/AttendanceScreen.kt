package com.trinity.hrm.ui.attendance

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
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.trinity.hrm.data.model.Attendance
import com.trinity.hrm.data.repository.AttendanceRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen() {
    val scope = rememberCoroutineScope()
    val repository = remember { AttendanceRepository() }
    val auth = remember { FirebaseAuth.getInstance() }
    
    var attendanceList by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var todayAttendance by remember { mutableStateOf<Attendance?>(null) }
    
    val userId = auth.currentUser?.uid
    
    fun loadAttendance() {
        if (userId == null) return
        scope.launch {
            isLoading = true
            val result = repository.getAttendanceForUser(userId)
            result.onSuccess { list ->
                attendanceList = list.sortedByDescending { it.date }
                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                todayAttendance = list.find { it.date == today }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadAttendance()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance") },
                actions = {
                    IconButton(onClick = { loadAttendance() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        if (todayAttendance != null && todayAttendance?.checkOut == null) {
                            // Check Out
                             repository.checkOut().onSuccess {
                                 loadAttendance()
                             }
                        } else if (todayAttendance == null) {
                            // Check In
                            repository.checkIn().onSuccess {
                                loadAttendance()
                            }
                        }
                        isLoading = false
                    }
                },
                containerColor = if (todayAttendance != null && todayAttendance?.checkOut == null) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer,
                icon = { 
                    if (todayAttendance != null && todayAttendance?.checkOut == null) 
                        Icon(Icons.Default.Logout, "Check Out") 
                    else 
                        Icon(Icons.Default.Login, "Check In") 
                },
                text = { 
                    Text(
                        if (todayAttendance != null && todayAttendance?.checkOut == null) "Check Out" 
                        else if (todayAttendance?.checkOut != null) "Completed"
                        else "Check In"
                    ) 
                }
            )
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
                items(attendanceList) { item ->
                    AttendanceCard(item)
                }
            }
        }
    }
}

@Composable
fun AttendanceCard(attendance: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(attendance.date, style = MaterialTheme.typography.titleMedium)
                Text("Status: ${attendance.status}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("In: ${attendance.checkIn ?: "-"}")
                Text("Out: ${attendance.checkOut ?: "-"}")
            }
        }
    }
}
