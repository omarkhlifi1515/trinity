package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.GoogleLoginRequest
import com.example.smarthr_app.data.model.GoogleSignUpRequest
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.data.model.UpdateProfileRequest
import com.example.smarthr_app.data.model.UploadImageResponse
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Expose user flow from repository
    val user: Flow<UserDto?> = repository.user

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState
    val authState: StateFlow<Resource<AuthResponse>?> = _loginState // Alias for compatibility

    private val _registerState = MutableStateFlow<Resource<UserDto>?>(null)
    val registerState: StateFlow<Resource<UserDto>?> = _registerState

    private val _googleLoginAuthState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val googleLoginAuthState: StateFlow<Resource<AuthResponse>?> = _googleLoginAuthState

    private val _googleSignUpAuthState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val googleSignUpAuthState: StateFlow<Resource<AuthResponse>?> = _googleSignUpAuthState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = repository.login(request)
        }
    }

    fun loginWithGoogle(request: GoogleLoginRequest) {
        viewModelScope.launch {
            _googleLoginAuthState.value = Resource.Loading()
            _googleLoginAuthState.value = repository.loginWithGoogle(request)
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

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearRegisterState() {
        _registerState.value = null
    }

    fun clearAuthState() {
        _loginState.value = null
        _googleLoginAuthState.value = null
        _googleSignUpAuthState.value = null
    }

    // Company management states
    private val _updateCompanyState = MutableStateFlow<Resource<UserDto>?>(null)
    val updateCompanyState: StateFlow<Resource<UserDto>?> = _updateCompanyState

    private val _leaveCompanyState = MutableStateFlow<Resource<UserDto>?>(null)
    val leaveCompanyState: StateFlow<Resource<UserDto>?> = _leaveCompanyState

    fun updateCompanyCode(companyCode: String) {
        viewModelScope.launch {
            _updateCompanyState.value = Resource.Loading()
            _updateCompanyState.value = repository.updateCompanyCode(companyCode)
        }
    }

    fun leaveCompany() {
        viewModelScope.launch {
            _leaveCompanyState.value = Resource.Loading()
            _leaveCompanyState.value = repository.leaveCompany()
        }
    }

    fun removeFromWaitlist() {
        viewModelScope.launch {
            _leaveCompanyState.value = Resource.Loading()
            _leaveCompanyState.value = repository.removeFromWaitlist()
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            repository.refreshProfile()
            // Refresh is silent - just updates the user flow
        }
    }

    fun clearUpdateCompanyState() {
        _updateCompanyState.value = null
    }

    fun clearLeaveCompanyState() {
        _leaveCompanyState.value = null
    }

    // Profile management states
    private val _updateProfileState = MutableStateFlow<Resource<UserDto>?>(null)
    val updateProfileState: StateFlow<Resource<UserDto>?> = _updateProfileState

    private val _uploadImageState = MutableStateFlow<Resource<UploadImageResponse>?>(null)
    val uploadImageState: StateFlow<Resource<UploadImageResponse>?> = _uploadImageState

    fun updateProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _updateProfileState.value = Resource.Loading()
            _updateProfileState.value = repository.updateProfile(request)
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uploadImageState.value = Resource.Loading()
            try {
                // Create MultipartBody.Part from Uri
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                _uploadImageState.value = repository.uploadProfileImage(imagePart)
                
                // Clean up temp file
                file.delete()
            } catch (e: Exception) {
                _uploadImageState.value = Resource.Error(e.message ?: "Failed to process image")
            }
        }
    }

    fun clearUpdateProfileState() {
        _updateProfileState.value = null
    }

    fun clearUploadImageState() {
        _uploadImageState.value = null
    }
}
