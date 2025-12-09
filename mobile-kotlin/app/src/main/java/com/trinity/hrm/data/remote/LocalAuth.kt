package com.trinity.hrm.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.Base64

/**
 * Local Authentication for Kotlin App
 * Uses JSONBin.io for shared data storage
 * No web app connection needed!
 */
class LocalAuth(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trinity_auth", Context.MODE_PRIVATE)
    private val USERS_CACHE_KEY = "users_cache"
    private val CURRENT_USER_KEY = "current_user"
    
    // Simple password hashing (same as web app)
    private fun hashPassword(password: String): String {
        // Use base64 encoding (same as web app)
        // In production, use bcrypt
        return Base64.getEncoder().encodeToString(password.toByteArray())
    }
    
    private fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return hashPassword(password) == hashedPassword
    }
    
    // Load users from JSONBin
    private suspend fun loadUsers(): List<JsonBinClient.User> {
        return withContext(Dispatchers.IO) {
            try {
                // Try JSONBin first
                val jsonbinUsers = JsonBinClient.readUsers()
                if (jsonbinUsers.isNotEmpty()) {
                    // Cache locally
                    val json = Json.encodeToString(jsonbinUsers)
                    prefs.edit().putString(USERS_CACHE_KEY, json).apply()
                    return@withContext jsonbinUsers
                }
                
                // Fallback to local cache
                val cached = prefs.getString(USERS_CACHE_KEY, null)
                if (cached != null) {
                    return@withContext Json.decodeFromString<List<JsonBinClient.User>>(cached)
                }
                
                emptyList()
            } catch (e: Exception) {
                println("Error loading users: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Save users to JSONBin
    private suspend fun saveUsers(users: List<JsonBinClient.User>) {
        withContext(Dispatchers.IO) {
            try {
                // Save to JSONBin
                JsonBinClient.writeUsers(users)
                // Also cache locally
                val json = Json.encodeToString(users)
                prefs.edit().putString(USERS_CACHE_KEY, json).apply()
            } catch (e: Exception) {
                println("Error saving users: ${e.message}")
            }
        }
    }
    
    // Login
    suspend fun login(email: String, password: String): JsonBinClient.User {
        val users = loadUsers()
        val user = users.find { it.email == email }
        
        if (user == null || user.password == null) {
            throw Exception("Invalid email or password")
        }
        
        if (!verifyPassword(password, user.password)) {
            throw Exception("Invalid email or password")
        }
        
        // Store user session (without password)
        val userWithoutPassword = JsonBinClient.User(
            id = user.id,
            email = user.email,
            password = null,
            createdAt = user.createdAt
        )
        
        val json = Json.encodeToString(userWithoutPassword)
        prefs.edit().putString(CURRENT_USER_KEY, json).apply()
        
        return userWithoutPassword
    }
    
    // Signup
    suspend fun signup(email: String, password: String): JsonBinClient.User {
        val users = loadUsers()
        
        // Check if user exists
        if (users.any { it.email == email }) {
            throw Exception("User already exists")
        }
        
        // Determine role based on email
        var role = JsonBinClient.UserRole.EMPLOYEE
        var department: String? = null
        
        if (email.lowercase() == "admin@gmail.com") {
            role = JsonBinClient.UserRole.ADMIN
        }
        // You can add more logic here to assign DEPARTMENT_HEAD role
        
        // Create new user
        val newUser = JsonBinClient.User(
            id = System.currentTimeMillis().toString(),
            email = email,
            password = hashPassword(password),
            createdAt = java.time.Instant.now().toString(),
            role = role,
            department = department
        )
        
        val updatedUsers = users + newUser
        saveUsers(updatedUsers)
        
        // Store user session (without password)
        val userWithoutPassword = JsonBinClient.User(
            id = newUser.id,
            email = newUser.email,
            password = null,
            createdAt = newUser.createdAt
        )
        
        val json = Json.encodeToString(userWithoutPassword)
        prefs.edit().putString(CURRENT_USER_KEY, json).apply()
        
        return userWithoutPassword
    }
    
    // Logout
    fun logout() {
        prefs.edit().remove(CURRENT_USER_KEY).apply()
    }
    
    // Get current user
    fun getCurrentUser(): JsonBinClient.User? {
        return try {
            val stored = prefs.getString(CURRENT_USER_KEY, null)
            if (stored != null) {
                Json.decodeFromString<JsonBinClient.User>(stored)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error getting current user: ${e.message}")
            null
        }
    }
}

