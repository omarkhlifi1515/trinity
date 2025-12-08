package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<Any>?>(null)
    val registerState: StateFlow<Resource<Any>?> = _registerState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = repository.login(request)
        }
    }

    fun register(request: UserRegisterRequest) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = repository.register(request)
            // Map UserDto result to Any for simpler state handling if needed, or keep generic
            if (result is Resource.Success) {
                _registerState.value = Resource.Success(result.data!!)
            } else {
                _registerState.value = Resource.Error(result.message ?: "Error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
