package com.trinity.hrm.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Local Auth API Client - No Supabase needed!
 * 
 * IMPORTANT: Update BASE_URL to match your web app URL
 * For local development: http://10.0.2.2:3000 (Android emulator)
 * For physical device: http://YOUR_COMPUTER_IP:3000
 * For production: https://your-domain.com
 */
object ApiClient {
    // Update this URL to match your web app
    // For Android emulator: http://10.0.2.2:3000
    // For physical device: http://YOUR_COMPUTER_IP:3000
    // For production: https://your-domain.com
    private const val BASE_URL = "http://10.0.2.2:3000" // Android emulator localhost
    
    private var storedToken: String? = null
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
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
    
    @Serializable
    data class ErrorResponse(
        val error: String
    )
    
    @Serializable
    data class MeResponse(
        val user: User
    )
    
    @Serializable
    data class LoginRequest(
        val email: String,
        val password: String
    )
    
    @Serializable
    data class AuthResponseWithToken(
        val user: User,
        val token: String? = null
    )
    
    suspend fun login(email: String, password: String): AuthResponse {
        val response = client.post("$BASE_URL/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        
        if (response.status.isSuccess()) {
            val authResponse: AuthResponseWithToken = response.body<AuthResponseWithToken>()
            // Store token for future requests
            authResponse.token?.let { storedToken = it }
            return AuthResponse(authResponse.user)
        } else {
            val error: ErrorResponse = response.body<ErrorResponse>()
            throw Exception(error.error)
        }
    }
    
    suspend fun signup(email: String, password: String): AuthResponse {
        val response = client.post("$BASE_URL/api/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        
        if (response.status.isSuccess()) {
            val authResponse: AuthResponseWithToken = response.body<AuthResponseWithToken>()
            // Store token for future requests
            authResponse.token?.let { storedToken = it }
            return AuthResponse(authResponse.user)
        } else {
            val error: ErrorResponse = response.body<ErrorResponse>()
            throw Exception(error.error)
        }
    }
    
    suspend fun logout() {
        storedToken = null
        try {
            client.post("$BASE_URL/api/auth/logout")
        } catch (e: Exception) {
            // Ignore logout errors
        }
    }
    
    suspend fun getCurrentUser(): User? {
        return try {
            val response = client.get("$BASE_URL/api/auth/me") {
                storedToken?.let {
                    header("Authorization", "Bearer $it")
                }
            }
            if (response.status.isSuccess()) {
                val meResponse: MeResponse = response.body<MeResponse>()
                meResponse.user
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

