package com.trinity.hrm.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Firebase Client for Trinity HRM
 * Provides authentication and database access
 */
object FirebaseClient {
    
    private var isInitialized = false
    
    // Firebase Auth instance
    val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    
    // Firestore database instance
    val firestore: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    
    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                // Initialize Firebase if not already initialized
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
                isInitialized = true
                println("✅ Firebase initialized successfully")
            } catch (e: Exception) {
                println("⚠️ Firebase initialization error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun isInitialized(): Boolean = isInitialized
}
