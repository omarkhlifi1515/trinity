package com.trinity.hrm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.trinity.hrm.data.remote.ApiClient
import com.trinity.hrm.data.storage.DataStorage
import com.trinity.hrm.navigation.AppNavigation
import com.trinity.hrm.ui.theme.TrinityHRMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Initialize Firebase (automatically uses google-services.json)
            println("🔥 Initializing Firebase...")
            ApiClient.initialize(this)
            DataStorage.initialize(this)
            println("✅ Firebase and DataStorage initialized successfully")
        } catch (e: Exception) {
            println("⚠️ Error initializing app: ${e.message}")
            e.printStackTrace()
            // Continue anyway - app will use local storage
            try {
                DataStorage.initialize(this)
            } catch (e2: Exception) {
                println("⚠️ DataStorage initialization also failed: ${e2.message}")
                e2.printStackTrace()
            }
        }
        
        setContent {
            TrinityHRMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

