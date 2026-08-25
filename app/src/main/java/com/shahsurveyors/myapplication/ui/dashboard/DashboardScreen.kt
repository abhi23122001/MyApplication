package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userName: String = "User",
    userRole: String = "Staff",
    userAccess: String = "ALL",
    onNavigateToAttendance: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToSurvey: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToExpense: () -> Unit,
    onNavigateToDsr: () -> Unit,
    onNavigateToClients: () -> Unit,
    isAdmin: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val currentTime = remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf("") }
    var showNotifications by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateSdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
            timeSdf.timeZone = TimeZone.getTimeZone("GMT+5:30")
            currentTime.value = timeSdf.format(Date())
            currentDate.value = dateSdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    val notificationCount = (if (viewModel.presentToday > 0) 1 else 0) +
            (if (viewModel.noticeMessage.isNotBlank()) 1 else 0) + 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("SHAH ERP", style = MaterialTheme.typography.titleMedium, color = ShahWhite, fontWeight = FontWeight.Bold)
                            Text("Workforce & Project Control", fontSize = 9.sp, color = ShahWhite.copy(alpha = 0.72f))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ShahWhite)
                    }
                    BadgedBox(badge = { if (notificationCount > 0) Badge { Text(notificationCount.toString()) } }) {
                        IconButton(onClick = { showNotifications = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ShahWhite)
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Profile",
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(ShahWhite),
                        contentScale = ContentScale.Inside
                    )
                    Spacer(Modifier.width(14.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(ShahGrey),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ShahDarkGreen
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Good day, $userRole", color = ShahWhite.copy(alpha = 0.78f), fontSize = 12.sp)
                            Spacer(Modifier.height(3.dp))
                            Text(userName, color = ShahWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("Stay on top of today's work.", color = ShahWhite.copy(alpha = 0.78f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(currentTime.value, color = ShahWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(currentDate.value, color = ShahWhite.copy(alpha = 0.72f), fontSize = 10.sp)
                        }
                    }
                }
            }

            item { SummarySection(viewModel, isAdmin) }

            item {
                BroadcastSection(viewModel.noticeMessage.ifBlank { "No new admin announcements." })
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ShahBlack)
                        Text("Common tasks at your fingertips", fontSize = 11.sp, color = ShahMediumGrey)
                    }
                    Icon(Icons.Default.Bolt, null, tint = WarningAmber)
                }
                Spacer(Modifier.height(10.dp))
                QuickActionsGrid(onNavigateToAttendance, onNavigateToExpense, onNavigateToAdmin, onNavigateToBilling, isAdmin)
            }
        }
    }

    if (showNotifications) {
        NotificationCenter(viewModel = viewModel, onDismiss = { showNotifications = false })
    }
}

@Composable
fun SummarySection(viewModel: DashboardViewModel, isAdmin: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Employees", viewModel.totalEmployees.toString(), Icons.Default.People, ShahGreen, Modifier.weight(1f))
            SummaryCard("Attendance", "${viewModel.presentToday} Present", Icons.Default.CheckCircle, SuccessGreen, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Projects", viewModel.activeProjects.toString(), Icons.Default.Work, ShahDarkGreen, Modifier.weight(1f))
            SummaryCard("Expenses", viewModel.monthlyExpenses, Icons.Default.AccountBalanceWallet, WarningAmber, Modifier.weight(1f))
        }
        if (isAdmin) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Salary Liability", viewModel.salaryLiability, Icons.Default.Payments, ErrorRed, Modifier.weight(1f))
                SummaryCard("Estimated Profit", viewModel.estimatedProfit, Icons.Default.TrendingUp, SuccessGreen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.10f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = ShahMediumGrey)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShahBlack)
        }
    }
}

@Composable
fun BroadcastSection(message: String) {
    Surface(
        color = ShahGreen.copy(alpha = 0.06f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ShahGreen.copy(alpha = 0.18f))
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(11.dp), color = ShahGreen.copy(alpha = 0.12f)) {
                Icon(Icons.Default.Campaign, null, tint = ShahGreen, modifier = Modifier.padding(9.dp).size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Admin Broadcast", style = MaterialTheme.typography.labelLarge, color = ShahGreen, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = ShahBlack)
            }
        }
    }
}

private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsGrid(
    onAttendance: () -> Unit,
    onExpense: () -> Unit,
    onAdmin: () -> Unit,
    onBilling: () -> Unit,
    isAdmin: Boolean
) {
    val actions = buildList {
        add(QuickAction("Punch IN / OUT", Icons.Default.AccessTime, onAttendance))
        add(QuickAction("Add Expense", Icons.Default.ReceiptLong, onExpense))
        add(QuickAction("Create Quote", Icons.Default.Description, onBilling))
        if (isAdmin) add(QuickAction("Add Employee", Icons.Default.PersonAdd, onAdmin))
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowActions.forEach { action ->
                    OutlinedButton(
                        onClick = action.onClick,
                        modifier = Modifier.weight(1f).height(68.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = ShahWhite, contentColor = ShahGreen),
                        border = BorderStroke(1.dp, ShahGreen.copy(alpha = 0.18f)),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(action.icon, null, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(5.dp))
                            Text(action.title, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                    }
                }
                if (rowActions.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
