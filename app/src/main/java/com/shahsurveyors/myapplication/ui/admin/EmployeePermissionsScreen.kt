package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.UserProfile

private val permissionModules = listOf(
    "ATTENDANCE" to "Attendance",
    "CHAT" to "Team Chat",
    "PROJECTS" to "Projects",
    "EXPENSE" to "Expenses",
    "DSR" to "Daily Status Report",
    "EQUIPMENT" to "Equipment",
    "TASKS" to "Tasks",
    "SURVEY" to "Survey Calculator",
    "BILLING" to "Quotation / Billing",
    "CLIENTS" to "Clients",
    "MARKETING" to "Marketing"
)

fun hasModuleAccess(access: String, module: String): Boolean {
    val values = access.split(",", ";", "|").map { it.trim().uppercase() }.filter { it.isNotBlank() }
    return values.contains("ALL") || values.contains(module.uppercase())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeePermissionsScreen(onBack: () -> Unit = {}) {
    val db = remember { FirebaseFirestore.getInstance() }
    var employees by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var selected by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        db.collection("users").get()
            .addOnSuccessListener { snap ->
                employees = snap.toObjects(UserProfile::class.java)
                    .filter { it.uid.isNotBlank() && !it.role.equals("admin", true) }
                    .sortedBy { it.name.lowercase() }
                loading = false
            }
            .addOnFailureListener { error = it.localizedMessage ?: "Unable to load employees"; loading = false }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Permissions") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Module access control", style = MaterialTheme.typography.titleMedium)
                    Text("Admin can enable or disable each module for every employee.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (employees.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No employees found") }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(employees, key = { it.uid }) { employee ->
                        Card(
                            onClick = { selected = employee },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(employee.name.ifBlank { "Unnamed employee" }, style = MaterialTheme.typography.titleMedium)
                                Text(employee.department, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    if (employee.access.isBlank()) "No module access" else "Access: ${employee.access}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("EDIT PERMISSIONS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { employee ->
        PermissionEditorDialog(
            employee = employee,
            onDismiss = { selected = null },
            onSaved = {
                selected = null
                load()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionEditorDialog(employee: UserProfile, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val initial = remember(employee.uid) { employee.access.split(",").map { it.trim().uppercase() }.filter { it.isNotBlank() }.toSet() }
    var selected by remember(employee.uid) { mutableStateOf(initial) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Permissions — ${employee.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Choose exactly which modules this employee can open.", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                permissionModules.forEach { (key, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = selected.contains(key) || selected.contains("ALL"),
                            onCheckedChange = { checked ->
                                selected = if (checked) selected + key else selected - key
                            }
                        )
                        Text(label)
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(enabled = !saving, onClick = {
                saving = true
                error = null
                val access = selected.filter { it != "ALL" }.joinToString(",")
                db.collection("users").document(employee.uid)
                    .update(mapOf("access" to access, "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
                    .addOnSuccessListener { onSaved() }
                    .addOnFailureListener { error = it.localizedMessage ?: "Unable to save permissions"; saving = false }
            }) { Text(if (saving) "Saving..." else "Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } }
    )
}
