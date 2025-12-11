package com.trinity.hrm.data.remote

import android.content.Context
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

/**
 * Unified API Client
 * Uses Firebase Auth for authentication across Web and Mobile apps
 */
object ApiClient {

    fun initialize(context: Context) {
        FirebaseClient.initialize(context)
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
    
    suspend fun login(userEmail: String, userPassword: String): AuthResponse {
        try {
            val result = FirebaseClient.auth.signInWithEmailAndPassword(userEmail, userPassword).await()
            val firebaseUser = result.user ?: throw Exception("Login succeeded but user is null")
            
            return AuthResponse(User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: userEmail
            ))
        } catch (e: FirebaseAuthException) {
            throw Exception("Login failed: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Login failed: ${e.message}")
        }
    }
    
    suspend fun signup(userEmail: String, userPassword: String): AuthResponse {
        try {
            val result = FirebaseClient.auth.createUserWithEmailAndPassword(userEmail, userPassword).await()
            val firebaseUser = result.user ?: throw Exception("Signup succeeded but user is null")
            
            return AuthResponse(User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: userEmail
            ))
        } catch (e: FirebaseAuthException) {
            throw Exception("Signup failed: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Signup failed: ${e.message}")
        }
    }
    
    suspend fun logout() {
        try {
            FirebaseClient.auth.signOut()
        } catch (e: Exception) {
            println("Logout error: ${e.message}")
        }
    }
    
    suspend fun getCurrentUser(): User? {
        val firebaseUser = FirebaseClient.auth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: ""
        )
    }
}
