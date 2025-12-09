package com.trinity.hrm.ui.leaves

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.trinity.hrm.data.model.Leave
import com.trinity.hrm.data.model.LeaveType
import java.time.Instant

@Composable
fun AddLeaveDialog(
    currentUserId: String,
    onDismiss: () -> Unit,
    onAdd: (Leave) -> Unit
) {
    var selectedType by remember { mutableStateOf(LeaveType.VACATION) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
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
                    text = "Request Leave",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // Leave type selector
                Text(
                    text = "Leave Type",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeaveType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
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
                            if (startDate.isNotBlank() && endDate.isNotBlank()) {
                                val leave = Leave(
                                    id = System.currentTimeMillis().toString(),
                                    employeeId = currentUserId,
                                    type = selectedType,
                                    startDate = startDate,
                                    endDate = endDate,
                                    reason = if (reason.isNotBlank()) reason else null,
                                    createdAt = Instant.now().toString()
                                )
                                onAdd(leave)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = startDate.isNotBlank() && endDate.isNotBlank()
                    ) {
                        Text("Submit Request")
                    }
                }
            }
        }
    }
}

