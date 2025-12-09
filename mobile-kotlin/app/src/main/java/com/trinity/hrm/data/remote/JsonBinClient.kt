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
 * JSONBin.io Client for Kotlin
 * All three apps (web, React Native, Kotlin) share the same JSONBin.io database
 * 
 * Set JSONBIN_API_KEY in your app's build.gradle.kts or use BuildConfig
 */
object JsonBinClient {
    // TODO: Set your JSONBin API key here or via BuildConfig
    // Get from: https://jsonbin.io/app/dashboard → API Keys → Master Key
    private const val API_KEY = "\$2a\$10\$XtgiWhpdzGwCmy0M915kdu9zNMfZi41jHYYGbimNLgjSSBmpFdJKq"
    private const val JSONBIN_API_URL = "https://api.jsonbin.io/v3"
    
    private var binId: String? = null
    
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
    enum class UserRole {
        ADMIN,
        DEPARTMENT_HEAD,
        EMPLOYEE
    }
    
    @Serializable
    data class User(
        val id: String,
        val email: String,
        val password: String? = null, // Hashed, only for internal use
        val createdAt: String? = null,
        val role: UserRole? = null, // User role: ADMIN, DEPARTMENT_HEAD, or EMPLOYEE
        val department: String? = null // Department name (for department heads)
    )
    
    @Serializable
    data class BinResponse(
        val metadata: BinMetadata
    )
    
    @Serializable
    data class BinMetadata(
        val id: String
    )
    
    // Read users from JSONBin
    suspend fun readUsers(): List<User> {
        return try {
            val currentBinId = binId ?: return emptyList()
            
            val response = client.get("$JSONBIN_API_URL/b/$currentBinId/latest") {
                headers {
                    append("X-Master-Key", API_KEY.trim())
                    append("X-Bin-Meta", "false")
                }
            }
            
            if (response.status.isSuccess()) {
                val users: List<User> = response.body<List<User>>()
                println("✅ Loaded ${users.size} users from JSONBin")
                users
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error reading from JSONBin: ${e.message}")
            emptyList()
        }
    }
    
    // Write users to JSONBin
    suspend fun writeUsers(users: List<User>): Boolean {
        return try {
            var currentBinId = binId
            
            // Create bin if it doesn't exist
            if (currentBinId == null) {
                val createResponse = client.post("$JSONBIN_API_URL/b") {
                    contentType(ContentType.Application.Json)
                    headers {
                        append("X-Master-Key", API_KEY.trim())
                        append("X-Bin-Name", "Trinity HRM Users")
                        append("X-Bin-Private", "true")
                    }
                    setBody(users)
                }
                
                if (createResponse.status.isSuccess()) {
                    val binResponse: BinResponse = createResponse.body()
                    currentBinId = binResponse.metadata.id
                    binId = currentBinId
                    println("✅ Created new JSONBin: $currentBinId")
                } else {
                    println("Failed to create bin: ${createResponse.status}")
                    return false
                }
            }
            
            // Update existing bin
            val updateResponse = client.put("$JSONBIN_API_URL/b/$currentBinId") {
                contentType(ContentType.Application.Json)
                headers {
                    append("X-Master-Key", API_KEY.trim())
                }
                setBody(users)
            }
            
            if (updateResponse.status.isSuccess()) {
                println("✅ Saved users to JSONBin")
                true
            } else {
                println("Failed to update bin: ${updateResponse.status}")
                false
            }
        } catch (e: Exception) {
            println("Error writing to JSONBin: ${e.message}")
            false
        }
    }
    
    // Set bin ID (for sharing with other apps)
    fun setBinId(id: String) {
        binId = id
    }
}

