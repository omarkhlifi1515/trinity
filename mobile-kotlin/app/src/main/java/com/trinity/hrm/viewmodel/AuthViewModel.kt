package com.trinity.hrm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trinity.hrm.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val userEmail: String? = null,
    val userId: String? = null
)

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val user = ApiClient.getCurrentUser()
                _authState.value = _authState.value.copy(
                    isAuthenticated = user != null,
                    userEmail = user?.email,
                    userId = user?.id
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isAuthenticated = false,
                    error = e.message
                )
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.login(email, password)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = response.user.email,
                    userId = response.user.id
                )
                onSuccess()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to sign in"
                )
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val response = ApiClient.signup(email, password)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    userEmail = response.user.email,
                    userId = response.user.id
                )
                onSuccess()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to sign up"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                ApiClient.logout()
                _authState.value = AuthState()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = e.message)
            }
        }
    }
}

