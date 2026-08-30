package com.shahsurveyors.myapplication.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EmployeeItem(
    val uid: String = "",
    val name: String = "",
    val id: String = "",
    val dept: String = "SURVEY",
    val role: String = "STAFF",
    val access: String = "ATTENDANCE,CHAT",
    val phone: String = "",
    val email: String = "",
    val active: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val salaryRepository = remember { SalaryRepository() }

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val employeeList = remember { mutableStateListOf<EmployeeItem>() }

    // Dialog states
    var selectedEmployeeForSettings by remember { mutableStateOf<EmployeeItem?>(null) }
    var selectedEmployeeSalaryHistory by remember { mutableStateOf<List<SalaryProfileModel>>(emptyList()) }
    var showAddEmployeeDialog by remember { mutableStateOf(false) }

    fun loadEmployees() {
        coroutineScope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("users").get().await()
                employeeList.clear()
                for (doc in snapshot.documents) {
                    val uid = doc.id
                    val name = doc.getString("name") ?: "Staff User"
                    val id = doc.getString("employeeId") ?: doc.getString("id") ?: uid.take(6).uppercase()
                    val dept = doc.getString("department") ?: doc.getString("dept") ?: "SURVEY"
                    val role = doc.getString("role") ?: "STAFF"
                    val access = doc.getString("access") ?: "ATTENDANCE,CHAT"
                    val phone = doc.getString("phone") ?: ""
                    val email = doc.getString("email") ?: ""
                    val active = doc.getBoolean("active") ?: true

                    employeeList.add(
                        EmployeeItem(
                            uid = uid,
                            name = name,
                            id = id,
                            dept = dept,
                            role = role,
                            access = access,
                            phone = phone,
                            email = email,
                            active = active
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error loading staff: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadEmployees()
    }

    val filteredEmployees = remember(employeeList, searchQuery) {
        if (searchQuery.isBlank()) {
            employeeList.toList()
        } else {
            employeeList.filter { employee ->
                employee.name.contains(searchQuery, ignoreCase = true) ||
                        employee.id.contains(searchQuery, ignoreCase = true) ||
                        employee.dept.contains(searchQuery, ignoreCase = true) ||
                        employee.role.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Employee & Salary Settings",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEmployeeDialog = true },
                containerColor = ShahGreen,
                contentColor = ShahWhite
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Employee")
            }
        },
        containerColor = ShahGrey
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShahGrey)
        ) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name, ID, department...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShahGreen,
                    unfocusedBorderColor = ShahMediumGrey,
                    focusedContainerColor = ShahWhite,
                    unfocusedContainerColor = ShahWhite
                )
            )

            Text(
                text = "${filteredEmployees.size} Registered Employee(s) • Tap employee for Salary & Permissions",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                color = ShahDarkGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (filteredEmployees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No employees found", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEmployees, key = { it.uid }) { employee ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        val history = salaryRepository.getSalaryProfilesForEmployee(employee.uid)
                                        selectedEmployeeSalaryHistory = history
                                        selectedEmployeeForSettings = employee
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ShahWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    color = ShahGreen.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = employee.name.trim().firstOrNull()?.uppercase() ?: "?",
                                            fontWeight = FontWeight.Bold,
                                            color = ShahGreen,
                                            fontSize = 20.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = employee.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ShahBlack
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "ID: ${employee.id} • ${employee.role.uppercase()}",
                                        fontSize = 12.sp,
                                        color = ShahMediumGrey
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${employee.dept} • Permissions: ${employee.access}",
                                        color = ShahGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Configure Settings",
                                    tint = ShahGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Employee Settings Dialog (Salary & Permissions)
    selectedEmployeeForSettings?.let { emp ->
        EmployeeSettingsDialog(
            employeeUid = emp.uid,
            employeeName = emp.name,
            employeeId = emp.id,
            department = emp.dept,
            currentAccess = emp.access,
            salaryHistory = selectedEmployeeSalaryHistory,
            onDismiss = { selectedEmployeeForSettings = null },
            onSaveSalary = { monthly, daily, ot, effectiveFrom, note ->
                coroutineScope.launch {
                    val profile = SalaryProfileModel(
                        employeeUid = emp.uid,
                        employeeName = emp.name,
                        employeeId = emp.id,
                        department = emp.dept,
                        monthlySalary = monthly,
                        dailyRate = daily,
                        overtimeRatePerHour = ot,
                        effectiveFrom = effectiveFrom,
                        note = note,
                        setByName = "Admin",
                        active = true
                    )
                    val success = salaryRepository.saveSalaryProfile(profile)
                    if (success) {
                        Toast.makeText(context, "Salary updated for ${emp.name}", Toast.LENGTH_SHORT).show()
                        selectedEmployeeForSettings = null
                    } else {
                        Toast.makeText(context, "Failed to save salary", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onSavePermissions = { newAccess ->
                coroutineScope.launch {
                    try {
                        firestore.collection("users").document(emp.uid)
                            .update("access", newAccess)
                            .await()
                        Toast.makeText(context, "Permissions updated for ${emp.name}", Toast.LENGTH_SHORT).show()
                        selectedEmployeeForSettings = null
                        loadEmployees()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Quick Add Employee Dialog
    if (showAddEmployeeDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newDept by remember { mutableStateOf("SURVEY") }
        var newRole by remember { mutableStateOf("employee") }

        AlertDialog(
            onDismissRequest = { showAddEmployeeDialog = false },
            title = { Text("Add New Staff Member", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDept,
                        onValueChange = { newDept = it },
                        label = { Text("Department (e.g. SURVEY, FINANCE)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newRole,
                        onValueChange = { newRole = it },
                        label = { Text("Role (employee, surveyor, admin)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    val newDoc = firestore.collection("users").document()
                                    val empData = hashMapOf(
                                        "name" to newName.trim(),
                                        "phone" to newPhone.trim(),
                                        "department" to newDept.trim().uppercase(),
                                        "role" to newRole.trim().lowercase(),
                                        "employeeId" to "EMP${(100..999).random()}",
                                        "access" to "ATTENDANCE,TASKS,CHAT",
                                        "active" to true,
                                        "approved" to true
                                    )
                                    newDoc.set(empData).await()
                                    Toast.makeText(context, "Employee $newName added", Toast.LENGTH_SHORT).show()
                                    showAddEmployeeDialog = false
                                    loadEmployees()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite)
                ) {
                    Text("ADD EMPLOYEE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEmployeeDialog = false }) {
                    Text("CANCEL", color = ShahGreen)
                }
            }
        )
    }
}