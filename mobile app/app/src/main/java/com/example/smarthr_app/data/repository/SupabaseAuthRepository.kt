package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.remote.SupabaseInstance
import com.example.smarthr_app.utils.Resource
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String?,
    val name: String?,
    val phone: String? = null,
    val gender: String? = null,
    val role: String? = "Employee",
    @SerialName("company_code")
    val company_code: String? = null,
    @SerialName("waiting_company_code")
    val waiting_company_code: String? = null,
    @SerialName("image_url")
    val image_url: String? = null
)

class SupabaseAuthRepository(private val dataStoreManager: DataStoreManager) {
    
    private val supabase = SupabaseInstance.getClient()
    
    val isLoggedIn: Flow<Boolean> = dataStoreManager.authToken.map { it != null }
    val user: Flow<UserDto?> = dataStoreManager.user
    
    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return try {
            // Sign in with Supabase Auth
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Get current session
            val session = supabase.auth.currentSessionOrNull()
                ?: return Resource.Error("Login failed: No session")
            
            // Access Session properties using reflection
            val userId = getUserIdFromSession(session) 
                ?: return Resource.Error("Login failed: Could not get user ID")
            
            // Try to get user data from Supabase users table
            var supabaseUser: SupabaseUser
            var shouldUpdateCompany = false
            
            try {
                supabaseUser = supabase.postgrest.from("users")
                    .select(Columns.ALL) {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingle<SupabaseUser>()
                
                // Special case: Auto-assign salah@gmail.com to company "123456" if not already assigned
                if (email == "salah@gmail.com" && supabaseUser.company_code.isNullOrBlank()) {
                    shouldUpdateCompany = true
                    supabaseUser = supabaseUser.copy(company_code = "123456", waiting_company_code = null)
                }
            } catch (e: Exception) {
                // User doesn't exist in users table yet - create it
                val defaultCompanyCode = if (email == "salah@gmail.com") "123456" else null
                supabaseUser = SupabaseUser(
                    id = userId,
                    email = email,
                    name = "",
                    phone = null,
                    gender = null,
                    role = "Employee",
                    company_code = defaultCompanyCode,
                    waiting_company_code = null,
                    image_url = null
                )
                shouldUpdateCompany = true
                
                // Try to insert user record
                try {
                    supabase.postgrest.from("users").insert(supabaseUser)
                } catch (insertError: Exception) {
                    // User might already exist, try to fetch again
                    try {
                        supabaseUser = supabase.postgrest.from("users")
                            .select(Columns.ALL) {
                                filter {
                                    eq("id", userId)
                                }
                            }
                            .decodeSingle<SupabaseUser>()
                        
                        // Special case: Auto-assign salah@gmail.com to company "123456" if not already assigned
                        if (email == "salah@gmail.com" && supabaseUser.company_code.isNullOrBlank()) {
                            shouldUpdateCompany = true
                            supabaseUser = supabaseUser.copy(company_code = "123456", waiting_company_code = null)
                        }
                    } catch (fetchError: Exception) {
                        // Continue with created user data
                    }
                }
            }
            
            // Update company code if needed (for salah@gmail.com)
            if (shouldUpdateCompany && email == "salah@gmail.com") {
                try {
                    supabase.postgrest.from("users")
                        .update(mapOf(
                            "company_code" to "123456",
                            "waiting_company_code" to null
                        )) {
                            filter {
                                eq("id", userId)
                            }
                        }
                    // Refresh user data after update
                    supabaseUser = supabaseUser.copy(company_code = "123456", waiting_company_code = null)
                } catch (e: Exception) {
                    // Ignore update errors, continue with current data
                }
            }
            
            // Convert to UserDto
            val userDto = UserDto(
                userId = supabaseUser.id,
                name = supabaseUser.name ?: "",
                email = supabaseUser.email ?: email,
                phone = supabaseUser.phone ?: "",
                gender = supabaseUser.gender,
                role = supabaseUser.role ?: "Employee",
                companyCode = supabaseUser.company_code,
                waitingCompanyCode = supabaseUser.waiting_company_code,
                imageUrl = supabaseUser.image_url
            )
            
            // Get accessToken from session
            val accessToken = getAccessTokenFromSession(session)
                ?: return Resource.Error("Login failed: No access token")
            
            dataStoreManager.saveToken(accessToken)
            dataStoreManager.saveUser(userDto)
            
            Resource.Success(
                AuthResponse(
                    token = accessToken,
                    user = userDto
                )
            )
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true -> 
                    "Invalid email or password"
                e.message?.contains("Email not confirmed", ignoreCase = true) == true -> 
                    "Please confirm your email address"
                e.message?.contains("User not found", ignoreCase = true) == true -> 
                    "User not found. Please register first"
                else -> e.message ?: "Login failed: ${e.javaClass.simpleName}"
            }
            Resource.Error(errorMessage)
        }
    }
    
    suspend fun register(request: UserRegisterRequest): Resource<UserDto> {
        return try {
            // Sign up with Supabase
            supabase.auth.signUpWith(Email) {
                this.email = request.email
                this.password = request.password
            }
            
            // Get current session after sign up
            val session = supabase.auth.currentSessionOrNull()
                ?: return Resource.Error("Registration failed: No session")
            
            // Get user ID using reflection
            val userId = getUserIdFromSession(session)
                ?: return Resource.Error("Registration failed: Could not get user ID")
            
            // Auto-assign company if user is HR/admin or specific email
            var companyCode: String? = null
            var userRole = request.role ?: "Employee"
            
            // Special case: salah@gmail.com gets company "123456" by default
            if (request.email == "salah@gmail.com") {
                companyCode = "123456"
            }
            
            // Create user record in Supabase users table
            val supabaseUser = SupabaseUser(
                id = userId,
                email = request.email,
                name = request.name,
                phone = request.phone,
                gender = request.gender,
                role = userRole,
                company_code = companyCode,
                waiting_company_code = null,
                image_url = null
            )
            
            supabase.postgrest.from("users").insert(supabaseUser)
            
            // Convert to UserDto
            val userDto = UserDto(
                userId = userId,
                name = request.name,
                email = request.email,
                phone = request.phone ?: "",
                gender = request.gender,
                role = userRole,
                companyCode = companyCode,
                waitingCompanyCode = null,
                imageUrl = null
            )
            
            // Get accessToken from session
            val accessToken = getAccessTokenFromSession(session)
                ?: return Resource.Error("Registration failed: No access token")
            
            dataStoreManager.saveToken(accessToken)
            dataStoreManager.saveUser(userDto)
            
            Resource.Success(userDto)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }
    
    // Helper function to extract user ID from session using reflection
    private fun getUserIdFromSession(session: Any): String? {
        return try {
            // Try to get user object from session
            val userObj = try {
                session.javaClass.getDeclaredMethod("getUser").invoke(session)
            } catch (e: Exception) {
                try {
                    val userField = session.javaClass.getDeclaredField("user")
                    userField.isAccessible = true
                    userField.get(session)
                } catch (e2: Exception) {
                    null
                }
            }
            
            // Try to get ID from user object
            userObj?.let { user ->
                try {
                    user.javaClass.getDeclaredMethod("getId").invoke(user) as? String
                } catch (e: Exception) {
                    try {
                        val idField = user.javaClass.getDeclaredField("id")
                        idField.isAccessible = true
                        idField.get(user) as? String
                    } catch (e2: Exception) {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Helper function to extract access token from session using reflection
    private fun getAccessTokenFromSession(session: Any): String? {
        return try {
            try {
                session.javaClass.getDeclaredMethod("getAccessToken").invoke(session) as? String
            } catch (e: Exception) {
                try {
                    val tokenField = session.javaClass.getDeclaredField("accessToken")
                    tokenField.isAccessible = true
                    tokenField.get(session) as? String
                } catch (e2: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun refreshProfile(): Resource<UserDto> {
        return try {
            val currentUser = dataStoreManager.user.first()
            if (currentUser == null) {
                return Resource.Error("User not found")
            }
            
            // Get updated user data from Supabase
            val supabaseUser = supabase.postgrest.from("users")
                .select(Columns.ALL) {
                    filter {
                        eq("id", currentUser.userId)
                    }
                }
                .decodeSingle<SupabaseUser>()
            
            // Convert to UserDto
            val userDto = UserDto(
                userId = supabaseUser.id,
                name = supabaseUser.name ?: currentUser.name,
                email = supabaseUser.email ?: currentUser.email,
                phone = supabaseUser.phone ?: currentUser.phone,
                gender = supabaseUser.gender ?: currentUser.gender,
                role = supabaseUser.role ?: currentUser.role,
                companyCode = supabaseUser.company_code,
                waitingCompanyCode = supabaseUser.waiting_company_code,
                imageUrl = supabaseUser.image_url
            )
            
            // Update DataStore
            dataStoreManager.saveUser(userDto)
            
            Resource.Success(userDto)
        } catch (e: Exception) {
            // If refresh fails, return current user data
            val currentUser = dataStoreManager.user.first()
            if (currentUser != null) {
                Resource.Success(currentUser)
            } else {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            // Ignore errors on logout
        }
        dataStoreManager.clearData()
    }
    
    suspend fun updateCompanyCode(companyCode: String): Resource<UserDto> {
        return try {
            val currentUser = dataStoreManager.user.first()
            if (currentUser == null) {
                return Resource.Error("Not authenticated")
            }
            
            // Update user's waiting_company_code in Supabase
            supabase.postgrest.from("users")
                .update(mapOf("waiting_company_code" to companyCode.uppercase())) {
                    filter {
                        eq("id", currentUser.userId)
                    }
                }
            
            // Refresh profile to get updated data
            refreshProfile()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
    
    suspend fun leaveCompany(): Resource<UserDto> {
        return try {
            val currentUser = dataStoreManager.user.first()
            if (currentUser == null) {
                return Resource.Error("Not authenticated")
            }
            
            // Remove company_code from user in Supabase
            supabase.postgrest.from("users")
                .update(mapOf("company_code" to null)) {
                    filter {
                        eq("id", currentUser.userId)
                    }
                }
            
            // Refresh profile to get updated data
            refreshProfile()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
    
    suspend fun removeFromWaitlist(): Resource<UserDto> {
        return try {
            val currentUser = dataStoreManager.user.first()
            if (currentUser == null) {
                return Resource.Error("Not authenticated")
            }
            
            // Remove waiting_company_code from user in Supabase
            supabase.postgrest.from("users")
                .update(mapOf("waiting_company_code" to null)) {
                    filter {
                        eq("id", currentUser.userId)
                    }
                }
            
            // Refresh profile to get updated data
            refreshProfile()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
    
    suspend fun updateProfile(name: String?, phone: String?, gender: String?): Resource<UserDto> {
        return try {
            val currentUser = dataStoreManager.user.first()
            if (currentUser == null) {
                return Resource.Error("Not authenticated")
            }
            
            val updates = mutableMapOf<String, Any?>()
            name?.let { updates["name"] = it }
            phone?.let { updates["phone"] = it }
            gender?.let { updates["gender"] = it }
            
            if (updates.isNotEmpty()) {
                supabase.postgrest.from("users")
                    .update(updates) {
                        filter {
                            eq("id", currentUser.userId)
                        }
                    }
            }
            
            // Refresh profile to get updated data
            refreshProfile()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}
