package com.trinity.hrm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.trinity.hrm.data.remote.ApiClient
import com.trinity.hrm.data.remote.SupabaseClient
import com.trinity.hrm.data.storage.DataStorage
import com.trinity.hrm.navigation.AppNavigation
import com.trinity.hrm.ui.theme.TrinityHRMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize Supabase (replace with your credentials)
            // TODO: Set your Supabase URL and anon key here or via BuildConfig
            // For now, skip Supabase if credentials are not set
            val supabaseUrl = "YOUR_SUPABASE_URL" // e.g., "https://xxxxx.supabase.co"
            val supabaseAnonKey = "YOUR_SUPABASE_ANON_KEY" // e.g., "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            
            if (supabaseUrl != "YOUR_SUPABASE_URL" && supabaseAnonKey != "YOUR_SUPABASE_ANON_KEY") {
                SupabaseClient.setCredentials(this, supabaseUrl, supabaseAnonKey)
                SupabaseClient.initialize(this)
            } else {
                println("⚠️ Supabase credentials not configured. Using local storage only.")
            }
            
            // Initialize ApiClient and DataStorage with context
            ApiClient.initialize(this)
            DataStorage.initialize(this)
        } catch (e: Exception) {
            println("⚠️ Error initializing app: ${e.message}")
            e.printStackTrace()
            // Continue anyway - app will use local storage
            try {
                ApiClient.initialize(this)
                DataStorage.initialize(this)
            } catch (e2: Exception) {
                println("⚠️ Critical error: ${e2.message}")
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

