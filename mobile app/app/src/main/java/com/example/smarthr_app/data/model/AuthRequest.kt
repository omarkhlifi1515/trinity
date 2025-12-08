package com.example.smarthr_app.data.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class UserRegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val gender: String = "M",
    val role: String,
    val companyCode: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class GoogleSignUpRequest(
    val idToken :String,
    val role:String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

@JsonAdapter(UserDtoTypeAdapter::class)
data class UserDto(
    @SerializedName("id")
    val userId: String, // Custom adapter converts Int to String
    val name: String,
    val email: String,
    @SerializedName("phone")
    val phone: String = "",
    @SerializedName("gender")
    val gender: String? = null,
    val role: String,
    @SerializedName("company_code")
    val companyCode: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val position: String? = null,
    val department: String? = null,
    @SerializedName("waiting_company_code")
    val waitingCompanyCode: String? = null,
    @SerializedName("joining_status")
    val joiningStatus: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String?,
    val gender: String?,
    val position: String?,
    val department: String?
)

data class CompanyWaitlistResponse(
    val companyCode: String,
    val users: List<UserDto>
)

data class CompanyEmployeesResponse(
    val companyCode: String,
    val users: List<UserDto>
)

data class SuccessApiResponseMessage(
    val message: String
)

data class UploadImageResponse(
    val message: String
)

// Enums for dropdowns
enum class Position {
    INTERN, JUNIOR_DEVELOPER, SENIOR_DEVELOPER, TEAM_LEAD, MANAGER, HR, CTO, CEO, OTHERS
}

enum class Department {
    HR, ENGINEERING, SALES, MARKETING, FINANCE, OPERATIONS, ADMINISTRATION, SUPPORT, OTHERS
}

enum class Gender {
    M, F
}

enum class UserRole {
    ROLE_HR, ROLE_USER
}
