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
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import com.shahsurveyors.myapplication.models.UserProfile
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(viewModel: AdminViewModel, onBack: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var salaryEmployee by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchAdminData() }
    val filteredEmployees = remember(searchQuery, viewModel.allEmployees) {
        if (searchQuery.isBlank()) viewModel.allEmployees else viewModel.allEmployees.filter { employee ->
            employee.name.contains(searchQuery, true) || employee.uid.contains(searchQuery, true) || employee.department.contains(searchQuery, true) || employee.role.contains(searchQuery, true)
        }
    }
    val departments = filteredEmployees.map { it.department }.filter { it.isNotBlank() }.distinct().size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Employee Management", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Team directory & staff details", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = ShahGreen, contentColor = ShahWhite, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, "Add Employee") }
        },
        containerColor = ShahGrey
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(ShahGrey)) {
            Surface(Modifier.fillMaxWidth().padding(16.dp), RoundedCornerShape(18.dp), color = ShahDarkGreen) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = ShahWhite.copy(.12f)) { Icon(Icons.Default.Groups, null, tint = ShahWhite, modifier = Modifier.padding(10.dp).size(24.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Team overview", color = ShahWhite, fontWeight = FontWeight.Bold); Text("${filteredEmployees.size} employees • $departments departments", fontSize = 10.sp, color = ShahWhite.copy(.72f)) }
                }
            }
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("Search name, ID, department or role...", fontSize = 12.sp) }, leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = { if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Clear") } }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShahGreen, unfocusedBorderColor = ShahMediumGrey, focusedContainerColor = ShahWhite, unfocusedContainerColor = ShahWhite))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Text("${filteredEmployees.size} Employee(s)", color = ShahDarkGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); if (searchQuery.isNotBlank()) Text("Filtered results", color = ShahGreen, fontSize = 10.sp) }
            if (filteredEmployees.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.PersonSearch, null, tint = ShahMediumGrey, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(10.dp)); Text("No employees found", fontWeight = FontWeight.Bold); Text(if (searchQuery.isBlank()) "Employee records will appear here." else "Try another name, ID, department or role.", fontSize = 11.sp, color = ShahMediumGrey) } }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredEmployees, key = { it.uid }) { employee -> EmployeeCard(employee, onSalaryClick = { salaryEmployee = employee }) }
                }
            }
        }
    }

    salaryEmployee?.let { employee ->
        SalarySettingsDialog(employee = employee, onDismiss = { salaryEmployee = null })
    }
}

@Composable
fun EmployeeCard(employee: UserProfile, onSalaryClick: () -> Unit = {}) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(50.dp).clip(CircleShape), color = ShahGreen.copy(.10f)) { Box(contentAlignment = Alignment.Center) { Text(employee.name.trim().firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = ShahGreen, fontSize = 20.sp) } }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) { Text(employee.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(3.dp)); Text("ID: ${employee.uid.take(8)} • ${employee.role}", fontSize = 10.sp, color = ShahMediumGrey); Spacer(Modifier.height(4.dp)); Surface(shape = RoundedCornerShape(7.dp), color = ShahGreen.copy(.08f)) { Text(employee.department, color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) } }
            IconButton(onClick = onSalaryClick) { Icon(Icons.Default.Payments, "Salary settings", tint = ShahGreen) }
            IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "Employee options", tint = ShahMediumGrey) }
        }
    }
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
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(employee.uid) {
        try {
            history = repository.getHistory(employee.uid)
            history.firstOrNull()?.let { current ->
                payType = current.payType
                monthlySalary = if (current.monthlySalary > 0) current.monthlySalary.toString() else ""
                dailyRate = if (current.dailyRate > 0) current.dailyRate.toString() else ""
                overtimeRate = if (current.overtimeRatePerHour > 0) current.overtimeRatePerHour.toString() else ""
            }
        } catch (e: Exception) { error = e.localizedMessage ?: "Unable to load salary" }
        finally { loading = false }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Salary Settings — ${employee.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Admin controls how this employee is paid. Salary changes are saved as new effective periods, so increments remain in history.", fontSize = 12.sp, color = ShahMediumGrey)
                Text("Payment basis", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { payType = "MONTHLY" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (payType == "MONTHLY") ShahGreen else ShahMediumGrey)) { Text("Monthly") }
                    Button(onClick = { payType = "DAILY" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (payType == "DAILY") ShahGreen else ShahMediumGrey)) { Text("Daily") }
                }
                if (payType == "MONTHLY") {
                    OutlinedTextField(value = monthlySalary, onValueChange = { monthlySalary = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Monthly salary (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                } else {
                    OutlinedTextField(value = dailyRate, onValueChange = { dailyRate = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Daily rate (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                OutlinedTextField(value = overtimeRate, onValueChange = { overtimeRate = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Overtime rate per hour (₹, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = effectiveFrom, onValueChange = { effectiveFrom = it }, label = { Text("Effective from (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Increment / salary note (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (history.isNotEmpty()) {
                    Text("Salary history", fontWeight = FontWeight.Bold)
                    history.forEach { item ->
                        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(10.dp), color = ShahGrey) {
                            Column(Modifier.padding(10.dp)) {
                                Text(if (item.payType == "DAILY") "Daily ₹${item.dailyRate}" else "Monthly ₹${item.monthlySalary}", fontWeight = FontWeight.Bold)
                                Text("Effective: ${item.effectiveFrom}${if (item.active) " • CURRENT" else ""}", fontSize = 10.sp, color = ShahMediumGrey)
                                if (item.note.isNotBlank()) Text(item.note, fontSize = 10.sp, color = ShahMediumGrey)
                            }
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
            }
        },
        confirmButton = {
            Button(enabled = !saving && !loading && effectiveFrom.isNotBlank() && (if (payType == "MONTHLY") (monthlySalary.toDoubleOrNull() ?: 0.0) > 0 else (dailyRate.toDoubleOrNull() ?: 0.0) > 0), onClick = {
                scope.launch {
                    saving = true
                    error = null
                    try {
                        repository.saveSalaryProfile(SalaryProfileModel(employeeUid = employee.uid, employeeName = employee.name, payType = payType, monthlySalary = if (payType == "MONTHLY") monthlySalary.toDoubleOrNull() ?: 0.0 else 0.0, dailyRate = if (payType == "DAILY") dailyRate.toDoubleOrNull() ?: 0.0 else 0.0, overtimeRatePerHour = overtimeRate.toDoubleOrNull() ?: 0.0, effectiveFrom = effectiveFrom.trim(), note = note.trim()))
                        onDismiss()
                    } catch (e: Exception) { error = e.localizedMessage ?: "Unable to save salary" }
                    finally { saving = false }
                }
            }) { Text(if (saving) "Saving..." else "Save Salary") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } }
    )
}
