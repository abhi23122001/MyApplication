package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.AttendanceRecord
import com.shahsurveyors.myapplication.models.ExpenseRecord
import com.shahsurveyors.myapplication.models.UserProfile
import com.shahsurveyors.myapplication.ui.components.GlobalAsyncLoader
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onNavigateToCompanySettings: () -> Unit = {},
    onNavigateToBankDetails: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {}
) {
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.fetchAdminData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Hub", fontWeight = FontWeight.Bold, color = ShahWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateUserDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, null) },
                    text = { Text("Add Employee") },
                    containerColor = ShahGreen,
                    contentColor = ShahWhite
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().background(ShahGrey)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = ShahWhite,
                contentColor = ShahGreen,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ShahGreen
                        )
                    }
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Logins", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Expenses", fontWeight = FontWeight.Bold) })
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; viewModel.refreshAttendance() },
                    text = { Text("Attendance", fontWeight = FontWeight.Bold) }
                )
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Settings", fontWeight = FontWeight.Bold) })
            }

            when (selectedTab) {
                0 -> PendingLoginsList(viewModel)
                1 -> PendingExpensesList(viewModel)
                2 -> AdminAttendanceList(viewModel)
                3 -> AdminSettingsList(
                    onNavigateToCompanySettings = onNavigateToCompanySettings,
                    onNavigateToBankDetails = onNavigateToBankDetails,
                    onNavigateToTerms = onNavigateToTerms
                )
            }
        }
    }

    if (showCreateUserDialog) {
        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text("Add Employee") },
            text = { Text("Employee creation can be connected here.") },
            confirmButton = {
                TextButton(onClick = { showCreateUserDialog = false }) { Text("OK") }
            }
        )
    }

    GlobalAsyncLoader(isLoading = viewModel.isLoading)
}

@Composable
fun PendingLoginsList(viewModel: AdminViewModel) {
    if (viewModel.pendingUsers.isEmpty() && !viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending login requests", color = ShahMediumGrey)
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(viewModel.pendingUsers, key = { it.uid }) { user ->
            Card(
                Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShahWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(user.name, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(user.email, color = ShahMediumGrey, fontSize = 12.sp)
                            Text("Dept: ${user.department}", color = ShahGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Close, "Reject", tint = ErrorRed)
                        }
                        IconButton(onClick = { viewModel.approveUser(user.uid, user.access) }) {
                            Icon(Icons.Default.Check, "Approve", tint = SuccessGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PendingExpensesList(viewModel: AdminViewModel) {
    if (viewModel.pendingExpenses.isEmpty() && !viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending expense claims", color = ShahMediumGrey)
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(viewModel.pendingExpenses) { expense ->
            Card(
                Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShahWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(expense.userName, fontWeight = FontWeight.Bold, color = ShahBlack)
                        Text(expense.category, color = ShahGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(expense.description, color = ShahMediumGrey, fontSize = 11.sp)
                        Text("₹ ${expense.amount}", color = ShahBlack, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                    IconButton(onClick = { viewModel.approveExpense(expense.id, "REJECTED") }) {
                        Icon(Icons.Default.Cancel, "Reject", tint = ErrorRed)
                    }
                    IconButton(onClick = { viewModel.approveExpense(expense.id, "APPROVED") }) {
                        Icon(Icons.Default.CheckCircle, "Approve", tint = SuccessGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAttendanceList(viewModel: AdminViewModel) {
    val attendanceCount = viewModel.attendanceSummary.size
    val activeEmployeeCount = viewModel.allEmployees.count { it.active }
    val totalCount = maxOf(activeEmployeeCount, attendanceCount)
    val absentCount = (totalCount - attendanceCount).coerceAtLeast(0)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem("Present", attendanceCount.toString(), SuccessGreen, Modifier.weight(1f))
            StatItem("Absent", absentCount.toString(), ErrorRed, Modifier.weight(1f))
            StatItem("Total", totalCount.toString(), ShahGreen, Modifier.weight(1f))
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { viewModel.fetchAdminData() }) {
                Icon(Icons.Default.Refresh, "Refresh")
                Spacer(Modifier.width(6.dp))
                Text("Refresh")
            }
        }

        HorizontalDivider(color = ShahLightGrey)

        if (viewModel.attendanceSummary.isEmpty() && !viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, null, tint = ShahMediumGrey, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No attendance records today", color = ShahMediumGrey, fontSize = 14.sp)
                }
            }
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.attendanceSummary, key = { it.id }) { attendance ->
                AttendanceCard(attendance)
            }
        }
    }
}

@Composable
fun AttendanceCard(attendance: AttendanceRecord) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(attendance.userName, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(attendance.siteName, color = ShahGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (attendance.punchOutTime == null) SuccessGreen.copy(alpha = 0.12f) else ShahLightGrey
                ) {
                    Text(
                        if (attendance.punchOutTime == null) "PUNCHED IN" else "COMPLETED",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = if (attendance.punchOutTime == null) SuccessGreen else ShahBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = ShahLightGrey)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TimeBox("Punch In", formatAttendanceTime(attendance.punchInTime), Modifier.weight(1f))
                TimeBox("Punch Out", formatAttendanceTime(attendance.punchOutTime), Modifier.weight(1f))
            }

            Spacer(Modifier.height(10.dp))
            Text("📍 Punch In GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShahBlack)
            Text("${attendance.punchInLat}, ${attendance.punchInLng}", fontSize = 10.sp, color = ShahMediumGrey)

            attendance.punchOutLat?.let { lat ->
                attendance.punchOutLng?.let { lng ->
                    Spacer(Modifier.height(6.dp))
                    Text("📍 Punch Out GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShahBlack)
                    Text("$lat, $lng", fontSize = 10.sp, color = ShahMediumGrey)
                }
            }

            if (!attendance.selfieUrl.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("📷 Selfie uploaded", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimeBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = ShahGrey) {
        Column(Modifier.padding(10.dp)) {
            Text(label, fontSize = 9.sp, color = ShahMediumGrey)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, color = ShahBlack, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatAttendanceTime(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "--:--"
    return SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
        timeZone = java.util.TimeZone.getTimeZone("GMT+05:30")
    }.format(timestamp.toDate())
}

@Composable
fun StatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = ShahBlack, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminSettingsList(
    onNavigateToCompanySettings: () -> Unit,
    onNavigateToBankDetails: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            SettingsItem("Company Settings", Icons.Default.Business, onNavigateToCompanySettings)
            SettingsItem("Bank Details", Icons.Default.AccountBalance, onNavigateToBankDetails)
            SettingsItem("Terms & Conditions", Icons.Default.Description, onNavigateToTerms)
        }
    }
}

@Composable
private fun SettingsItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ShahWhite)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = ShahGreen)
            Spacer(Modifier.width(12.dp))
            Text(title, color = ShahBlack, fontWeight = FontWeight.Bold)
        }
    }
}
