package com.trinity.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trinity.mobile.data.model.ChatRequest
import com.trinity.mobile.data.model.Message
import com.trinity.mobile.network.TrinityApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel holds the list of chat messages and exposes a sendMessage API.
 * It performs optimistic UI updates: the user's message is appended immediately,
 * then the API is called and the assistant reply is appended when available.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val api: TrinityApi,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var localIdCounter = 0

    /**
     * Sends a message text to the backend. Adds a user message optimistically,
     * calls the API, and then appends the assistant message.
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = Message(
            id = "local-${localIdCounter++}",
            userId = null,
            role = "user",
            message = text,
        )

        // Optimistic update
        _messages.value = _messages.value + userMsg

        // Launch network call
        viewModelScope.launch {
            try {
                val resp = api.chat(ChatRequest(userId = null, message = text))
                val aiMsg = Message(
                    id = null,
                    userId = null,
                    role = "assistant",
                    message = resp.reply,
                )

                _messages.value = _messages.value + aiMsg
            } catch (e: Exception) {
                // On failure, append an error message with assistant role to inform the user.
                val errMsg = Message(
                    id = null,
                    userId = null,
                    role = "assistant",
                    message = "Error: ${e.message ?: "Request failed"}",
                )
                _messages.value = _messages.value + errMsg
            }
        }
    }
}
