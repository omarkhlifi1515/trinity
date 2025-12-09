package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.GoogleLoginRequest
import com.example.smarthr_app.data.model.GoogleSignUpRequest
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.data.model.UpdateProfileRequest
import com.example.smarthr_app.data.model.UploadImageResponse
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody

class AuthRepository(private val dataStoreManager: DataStoreManager) {

    private val supabaseAuthRepository = SupabaseAuthRepository(dataStoreManager)

    val isLoggedIn: Flow<Boolean> = dataStoreManager.authToken.map { it != null }
    val user: Flow<UserDto?> = dataStoreManager.user

    private suspend fun isOfflineMode(): Boolean {
        return dataStoreManager.getConnectionMode() == "offline"
    }

    suspend fun login(loginRequest: LoginRequest): Resource<AuthResponse> {
        // Check if offline mode is enabled
        if (isOfflineMode()) {
            return supabaseAuthRepository.login(loginRequest.email, loginRequest.password)
        }
        
        // Online mode - use Laravel API
        return try {
            val response = RetrofitInstance.api.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Save token and user to DataStore
                dataStoreManager.saveToken(authResponse.token)
                dataStoreManager.saveUser(authResponse.user)
                Resource.Success(authResponse)
            } else {
                // Parse error body - handle both JSON and HTML responses
                val errorBody = response.errorBody()?.string() ?: "Login failed"
                val errorMessage = try {
                    // Try to parse as JSON
                    val json = com.google.gson.JsonParser.parseString(errorBody)
                    if (json.isJsonObject) {
                        val obj = json.asJsonObject
                        obj.get("message")?.asString ?: obj.get("error")?.asString ?: errorBody
                    } else {
                        errorBody
                    }
                } catch (e: Exception) {
                    // If it's HTML (doctype error), return a clean message
                    if (errorBody.contains("<!DOCTYPE") || errorBody.contains("<html")) {
                        when (response.code()) {
                            401 -> "Invalid email or password"
                            422 -> "Validation failed. Please check your input."
                            500 -> "Server error. Please try again later."
                            else -> "Login failed. Please check your credentials."
                        }
                    } else {
                        errorBody
                    }
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> 
                    "Network error. Please check your internet connection."
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    "Connection timeout. Please try again."
                else -> e.message ?: "An error occurred during login"
            }
            Resource.Error(errorMessage)
        }
    }

