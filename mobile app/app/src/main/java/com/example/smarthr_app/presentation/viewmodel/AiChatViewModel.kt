package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.remote.N8nApiClient
import com.example.smarthr_app.data.remote.N8nChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Simple model for UI state
data class AiMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<AiMessage>>(emptyList())
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add User Message
        val userMsg = AiMessage(text, true)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 2. Call n8n
                val response = N8nApiClient.api.sendMessage(N8nChatRequest(text))

                // 3. Add Bot Response
                val botMsg = AiMessage(response.response, false)
                _messages.value = _messages.value + botMsg
            } catch (e: Exception) {
                val errorMsg = AiMessage("Error: ${e.message}", false)
                _messages.value = _messages.value + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
}
