package com.example.smarthr_app.data.remote

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.android.Android

object SupabaseInstance {
    
    // Supabase project configuration
    private const val SUPABASE_URL = "https://nghwpwajcoofbgvsevgf.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx"
    
    private var supabaseClient: SupabaseClient? = null
    
    fun initialize(context: Context? = null) {
        if (supabaseClient == null) {
            supabaseClient = createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_ANON_KEY
            ) {
                install(Auth)
                install(Postgrest)
                install(Storage)
                httpEngine = Android.create()
            }
        }
    }
    
    fun getClient(): SupabaseClient {
        if (supabaseClient == null) {
            initialize()
        }
        return supabaseClient!!
    }
}

