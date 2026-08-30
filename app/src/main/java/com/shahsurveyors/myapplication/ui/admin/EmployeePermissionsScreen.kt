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
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.launch

private val permissionModules = listOf(
    "ATTENDANCE" to "Attendance / Punch In-Out",
    "TASKS" to "Tasks",
    "CHAT" to "Team / Personal Chat",
    "LEAVE" to "Leave",
    "EXPENSE" to "Expenses",
    "SALARY" to "Salary & Payroll",
    "ADVANCE" to "Advance Salary",
    "DSR" to "Daily Status Report",
    "SURVEY" to "Survey Calculator",
    "MARKETING" to "Marketing",
    "REPORTS" to "Reports"
)

private val fixedEmployeeModules = setOf("ATTENDANCE", "TASKS", "CHAT", "LEAVE", "EXPENSE", "EXPENSES", "SALARY", "PAYROLL", "ADVANCE", "ADVANCE_SALARY", "DSR", "SURVEY")

fun hasModuleAccess(access: String, module: String): Boolean {
    val values = access.split(",", ";", "|")
        .map { it.trim().uppercase() }
        .filter { it.isNotBlank() }
        .toSet()
    if (values.contains("ALL")) return true
    val normalizedModule = module.trim().uppercase()
    if (values.isNotEmpty() && values.none { it in setOf("ADMIN", "ADMIN_HUB", "PROJECTS", "CLIENTS", "BILLING", "EQUIPMENT") }) {
        if (normalizedModule in fixedEmployeeModules) return true
    }
    return when (normalizedModule) {
        "EXPENSE", "EXPENSES" -> values.contains("EXPENSE") || values.contains("EXPENSES")
        "SALARY", "PAYROLL" -> values.contains("SALARY") || values.contains("PAYROLL")
        "ADVANCE", "ADVANCE_SALARY" -> values.contains("ADVANCE") || values.contains("ADVANCE_SALARY")
        else -> values.contains(normalizedModule)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeePermissionsScreen(onBack: () -> Unit = {}) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    var employees by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var selected by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                employees = repository.getAllEmployeesForReports()
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Unable to load employees"
            } finally {
                loading = false
            }
        }
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
                    Text("Employee access", style = MaterialTheme.typography.titleMedium)
                    Text("Employee workforce modules are fixed. Admin-only modules remain restricted.", style = MaterialTheme.typography.bodySmall)
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
                        Card(onClick = { selected = employee }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(employee.name.ifBlank { "Unnamed employee" }, style = MaterialTheme.typography.titleMedium)
                                Text(employee.department, style = MaterialTheme.typography.bodySmall)
                                Text(if (employee.active) "ACTIVE" else "INACTIVE", style = MaterialTheme.typography.labelSmall)
                                Text("Fixed employee access: Attendance • Tasks • Chat • Leave • Expense • Salary • Advance • DSR • Survey", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { employee ->
        PermissionEditorDialog(employee = employee, onDismiss = { selected = null }, onSaved = { selected = null; load() })
    }
}

@Composable
private fun PermissionEditorDialog(employee: UserProfile, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    val initial = remember(employee.uid) {
        employee.access.split(",", ";", "|")
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .toSet()
    }
    var selected by remember(employee.uid) { mutableStateOf(initial) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Employee access — ${employee.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("These workforce modules are fixed for every active employee. Admin-only modules cannot be assigned here.", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                permissionModules.forEach { (key, label) ->
                    val fixed = key in fixedEmployeeModules
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = fixed || hasModuleAccess(selected.joinToString(","), key),
                            onCheckedChange = { checked ->
                                if (!fixed) selected = if (checked) selected + key else selected - key
                            },
                            enabled = !fixed
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
                scope.launch {
                    try {
                        val access = (selected + fixedEmployeeModules).filter { it !in setOf("ALL", "ADMIN", "ADMIN_HUB", "PROJECTS", "CLIENTS", "BILLING", "EQUIPMENT") }.joinToString(",")
                        repository.updateUserAccess(employee.uid, access)
                        onSaved()
                    } catch (e: Exception) {
                        error = e.localizedMessage ?: "Unable to save permissions"
                        saving = false
                    }
                }
            }) { Text(if (saving) "Saving..." else "Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } }
    )
}