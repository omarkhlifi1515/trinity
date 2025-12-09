package com.example.smarthr_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smarthr_app.data.model.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_USER_ID = stringPreferencesKey("user_id") // Changed to String to match UserDto.userId
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        val KEY_USER_GENDER = stringPreferencesKey("user_gender")
        val KEY_COMPANY_CODE = stringPreferencesKey("company_code")
        val KEY_USER_IMAGE = stringPreferencesKey("user_image")
        val KEY_CONNECTION_MODE = stringPreferencesKey("connection_mode") // "online" or "offline"
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    suspend fun saveUser(user: UserDto) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = user.userId.toString() // Ensure it's a String
            preferences[KEY_USER_NAME] = user.name
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_USER_ROLE] = user.role
            if (user.phone.isNotEmpty()) {
                preferences[KEY_USER_PHONE] = user.phone
            }
            user.gender?.let { preferences[KEY_USER_GENDER] = it }
            user.companyCode?.let { preferences[KEY_COMPANY_CODE] = it }
            user.imageUrl?.let { preferences[KEY_USER_IMAGE] = it }
        }
    }

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[KEY_TOKEN] }

    val user: Flow<UserDto?> = context.dataStore.data
        .map { preferences ->
            val id = preferences[KEY_USER_ID]
            val name = preferences[KEY_USER_NAME]
            val email = preferences[KEY_USER_EMAIL]
            val role = preferences[KEY_USER_ROLE] ?: "Employee"
            val phone = preferences[KEY_USER_PHONE] ?: ""
            val gender = preferences[KEY_USER_GENDER]
            val companyCode = preferences[KEY_COMPANY_CODE]
            val imageUrl = preferences[KEY_USER_IMAGE]

            if (id != null && name != null && email != null) {
                UserDto(
                    userId = id,
                    name = name,
                    email = email,
                    phone = phone,
                    gender = gender,
                    role = role,
                    companyCode = companyCode,
                    imageUrl = imageUrl
                )
            } else {
                null
            }
        }

    suspend fun clearData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getToken(): String? {
        return authToken.first()
    }

    suspend fun getUser(): UserDto? {
        return user.first()
    }
    
    suspend fun setConnectionMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CONNECTION_MODE] = mode // "online" or "offline"
        }
    }
    
    val connectionMode: Flow<String> = context.dataStore.data
        .map { preferences -> 
            preferences[KEY_CONNECTION_MODE] ?: "online" // Default to online mode
        }
    
    suspend fun getConnectionMode(): String {
        return connectionMode.first()
    }
}
