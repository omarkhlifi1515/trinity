package com.trinity.hrm.data.remote

import android.content.Context
import kotlinx.serialization.Serializable

/**
 * Local Authentication API Client
 * Uses JSONBin.io for shared data storage with web and React Native apps
 * No web app connection needed!
 * 
 * All three apps share the same JSONBin.io database:
 * - Web app (Next.js)
 * - React Native app
 * - Kotlin app (Android)
 */
object ApiClient {
    // Use LocalAuth for authentication
    private var localAuth: LocalAuth? = null
    
    fun initialize(context: Context) {
        localAuth = LocalAuth(context)
    }
    
    @Serializable
    data class User(
        val id: String,
        val email: String
    )
    
    @Serializable
    data class AuthResponse(
        val user: User
    )
    
    suspend fun login(email: String, password: String): AuthResponse {
        val auth = localAuth ?: throw IllegalStateException("ApiClient not initialized. Call initialize(context) first.")
        val user = auth.login(email, password)
        return AuthResponse(User(user.id, user.email))
    }
    
    suspend fun signup(email: String, password: String): AuthResponse {
        val auth = localAuth ?: throw IllegalStateException("ApiClient not initialized. Call initialize(context) first.")
        val user = auth.signup(email, password)
        return AuthResponse(User(user.id, user.email))
    }
    
    fun logout() {
        localAuth?.logout()
    }
    
    fun getCurrentUser(): User? {
        val user = localAuth?.getCurrentUser() ?: return null
        return User(user.id, user.email)
    }
}

