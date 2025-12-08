package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(private val dataStoreManager: DataStoreManager) {

    val isLoggedIn: Flow<Boolean> = dataStoreManager.authToken.map { it != null }
    val user: Flow<UserDto?> = dataStoreManager.user

    suspend fun login(loginRequest: LoginRequest): Resource<AuthResponse> {
        return try {
            val response = RetrofitInstance.api.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Save token and user to DataStore
                dataStoreManager.saveToken("Bearer ${authResponse.token}")
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

    suspend fun logout() {
        dataStoreManager.clearData()
    }
}
