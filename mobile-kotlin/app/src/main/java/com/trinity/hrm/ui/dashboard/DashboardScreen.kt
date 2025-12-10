package com.trinity.hrm.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trinity.hrm.data.remote.ApiClient
import com.trinity.hrm.data.remote.LocalAuth
import com.trinity.hrm.data.remote.RoleHelper
import com.trinity.hrm.navigation.Screen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val userEmail = remember { mutableStateOf("") }
    val userRole = remember { mutableStateOf("Employee") }
    val currentUser = remember { mutableStateOf<com.trinity.hrm.data.remote.JsonBinClient.User?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val user = ApiClient.getCurrentUser()
            userEmail.value = user?.email ?: ""
            
            // Get full user object for role checking
            val localAuth = LocalAuth(context)
            currentUser.value = localAuth.getCurrentUser()
            
            // Set role display
            userRole.value = when {
                RoleHelper.isAdmin(currentUser.value) -> "Admin"
                RoleHelper.isDepartmentHead(currentUser.value) -> "Department Head"
                else -> "Employee"
            }
        }
    }
    
    // Auto-refresh stats to sync with web app
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(15000) // 15 seconds
            // Stats will update when user navigates to other screens
        }
    }
    
    val canAddEmployees = RoleHelper.canAddEmployees(currentUser.value)
    val canAddTasks = RoleHelper.canAddTasks(currentUser.value)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trinity HRM") },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            ApiClient.logout()
                            com.trinity.hrm.data.storage.DataStorage.clearAll()
                        }
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                if (canAddEmployees) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Screen.Employees.route) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Employees") },
                        label = { Text("Employees") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Screen.Departments.route) },
                        icon = { Icon(Icons.Default.Business, contentDescription = "Departments") },
                        label = { Text("Depts") }
                    )
                }
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Tasks.route) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Tasks") },
                    label = { Text("Tasks") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Attendance.route) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Attendance") },
                    label = { Text("Attendance") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Leaves.route) },
                    icon = { Icon(Icons.Default.Event, contentDescription = "Leaves") },
                    label = { Text("Leaves") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Messages.route) },
                    icon = { Icon(Icons.Default.Email, contentDescription = "Messages") },
                    label = { Text("Messages") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(
                                text = userEmail.value.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Column {
                        Text(
                            text = userEmail.value,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = userRole.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stats Grid with improved design
            if (canAddEmployees) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Employees",
                        value = "0",
                        icon = Icons.Default.Person,
                        color = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Employees.route) }
                    )
                    StatCard(
                        title = "Departments",
                        value = "0",
                        icon = Icons.Default.Business,
                        color = androidx.compose.ui.graphics.Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Departments.route) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Tasks",
                    value = "0",
                    icon = Icons.Default.List,
                    color = androidx.compose.ui.graphics.Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Tasks.route) }
                )
                StatCard(
                    title = "Attendance",
                    value = "0",
                    icon = Icons.Default.CalendarMonth,
                    color = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Attendance.route) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Leaves",
                    value = "0",
                    icon = Icons.Default.Event,
                    color = androidx.compose.ui.graphics.Color(0xFFEC4899),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Leaves.route) }
                )
                StatCard(
                    title = "Messages",
                    value = "0",
                    icon = Icons.Default.Email,
                    color = androidx.compose.ui.graphics.Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Messages.route) }
                )
            }

            // Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Your dashboard is ready. Start managing your HR operations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

