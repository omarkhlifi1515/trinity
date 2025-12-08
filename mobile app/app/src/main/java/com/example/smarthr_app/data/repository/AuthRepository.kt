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
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody

class AuthRepository(private val dataStoreManager: DataStoreManager) {

    val isLoggedIn: Flow<Boolean> = dataStoreManager.authToken.map { it != null }
    val user: Flow<UserDto?> = dataStoreManager.user

    suspend fun login(loginRequest: LoginRequest): Resource<AuthResponse> {
        return try {
            val response = RetrofitInstance.api.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Save token and user to DataStore
                dataStoreManager.saveToken(authResponse.token) // Token in response likely doesn't have "Bearer " prefix yet, or if it does, check API. Usually raw token.
                // Assuming raw token. The Interceptor might add "Bearer ".
                dataStoreManager.saveUser(authResponse.user)
                Resource.Success(authResponse)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun register(request: UserRegisterRequest): Resource<UserDto> {
        return try {
            val response = RetrofitInstance.api.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
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
        dataStoreManager.clearData()
    }

    suspend fun updateCompanyCode(companyCode: String): Resource<UserDto> {
        return try {
            val token = dataStoreManager.getToken()
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
        return try {
            val token = dataStoreManager.getToken()
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
        return try {
            val token = dataStoreManager.getToken()
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
        return try {
            val token = dataStoreManager.getToken()
            if (token == null) {
                return Resource.Error("Not authenticated")
            }
            val currentUser = dataStoreManager.getUser()
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
            val currentUser = dataStoreManager.getUser()
            if (currentUser != null) {
                Resource.Success(currentUser)
            } else {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<UserDto> {
        return try {
            val token = dataStoreManager.getToken()
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
            val token = dataStoreManager.getToken()
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
}
