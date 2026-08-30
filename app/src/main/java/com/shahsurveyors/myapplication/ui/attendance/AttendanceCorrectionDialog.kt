package com.shahsurveyors.myapplication.ui.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCorrectionDialog(
    date: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (issueType: String, reason: String) -> Unit
) {
    var issueType by remember { mutableStateOf("LATE") }
    var reason by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val issueTypes = listOf("LATE", "MISSING_PUNCH_OUT", "ABSENT", "EARLY_OUT", "WRONG_PUNCH")

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Request Attendance Correction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Date: $date")
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (!isSubmitting) expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = issueType.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Issue Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        issueTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replace('_', ' ')) },
                                onClick = { issueType = type; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reason") },
                    placeholder = { Text("Explain what happened") },
                    minLines = 3,
                    enabled = !isSubmitting
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && reason.isNotBlank(),
                onClick = { onSubmit(issueType, reason.trim()) }
            ) { Text(if (isSubmitting) "Submitting..." else "Submit for Approval") }
        },
        dismissButton = {
            TextButton(enabled = !isSubmitting, onClick = onDismiss) { Text("Cancel") }
        }
    )
}
