package com.trinity.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trinity.mobile.viewmodel.MainViewModel

/**
 * ChatScreen: displays a list of messages and an input bar.
 * - User messages are aligned to the right (blue/cyan)
 * - AI messages aligned to the left (dark/gray)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: MainViewModel = hiltViewModel()) {
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    // Auto-scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF030312))
        .padding(8.dp)) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(messages) { _, msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    val bubbleColor = if (isUser) Color(0xFF064E3B) else Color(0xFF111827)
                    val textColor = if (isUser) Color(0xFFBBF7D0) else Color(0xFFE5E7EB)

                    Box(modifier = Modifier
                        .widthIn(max = 280.dp)
                        .background(bubbleColor)
                        .padding(12.dp)) {
                        Text(
                            text = msg.message,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        // Input area
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Ask Trinity…", color = Color(0xFF94A3B8)) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                if (input.isNotBlank()) {
                    viewModel.sendMessage(input.trim())
                    input = ""
                }
            }) {
                Text(text = "Send", textAlign = TextAlign.Center)
            }
        }
    }
}
