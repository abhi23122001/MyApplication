package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import com.shahsurveyors.myapplication.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EmployeeSettingsDialog(
    employeeUid: String,
    employeeName: String,
    employeeId: String,
    department: String,
    currentAccess: String,
    salaryHistory: List<SalaryProfileModel>,
    onDismiss: () -> Unit,
    onSaveSalary: (
        monthlySalary: Double,
        dailyRate: Double,
        overtimeRate: Double,
        effectiveFrom: String,
        note: String
    ) -> Unit,
    onSavePermissions: (newAccess: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Active salary profile if available
    val activeProfile = salaryHistory.find { it.active } ?: salaryHistory.firstOrNull()

    var monthlySalaryText by remember {
        mutableStateOf(if (activeProfile != null && activeProfile.monthlySalary > 0) activeProfile.monthlySalary.toInt().toString() else "15000")
    }

    var dailyRateText by remember {
        mutableStateOf(if (activeProfile != null && activeProfile.dailyRate > 0) activeProfile.dailyRate.toInt().toString() else "")
    }

    var overtimeRateText by remember {
        mutableStateOf(if (activeProfile != null && activeProfile.overtimeRatePerHour > 0) activeProfile.overtimeRatePerHour.toInt().toString() else "100")
    }

    var effectiveFromText by remember {
        mutableStateOf(activeProfile?.effectiveFrom?.ifBlank { null } ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }

    var salaryNoteText by remember { mutableStateOf("") }

    // Permissions state
    val availablePermissions = listOf(
        "ATTENDANCE" to "Attendance & Geofencing",
        "TASKS" to "Task Management",
        "SURVEY" to "Survey Grid Engine",
        "EXPENSE" to "Expense Claims",
        "DSR" to "Daily Status Reports",
        "BILLING" to "Billing & Invoices",
        "CRM" to "Clients CRM",
        "CHAT" to "Team Chat & Radar"
    )

    val selectedPermissions = remember {
        val currentSet = currentAccess.split(",").map { it.trim().uppercase() }.toMutableSet()
        mutableStateListOf<String>().apply { addAll(currentSet) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = ShahWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = employeeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShahBlack
                        )
                        Text(
                            text = "ID: $employeeId • $department",
                            fontSize = 12.sp,
                            color = ShahMediumGrey
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ShahMediumGrey)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ShahGrey,
                    contentColor = ShahGreen,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = ShahGreen
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Salary Settings", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Permissions", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Salary Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = monthlySalaryText,
                                onValueChange = { monthlySalaryText = it },
                                label = { Text("Monthly Base Salary (₹)") },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = ShahGreen) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = dailyRateText,
                                onValueChange = { dailyRateText = it },
                                label = { Text("Daily Rate (Optional, ₹/day)") },
                                placeholder = { Text("Auto-calculated if empty") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ShahGreen) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = overtimeRateText,
                                onValueChange = { overtimeRateText = it },
                                label = { Text("Overtime Rate (₹/hour)") },
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = ShahGreen) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = effectiveFromText,
                                onValueChange = { effectiveFromText = it },
                                label = { Text("Effective From Date (YYYY-MM-DD)") },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = ShahGreen) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = salaryNoteText,
                                onValueChange = { salaryNoteText = it },
                                label = { Text("Increment Note / Remarks") },
                                placeholder = { Text("e.g. Annual revision, Base package") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        if (salaryHistory.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Salary History & Periods",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ShahDarkGreen
                                )
                            }

                            items(salaryHistory) { history ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = ShahGrey),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "₹ ${history.monthlySalary.toInt()}/month",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = ShahBlack
                                            )
                                            val period = if (history.effectiveTo != null) {
                                                "${history.effectiveFrom} to ${history.effectiveTo}"
                                            } else {
                                                "From ${history.effectiveFrom} (Active)"
                                            }
                                            Text(
                                                text = period,
                                                fontSize = 11.sp,
                                                color = ShahMediumGrey
                                            )
                                        }

                                        if (history.active) {
                                            Surface(
                                                color = SuccessGreen.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val monthly = monthlySalaryText.toDoubleOrNull() ?: 15000.0
                            val daily = dailyRateText.toDoubleOrNull() ?: 0.0
                            val ot = overtimeRateText.toDoubleOrNull() ?: 100.0
                            onSaveSalary(
                                monthly,
                                daily,
                                ot,
                                effectiveFromText.trim(),
                                salaryNoteText.trim()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE SALARY PROFILE", fontWeight = FontWeight.Bold)
                    }

                } else {
                    // Permissions Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Grant / Revoke Module Access",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ShahDarkGreen
                            )
                            Text(
                                text = "Employee will only see modules checked below.",
                                fontSize = 11.sp,
                                color = ShahMediumGrey
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(availablePermissions) { (key, label) ->
                            val isChecked = selectedPermissions.contains(key)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) {
                                            selectedPermissions.remove(key)
                                        } else {
                                            selectedPermissions.add(key)
                                        }
                                    }
                                    .background(if (isChecked) ShahGreen.copy(alpha = 0.05f) else Color.Transparent)
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedPermissions.add(key) else selectedPermissions.remove(key)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = ShahGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ShahBlack,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newAccessString = selectedPermissions.joinToString(",")
                            onSavePermissions(newAccessString)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UPDATE PERMISSIONS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
