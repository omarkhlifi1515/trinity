package com.example.smarthr_app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.data.repository.ChatRepository
import com.example.smarthr_app.presentation.navigation.NavGraph
import com.example.smarthr_app.presentation.navigation.Screen
import com.example.smarthr_app.presentation.theme.SmartHRTheme
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.utils.notification.createNotificationChannel
import com.example.smarthr_app.utils.notification.showNotification
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(applicationContext)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            SmartHRTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartHRApp()
                }
            }
        }
    }
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}

@Composable
fun SmartHRApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    
    // Initialize RetrofitInstance with context for automatic token injection
    // Initialize SupabaseInstance for offline mode
    LaunchedEffect(Unit) {
        com.example.smarthr_app.data.remote.RetrofitInstance.initialize(context)
        com.example.smarthr_app.data.remote.SupabaseInstance.initialize(context)
    }
    
    val authRepository = AuthRepository(dataStoreManager)

    // 1. Collect the User State (Used for deciding start destination)
    val userState by authRepository.user.collectAsState(initial = null)
    val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = false)

    // ... (Keep your chatViewModel / notification logic here) ...

    var startDestination by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // 2. Logic to determine start destination
    LaunchedEffect(isLoggedIn, userState) {
        delay(100) // Allow DataStore to load
        if (isLoggedIn && userState != null) {
            startDestination = if (userState?.role == "HR" || userState?.role == "Admin") {
                Screen.HRDashboard.route
            } else {
                Screen.EmployeeDashboard.route
            }
        } else {
            startDestination = Screen.Login.route
        }
        isInitialized = true
    }

    if (isInitialized && startDestination != null) {
        NavGraph(
            navController = navController,
            startDestination = startDestination!!
            // REMOVED: user = userState  <-- This line was causing the error
        )
    }
}
