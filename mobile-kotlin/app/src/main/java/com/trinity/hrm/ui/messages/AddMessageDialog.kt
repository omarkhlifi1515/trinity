package com.trinity.hrm.ui.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trinity.hrm.data.model.Message
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMessageDialog(
    currentUserId: String,
    employees: List<com.trinity.hrm.data.model.Employee>,
    onDismiss: () -> Unit,
    onAdd: (Message) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedRecipient by remember { mutableStateOf("all") // "all" for broadcast or employee ID
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New Message",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // Recipient selector
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = when (selectedRecipient) {
                            "all" -> "All Employees"
                            else -> employees.find { it.id == selectedRecipient }?.name ?: "Select Recipient"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Employees") },
                            onClick = {
                                selectedRecipient = "all"
                                expanded = false
                            }
                        )
                        employees.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text(employee.name) },
                                onClick = {
                                    selectedRecipient = employee.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (subject.isNotBlank() && content.isNotBlank()) {
                                val message = Message(
                                    id = System.currentTimeMillis().toString(),
                                    from = currentUserId,
                                    to = selectedRecipient,
                                    subject = subject,
                                    content = content,
                                    createdAt = Instant.now().toString()
                                )
                                onAdd(message)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = subject.isNotBlank() && content.isNotBlank()
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}

