package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import com.shahsurveyors.myapplication.models.UserProfile
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(viewModel: AdminViewModel, onBack: () -> Unit = {}, onPermissions: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var salaryEmployee by remember { mutableStateOf<UserProfile?>(null) }
    var menuEmployee by remember { mutableStateOf<UserProfile?>(null) }
    var detailsEmployee by remember { mutableStateOf<UserProfile?>(null) }
    var showAddEmployee by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchAdminData() }
    val filteredEmployees = remember(searchQuery, viewModel.allEmployees) {
        if (searchQuery.isBlank()) viewModel.allEmployees else viewModel.allEmployees.filter { e ->
            e.name.contains(searchQuery, true) || e.uid.contains(searchQuery, true) || e.department.contains(searchQuery, true) || e.role.contains(searchQuery, true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Employee Management", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Team directory & staff details", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } },
                actions = { IconButton(onClick = onPermissions) { Icon(Icons.Default.Security, "Employee permissions", tint = ShahWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { showAddEmployee = true }, containerColor = ShahGreen, contentColor = ShahWhite, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, "Add Employee") } },
        containerColor = ShahGrey
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(ShahGrey)) {
            Surface(Modifier.fillMaxWidth().padding(16.dp), RoundedCornerShape(18.dp), color = ShahDarkGreen) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, tint = ShahWhite)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Team overview", color = ShahWhite, fontWeight = FontWeight.Bold); Text("${filteredEmployees.size} employees", fontSize = 10.sp, color = ShahWhite.copy(.72f)) }
                    TextButton(onClick = onPermissions) { Text("PERMISSIONS", color = ShahWhite) }
                }
            }
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("Search name, ID, department or role...", fontSize = 12.sp) }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShahGreen, unfocusedBorderColor = ShahMediumGrey, focusedContainerColor = ShahWhite, unfocusedContainerColor = ShahWhite))
            if (filteredEmployees.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No employees found") }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredEmployees, key = { it.uid }) { employee ->
                        EmployeeCard(employee, onSalaryClick = { salaryEmployee = employee }, onMenuClick = { menuEmployee = employee })
                    }
                }
            }
        }
    }

    salaryEmployee?.let { employee -> SalarySettingsDialog(employee) { salaryEmployee = null } }
    menuEmployee?.let { employee ->
        EmployeeOptionsMenuDialog(employee, onDismiss = { menuEmployee = null }, onDetails = { detailsEmployee = employee; menuEmployee = null }, onPermissions = { onPermissions(); menuEmployee = null }, onToggleActive = { viewModel.setEmployeeActive(employee.uid, !employee.active); menuEmployee = null })
    }
    detailsEmployee?.let { employee -> EmployeeDetailsDialog(employee) { detailsEmployee = null } }
    if (showAddEmployee) AddEmployeeDialog(onDismiss = { showAddEmployee = false }, onCreated = { showAddEmployee = false; viewModel.fetchAdminData() })
}

@Composable
private fun EmployeeCard(employee: UserProfile, onSalaryClick: () -> Unit, onMenuClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(50.dp).clip(CircleShape), color = ShahGreen.copy(.10f)) { Box(contentAlignment = Alignment.Center) { Text(employee.name.trim().firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = ShahGreen, fontSize = 20.sp) } }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) { Text(employee.name, fontWeight = FontWeight.Bold); Text("ID: ${employee.uid.take(8)} • ${employee.role}", fontSize = 10.sp, color = ShahMediumGrey); Spacer(Modifier.height(4.dp)); Text(if (employee.active) employee.department else "INACTIVE • ${employee.department}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (employee.active) ShahGreen else ShahMediumGrey) }
            IconButton(onClick = onSalaryClick) { Icon(Icons.Default.Payments, "Salary settings", tint = ShahGreen) }
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.MoreVert, "Employee options", tint = ShahMediumGrey) }
        }
    }
}

@Composable
private fun EmployeeOptionsMenuDialog(employee: UserProfile, onDismiss: () -> Unit, onDetails: () -> Unit, onPermissions: () -> Unit, onToggleActive: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Employee Options", fontWeight = FontWeight.Bold) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onDetails, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Info, null); Spacer(Modifier.width(8.dp)); Text("View employee details") }
            TextButton(onClick = onPermissions, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Security, null); Spacer(Modifier.width(8.dp)); Text("Manage module permissions") }
            TextButton(onClick = onToggleActive, modifier = Modifier.fillMaxWidth()) { Icon(if (employee.active) Icons.Default.PersonOff else Icons.Default.PersonAdd, null); Spacer(Modifier.width(8.dp)); Text(if (employee.active) "Deactivate employee" else "Activate employee") }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
