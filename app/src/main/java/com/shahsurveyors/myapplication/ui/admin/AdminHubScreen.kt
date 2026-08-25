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
import com.shahsurveyors.myapplication.ui.components.GlobalAsyncLoader
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(viewModel: AdminViewModel, onBack: () -> Unit, onNavigateToCompanySettings: () -> Unit = {}, onNavigateToBankDetails: () -> Unit = {}, onNavigateToTerms: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { viewModel.fetchAdminData() }
    val titles = listOf("Logins", "Expenses", "Attendance", "Settings")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Admin Hub", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Control center • people, approvals & settings", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            Surface(Modifier.fillMaxWidth().padding(16.dp), RoundedCornerShape(18.dp), color = ShahDarkGreen) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = ShahWhite.copy(.12f)) { Icon(Icons.Default.AdminPanelSettings, null, tint = ShahWhite, modifier = Modifier.padding(10.dp).size(25.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Management Console", color = ShahWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("${viewModel.allEmployees.size} employees • ${viewModel.pendingUsers.size} pending approvals", fontSize = 10.sp, color = ShahWhite.copy(.72f)) }
                }
            }
            ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = ShahWhite, contentColor = ShahGreen, edgePadding = 8.dp, indicator = { positions -> if (selectedTab < positions.size) TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[selectedTab]), color = ShahGreen) }) {
                titles.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index; if (index == 2) viewModel.refreshAttendance() }, text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) }) }
            }
            when (selectedTab) {
                0 -> PendingLoginsList(viewModel)
                1 -> PendingExpensesList(viewModel)
                2 -> AdminAttendanceList(viewModel)
                else -> AdminSettingsList(onNavigateToCompanySettings, onNavigateToBankDetails, onNavigateToTerms)
            }
        }
    }
    GlobalAsyncLoader(isLoading = viewModel.isLoading)
}

@Composable
private fun PendingLoginsList(viewModel: AdminViewModel) {
    if (viewModel.pendingUsers.isEmpty() && !viewModel.isLoading) { EmptyAdminState(Icons.Default.PersonSearch, "No pending login requests", "New access requests will appear here."); return }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(viewModel.pendingUsers, key = { it.uid }) { user ->
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, null, tint = WarningAmber, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) { Text(user.name, color = ShahBlack, fontWeight = FontWeight.Bold); Text(user.email, fontSize = 10.sp, color = ShahMediumGrey); Text("${user.department} • ${user.access}", fontSize = 10.sp, color = ShahGreen, fontWeight = FontWeight.Bold) }
                    IconButton(onClick = { viewModel.approveUser(user.uid, user.access) }) { Icon(Icons.Default.CheckCircle, "Approve", tint = SuccessGreen) }
                }
            }
        }
    }
}

@Composable
private fun PendingExpensesList(viewModel: AdminViewModel) {
    if (viewModel.pendingExpenses.isEmpty() && !viewModel.isLoading) { EmptyAdminState(Icons.Default.ReceiptLong, "No pending expense claims", "Approved or new claims will be reflected here."); return }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(viewModel.pendingExpenses) { expense ->
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, null, tint = ShahGreen, modifier = Modifier.size(26.dp)); Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) { Text(expense.userName, fontWeight = FontWeight.Bold, color = ShahBlack); Text(expense.category, fontSize = 10.sp, color = ShahGreen, fontWeight = FontWeight.Bold); Text(expense.description, fontSize = 10.sp, color = ShahMediumGrey); Text("₹ ${expense.amount}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                    Column { IconButton(onClick = { viewModel.approveExpense(expense.id, "APPROVED") }) { Icon(Icons.Default.CheckCircle, "Approve", tint = SuccessGreen) }; IconButton(onClick = { viewModel.approveExpense(expense.id, "REJECTED") }) { Icon(Icons.Default.Cancel, "Reject", tint = ErrorRed) } }
                }
            }
        }
    }
}

@Composable
private fun AdminAttendanceList(viewModel: AdminViewModel) {
    val present = viewModel.attendanceSummary.size
    val total = maxOf(viewModel.allEmployees.count { it.active }, present)
    val absent = (total - present).coerceAtLeast(0)
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatItem("Present", present.toString(), SuccessGreen, Modifier.weight(1f)); StatItem("Absent", absent.toString(), ErrorRed, Modifier.weight(1f)); StatItem("Total", total.toString(), ShahGreen, Modifier.weight(1f)) }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) { OutlinedButton(onClick = { viewModel.fetchAdminData() }) { Icon(Icons.Default.Refresh, "Refresh"); Spacer(Modifier.width(6.dp)); Text("Refresh") } }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(viewModel.attendanceSummary, key = { it.id }) { AttendanceCard(it) } }
    }
}

@Composable
private fun AttendanceCard(attendance: AttendanceRecord) {
    val active = attendance.punchOutTime == null
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(attendance.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(attendance.siteName, fontSize = 10.sp, color = ShahGreen, fontWeight = FontWeight.Bold) }; Surface(shape = RoundedCornerShape(50), color = if (active) SuccessGreen.copy(.12f) else ShahLightGrey) { Text(if (active) "PUNCHED IN" else "COMPLETED", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (active) SuccessGreen else ShahBlack) } }
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = ShahLightGrey)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TimeBox("Punch In", formatAttendanceTime(attendance.punchInTime), Modifier.weight(1f)); TimeBox("Punch Out", formatAttendanceTime(attendance.punchOutTime), Modifier.weight(1f)) }
            Spacer(Modifier.height(8.dp)); Text("GPS: ${attendance.punchInLat}, ${attendance.punchInLng}", fontSize = 10.sp, color = ShahMediumGrey)
            if (!attendance.selfieUrl.isNullOrBlank()) Text("✓ Verification selfie uploaded", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable private fun TimeBox(label: String, value: String, modifier: Modifier) { Surface(modifier, RoundedCornerShape(10.dp), color = ShahGrey) { Column(Modifier.padding(10.dp)) { Text(label, fontSize = 9.sp, color = ShahMediumGrey); Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ShahBlack) } } }
@Composable private fun StatItem(label: String, value: String, color: Color, modifier: Modifier) { Surface(modifier, RoundedCornerShape(12.dp), color = color.copy(.10f)) { Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold); Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = ShahBlack) } } }
@Composable private fun EmptyAdminState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) { Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = ShahMediumGrey, modifier = Modifier.size(45.dp)); Spacer(Modifier.height(10.dp)); Text(title, fontWeight = FontWeight.Bold); Text(subtitle, fontSize = 11.sp, color = ShahMediumGrey) } } }
@Composable private fun AdminSettingsList(onCompany: () -> Unit, onBank: () -> Unit, onTerms: () -> Unit) { LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { SettingsItem("Company Settings", Icons.Default.Business, onCompany); SettingsItem("Bank Details", Icons.Default.AccountBalance, onBank); SettingsItem("Terms & Conditions", Icons.Default.Description, onTerms) } } }
@Composable private fun SettingsItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { Card(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = ShahGreen, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(12.dp)); Text(title, color = ShahBlack, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, "Open", tint = ShahMediumGrey) } } }
private fun formatAttendanceTime(timestamp: com.google.firebase.Timestamp?): String { if (timestamp == null) return "--:--"; return SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply { timeZone = java.util.TimeZone.getTimeZone("GMT+05:30") }.format(timestamp.toDate()) }