    suspend fun register(request: UserRegisterRequest): Resource<UserDto> {
        // Check if offline mode is enabled
        if (isOfflineMode()) {
            return supabaseAuthRepository.register(request)
        }
        
        // Online mode - use Laravel API
        return try {
            val response = RetrofitInstance.api.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                
                // After registration, auto-login to get token
                // This ensures user is authenticated immediately after registration
                try {
                    val loginResponse = RetrofitInstance.api.login(
                        LoginRequest(
                            email = request.email,
                            password = request.password
                        )
                    )
                    if (loginResponse.isSuccessful && loginResponse.body() != null) {
                        val authResponse = loginResponse.body()!!
                        // Save token and updated user data (may have company if HR)
                        dataStoreManager.saveToken(authResponse.token)
                        dataStoreManager.saveUser(authResponse.user)
                        return Resource.Success(authResponse.user)
                    }
                } catch (e: Exception) {
                    // Auto-login failed, but user is registered
                    // Return registered user without token - user will need to login manually
                }
                
                // If auto-login fails, save user data without token
                dataStoreManager.saveUser(userDto)
                Resource.Success(userDto)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Registration failed"
                Resource.Error(errorBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during registration")
        }
    }

    suspend fun loginWithGoogle(request: GoogleLoginRequest): Resource<AuthResponse> {
        return try {
            val response = RetrofitInstance.api.loginWithGoogle(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                dataStoreManager.saveToken(authResponse.token)
                dataStoreManager.saveUser(authResponse.user)
                Resource.Success(authResponse)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Google Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun signUpWithGoogle(request: GoogleSignUpRequest): Resource<AuthResponse> {
        return try {
            val response = RetrofitInstance.api.signUpWithGoogle(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                dataStoreManager.saveToken(authResponse.token)
                dataStoreManager.saveUser(authResponse.user)
                Resource.Success(authResponse)
            } else {
                 Resource.Error(response.errorBody()?.string() ?: "Google Sign-Up failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun logout() {
        // If offline mode, logout from Supabase first
        if (isOfflineMode()) {
            supabaseAuthRepository.logout()
        } else {
            dataStoreManager.clearData()
        }
    }

    suspend fun updateCompanyCode(companyCode: String): Resource<UserDto> {
        // Check if offline mode is enabled
        if (isOfflineMode()) {
            return supabaseAuthRepository.updateCompanyCode(companyCode)
        }
        
        // Online mode - use Laravel API
        return try {
            val token = dataStoreManager.authToken.first()
            if (token == null) {
                return Resource.Error("Not authenticated")
            }
            val response = RetrofitInstance.api.updateCompanyCode("Bearer $token", companyCode)
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                dataStoreManager.saveUser(updatedUser)
                Resource.Success(updatedUser)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to update company code"
                Resource.Error(errorBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun leaveCompany(): Resource<UserDto> {
        // Check if offline mode is enabled
        if (isOfflineMode()) {
            return supabaseAuthRepository.leaveCompany()
        }
        
        // Online mode - use Laravel API
        return try {
            val token = dataStoreManager.authToken.first()
            if (token == null) {
                return Resource.Error("Not authenticated")
            }
            val response = RetrofitInstance.api.leaveCompany("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                dataStoreManager.saveUser(updatedUser)
                Resource.Success(updatedUser)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to leave company"
                Resource.Error(errorBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun removeFromWaitlist(): Resource<UserDto> {
        // Check if offline mode is enabled
        if (isOfflineMode()) {
            return supabaseAuthRepository.removeFromWaitlist()
        }
        
        // Online mode - use Laravel API
        return try {
            val token = dataStoreManager.authToken.first()
            if (token == null) {
                return Resource.Error("Not authenticated")
            }
            val response = RetrofitInstance.api.removeWaitlistCompany("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                dataStoreManager.saveUser(updatedUser)
                Resource.Success(updatedUser)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to remove from waitlist"
                Resource.Error(errorBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun refreshProfile(): Resource<UserDto> {
        // Check if offline mode is enabled
        if (isOfflineMode()) {
            return supabaseAuthRepository.refreshProfile()
        }
        
        // Online mode - use Laravel API
        return try {
            val token = dataStoreManager.authToken.first()
            if (token == null) {
                return Resource.Error("Not authenticated")
            }
            val currentUser = dataStoreManager.user.first()
            if (currentUser == null) {
                return Resource.Error("User not found")
            }
            // Get user profile to refresh data
            val response = RetrofitInstance.api.getUserProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                dataStoreManager.saveUser(updatedUser)
                Resource.Success(updatedUser)
            } else {
                // If refresh fails, return current user data as success
                Resource.Success(currentUser)
            }
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

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<UserDto> {
        // Check if offline mode is enabled
        if (isOfflineMode()) {
            return supabaseAuthRepository.updateProfile(
                name = request.name,
                phone = request.phone,
                gender = request.gender
            )
        }
        
        // Online mode - use Laravel API
        return try {
            val token = dataStoreManager.authToken.first()
            if (token == null) {
                return Resource.Error("Not authenticated")
            }
            val response = RetrofitInstance.api.updateProfile("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                dataStoreManager.saveUser(updatedUser)
                Resource.Success(updatedUser)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to update profile"
                Resource.Error(errorBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun uploadProfileImage(imagePart: MultipartBody.Part): Resource<UploadImageResponse> {
        return try {
            val token = dataStoreManager.authToken.first()
            if (token == null) {
                return Resource.Error("Not authenticated")
            }
            val response = RetrofitInstance.api.uploadProfileImage("Bearer $token", imagePart)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to upload image"
                Resource.Error(errorBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
    
    suspend fun setConnectionMode(mode: String) {
        dataStoreManager.setConnectionMode(mode)
    }
}
