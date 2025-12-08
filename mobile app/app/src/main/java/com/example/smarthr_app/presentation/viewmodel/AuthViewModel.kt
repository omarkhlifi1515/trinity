package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.GoogleSignUpRequest
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<UserDto>?>(null)
    val registerState: StateFlow<Resource<UserDto>?> = _registerState

    private val _googleSignUpAuthState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val googleSignUpAuthState: StateFlow<Resource<AuthResponse>?> = _googleSignUpAuthState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = repository.login(request)
        }
    }

    fun registerUser(request: UserRegisterRequest) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            _registerState.value = repository.register(request)
        }
    }

    fun signUpWithGoogle(request: GoogleSignUpRequest) {
        viewModelScope.launch {
            _googleSignUpAuthState.value = Resource.Loading()
            _googleSignUpAuthState.value = repository.signUpWithGoogle(request)
        }
    }

    fun clearRegisterState() {
        _registerState.value = null
    }

    fun clearAuthState() {
        _googleSignUpAuthState.value = null
    }
}
