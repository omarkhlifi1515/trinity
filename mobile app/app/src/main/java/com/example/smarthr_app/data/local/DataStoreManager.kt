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
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_USER_ID = intPreferencesKey("user_id") // Changed to Int
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    suspend fun saveUser(user: UserDto) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = user.id
            preferences[KEY_USER_NAME] = user.name
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_USER_ROLE] = user.role
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

            if (id != null && name != null && email != null) {
                UserDto(
                    id = id,
                    name = name,
                    email = email,
                    role = role,
                    createdAt = null,
                    updatedAt = null
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
}
