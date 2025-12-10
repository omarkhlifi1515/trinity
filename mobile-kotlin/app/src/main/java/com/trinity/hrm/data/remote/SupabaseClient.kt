package com.trinity.hrm.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.android.Android
import android.content.Context
import android.content.SharedPreferences

/**
 * Supabase Client for Kotlin
 * Provides real-time sync and better data access than JSONBin.io
 */
object SupabaseClient {
    private const val SUPABASE_URL_KEY = "SUPABASE_URL"
    private const val SUPABASE_ANON_KEY_KEY = "SUPABASE_ANON_KEY"
    
    private var client: SupabaseClient? = null
    private lateinit var prefs: SharedPreferences
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences("trinity_supabase", Context.MODE_PRIVATE)
        
        val supabaseUrl = prefs.getString(SUPABASE_URL_KEY, "") ?: ""
        val supabaseAnonKey = prefs.getString(SUPABASE_ANON_KEY_KEY, "") ?: ""
        
        if (supabaseUrl.isNotEmpty() && supabaseAnonKey.isNotEmpty()) {
            client = createSupabaseClient(
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseAnonKey
            ) {
                install(Postgrest)
                install(Realtime)
                httpEngine = Android.create()
            }
        } else {
            println("⚠️ Supabase credentials not set. Please configure SUPABASE_URL and SUPABASE_ANON_KEY")
        }
    }
    
    fun getClient(): SupabaseClient? {
        return client
    }
    
    fun getClientOrThrow(): SupabaseClient {
        return client ?: throw IllegalStateException("SupabaseClient not initialized. Call initialize(context) first.")
    }
    
    fun setCredentials(context: Context, url: String, anonKey: String) {
        if (!::prefs.isInitialized) {
            prefs = context.getSharedPreferences("trinity_supabase", Context.MODE_PRIVATE)
        }
        prefs.edit()
            .putString(SUPABASE_URL_KEY, url)
            .putString(SUPABASE_ANON_KEY_KEY, anonKey)
            .apply()
    }
    
    fun isInitialized(): Boolean {
        return client != null
    }
}

