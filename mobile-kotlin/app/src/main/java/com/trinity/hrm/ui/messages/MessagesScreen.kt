package com.trinity.hrm.ui.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trinity.hrm.data.model.Message
import com.trinity.hrm.data.repository.MessageRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen() {
    val scope = rememberCoroutineScope()
    val repository = remember { MessageRepository() }
    
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    fun loadMessages() {
        scope.launch {
            isLoading = true
            val result = repository.getMessages()
            result.onSuccess { list ->
                messages = list.sortedByDescending { it.createdAt }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                actions = {
                    IconButton(onClick = { loadMessages() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Edit, "New Message")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    MessageCard(msg)
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddMessageDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { to, subject, content ->
                scope.launch {
                    repository.sendMessage(to, subject, content)
                    loadMessages()
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun MessageCard(message: Message) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(text = message.subject, style = MaterialTheme.typography.titleMedium)
            Text(text = "From: ${message.from}", style = MaterialTheme.typography.bodySmall)
            Text(text = message.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AddMessageDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Message") },
        text = {
            Column {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") }
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.height(100.dp)
                )
                Text("To: All (Default for MVP)", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm("all", subject, content) }) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
