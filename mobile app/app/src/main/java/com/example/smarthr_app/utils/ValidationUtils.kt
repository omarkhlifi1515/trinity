package com.example.smarthr_app.utils

import android.util.Patterns

object ValidationUtils {

    fun validateName(name: String): ValidationResult {
        val nameWithoutSpaces = name.replace(" ", "")
        return when {
            name.isBlank() -> ValidationResult(false, "Name is required")
            nameWithoutSpaces.length < 3 -> ValidationResult(false, "Name must be at least 3 characters (excluding spaces)")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationResult(false, "Name should only contain alphabets and spaces")
            else -> ValidationResult(true, "")
        }
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult(false, "Please enter a valid email address")
            // Removed the strict ".com" check in case you use other domains like .net or .org
            // !email.endsWith(".com", ignoreCase = true) -> ValidationResult(false, "Email must end with .com")
            else -> ValidationResult(true, "")
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        // Simple check to allow flexible formats or keep it strict if you prefer
        // This version keeps your original logic but is slightly safer
        val cleanPhone = phone.replace("+91", "").trim()
        return when {
            phone.isBlank() -> ValidationResult(false, "Phone number is required")
            cleanPhone.length != 10 -> ValidationResult(false, "Phone number must be exactly 10 digits")
            !cleanPhone.matches(Regex("^[0-9]+$")) -> ValidationResult(false, "Phone number should only contain digits")
            else -> ValidationResult(true, "")
        }
    }

    // FIXED: Simplified to match Laravel's standard validation
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            // Changed from strict Regex to simple length check
            // Laravel default is often 8, but we'll keep 6 to be safe
            password.length < 6 -> ValidationResult(false, "Password must be at least 6 characters")
            else -> ValidationResult(true, "")
        }
    }

    fun validateCompanyCode(companyCode: String, isRequired: Boolean = false): ValidationResult {
        return when {
            isRequired && companyCode.isBlank() -> ValidationResult(false, "Company code is required")
            companyCode.isNotBlank() && companyCode.length < 3 -> ValidationResult(false, "Company code must be at least 3 characters")
            else -> ValidationResult(true, "")
        }
    }

    fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace("+91", "").replace(" ", "").trim()
        return if (cleanPhone.length == 10 && cleanPhone.matches(Regex("^[0-9]+$"))) {
            "+91$cleanPhone"
        } else {
            phone
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)