private fun EmployeeDetailsDialog(employee: UserProfile, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(employee.name, fontWeight = FontWeight.Bold) }, text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { DetailRow("Email", employee.email); DetailRow("User ID", employee.uid); DetailRow("Role", employee.role); DetailRow("Department", employee.department); DetailRow("Access", employee.access.ifBlank { "No module access" }); DetailRow("Approved", if (employee.approved) "Yes" else "No"); DetailRow("Active", if (employee.active) "Yes" else "No") } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } })
}

@Composable
private fun DetailRow(label: String, value: String) { Column { Text(label, fontSize = 10.sp, color = ShahMediumGrey, fontWeight = FontWeight.Bold); Text(value.ifBlank { "—" }, fontSize = 13.sp) } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEmployeeDialog(onDismiss: () -> Unit, onCreated: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("SURVEY") }
    var access by remember { mutableStateOf("ATTENDANCE,CHAT") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("Add Employee") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(email, { email = it.trim() }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(password, { password = it }, label = { Text("Temporary password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(department, { department = it.uppercase() }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(access, { access = it.uppercase() }, label = { Text("Initial module access") }, supportingText = { Text("Example: ATTENDANCE,CHAT,EXPENSE") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                enabled = !loading && name.isNotBlank() && email.contains("@") && password.length >= 6 && department.isNotBlank(),
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            repository.createEmployeeAccountAsAdmin(name, email, password, department, access)
                            onCreated()
                        } catch (e: Exception) {
                            error = e.localizedMessage ?: "Unable to create employee account"
                        } finally {
                            loading = false
                        }
                    }
                }
            ) { Text(if (loading) "Creating..." else "Create Employee") }
        },
        dismissButton = { TextButton(enabled = !loading, onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalarySettingsDialog(employee: UserProfile, onDismiss: () -> Unit) {
    val repository = remember { SalaryRepository() }
    val scope = rememberCoroutineScope()
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) }
    var payType by remember { mutableStateOf("MONTHLY") }
    var monthlySalary by remember { mutableStateOf("") }
    var dailyRate by remember { mutableStateOf("") }
    var overtimeRate by remember { mutableStateOf("") }
    var effectiveFrom by remember { mutableStateOf(today) }
    var note by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<SalaryProfileModel>>(emptyList()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(employee.uid) {
        try {
            history = repository.getHistory(employee.uid)
            history.firstOrNull()?.let { c ->
                payType = c.payType
                monthlySalary = if (c.monthlySalary > 0) c.monthlySalary.toString() else ""
                dailyRate = if (c.dailyRate > 0) c.dailyRate.toString() else ""
                overtimeRate = if (c.overtimeRatePerHour > 0) c.overtimeRatePerHour.toString() else ""
            }
        } catch (e: Exception) { error = e.localizedMessage }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Salary Settings — ${employee.name}") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { payType = "MONTHLY" }, modifier = Modifier.weight(1f)) { Text("Monthly") }
                    Button(onClick = { payType = "DAILY" }, modifier = Modifier.weight(1f)) { Text("Daily") }
                }
                if (payType == "MONTHLY") OutlinedTextField(monthlySalary, { monthlySalary = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Monthly salary ₹") }, modifier = Modifier.fillMaxWidth())
                else OutlinedTextField(dailyRate, { dailyRate = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Daily rate ₹") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(overtimeRate, { overtimeRate = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Overtime/hour ₹") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(effectiveFrom, { effectiveFrom = it }, label = { Text("Effective from yyyy-MM-dd") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(note, { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
                if (history.isNotEmpty()) {
                    Text("Salary history", fontWeight = FontWeight.Bold)
                    history.forEach { Text(if (it.payType == "DAILY") "Daily ₹${it.dailyRate}" else "Monthly ₹${it.monthlySalary}") }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving && effectiveFrom.isNotBlank() && (if (payType == "MONTHLY") (monthlySalary.toDoubleOrNull() ?: 0.0) > 0 else (dailyRate.toDoubleOrNull() ?: 0.0) > 0),
                onClick = {
                    scope.launch {
                        saving = true
                        try {
                            repository.saveSalaryProfile(
                                SalaryProfileModel(
                                    employeeUid = employee.uid,
                                    employeeName = employee.name,
                                    payType = payType,
                                    monthlySalary = if (payType == "MONTHLY") monthlySalary.toDoubleOrNull() ?: 0.0 else 0.0,
                                    dailyRate = if (payType == "DAILY") dailyRate.toDoubleOrNull() ?: 0.0 else 0.0,
                                    overtimeRatePerHour = overtimeRate.toDoubleOrNull() ?: 0.0,
                                    effectiveFrom = effectiveFrom.trim(),
                                    note = note.trim()
                                )
                            )
                            onDismiss()
                        } catch (e: Exception) { error = e.localizedMessage }
                        finally { saving = false }
                    }
                }
            ) { Text(if (saving) "Saving..." else "Save Salary") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
