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
import com.shahsurveyors.myapplication.data.NotificationRepository
import com.shahsurveyors.myapplication.ui.admin.hasModuleAccess
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
    onNavigateToProjects: () -> Unit = {},
    onNavigateToBroadcast: () -> Unit = {},
    isAdmin: Boolean = false,
    notificationUid: String = "",
    onRefresh: () -> Unit = {}
) {
    val currentTime = remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf("") }
    var showNotifications by remember { mutableStateOf(false) }
    var notificationCount by remember { mutableIntStateOf(0) }
    val notificationRepository = remember { NotificationRepository() }

    fun can(module: String): Boolean = hasModuleAccess(userAccess, module)

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

    LaunchedEffect(notificationUid, isAdmin, showNotifications) {
        notificationCount = notificationRepository.getUnreadCount(notificationUid, isAdmin)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "SHAH ERP", modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("SHAH ERP", style = MaterialTheme.typography.titleMedium, color = ShahWhite, fontWeight = FontWeight.Bold)
                            Text("Workforce & Project Control", fontSize = 9.sp, color = ShahWhite.copy(alpha = 0.72f))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "Refresh", tint = ShahWhite) }
                    BadgedBox(badge = { if (notificationCount > 0) Badge { Text(notificationCount.toString()) } }) {
                        IconButton(onClick = { showNotifications = true }) { Icon(Icons.Default.Notifications, "Notifications", tint = ShahWhite) }
                    }
                    Spacer(Modifier.width(4.dp))
                    Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "Profile", modifier = Modifier.size(32.dp).clip(CircleShape).background(ShahWhite), contentScale = ContentScale.Inside)
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
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = ShahDarkGreen) {
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
            item {
                SummarySection(
                    viewModel = viewModel,
                    isAdmin = isAdmin,
                    userAccess = userAccess,
                    onEmployeesClick = onNavigateToAdmin,
                    onAttendanceClick = onNavigateToAttendance,
                    onProjectsClick = onNavigateToProjects,
                    onExpensesClick = onNavigateToExpense
                )
            }
            if (isAdmin || can("CHAT")) {
                item { BroadcastSection(viewModel.noticeMessage.ifBlank { "No new admin announcements." }, onClick = if (isAdmin) onNavigateToBroadcast else null) }
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
                QuickActionsGrid(
                    onAttendance = onNavigateToAttendance,
                    onExpense = onNavigateToExpense,
                    onAdmin = onNavigateToAdmin,
                    onBilling = onNavigateToBilling,
                    onTasks = onNavigateToTasks,
                    userAccess = userAccess,
                    isAdmin = isAdmin
                )
            }
        }
    }

    if (showNotifications) NotificationCenter(viewModel = viewModel, onDismiss = { showNotifications = false })
}

@Composable
fun SummarySection(
    viewModel: DashboardViewModel,
    isAdmin: Boolean,
    userAccess: String,
    onEmployeesClick: () -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onProjectsClick: () -> Unit = {},
    onExpensesClick: () -> Unit = {}
) {
    val canAttendance = hasModuleAccess(userAccess, "ATTENDANCE")
    val canEmployees = hasModuleAccess(userAccess, "ADMIN")
    val canProjects = hasModuleAccess(userAccess, "PROJECTS")
    val canExpenses = hasModuleAccess(userAccess, "EXPENSE")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val topCards = buildList {
            if (canEmployees) add(Triple("Employees", viewModel.totalEmployees.toString(), Icons.Default.People to ShahGreen))
            if (canAttendance) add(Triple("Attendance", "${viewModel.presentToday} Present", Icons.Default.CheckCircle to SuccessGreen))
        }
        if (topCards.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                topCards.forEach { (title, value, iconColor) ->
                    SummaryCard(title, value, iconColor.first, iconColor.second, Modifier.weight(1f), when (title) {
                        "Employees" -> onEmployeesClick
                        else -> onAttendanceClick
                    })
                }
                if (topCards.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        val secondCards = buildList {
            if (canProjects) add(Triple("Projects", viewModel.activeProjects.toString(), Icons.Default.Work to ShahDarkGreen))
            if (canExpenses) add(Triple("Expenses", viewModel.monthlyExpenses, Icons.Default.AccountBalanceWallet to WarningAmber))
        }
        if (secondCards.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                secondCards.forEach { (title, value, iconColor) ->
                    SummaryCard(title, value, iconColor.first, iconColor.second, Modifier.weight(1f), when (title) {
                        "Projects" -> onProjectsClick
                        else -> onExpensesClick
                    })
                }
                if (secondCards.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        if (isAdmin && hasModuleAccess(userAccess, "ADMIN")) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Salary Liability", viewModel.salaryLiability, Icons.Default.Payments, ErrorRed, Modifier.weight(1f))
                SummaryCard("Estimated Profit", viewModel.estimatedProfit, Icons.Default.TrendingUp, SuccessGreen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
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
fun BroadcastSection(message: String, onClick: (() -> Unit)? = null) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahGreen.copy(alpha = 0.06f)),
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
                if (onClick != null) Text("Tap to manage announcements", style = MaterialTheme.typography.labelSmall, color = ShahGreen)
            }
        }
    }
}

private data class QuickAction(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun QuickActionsGrid(
    onAttendance: () -> Unit,
    onExpense: () -> Unit,
    onAdmin: () -> Unit,
    onBilling: () -> Unit,
    onTasks: () -> Unit,
    userAccess: String,
    isAdmin: Boolean
) {
    val actions = buildList {
        if (hasModuleAccess(userAccess, "ATTENDANCE")) add(QuickAction("Punch IN / OUT", Icons.Default.AccessTime, onAttendance))
        if (hasModuleAccess(userAccess, "EXPENSE")) add(QuickAction("Add Expense", Icons.Default.ReceiptLong, onExpense))
        if (hasModuleAccess(userAccess, "BILLING")) add(QuickAction("Create Quote", Icons.Default.Description, onBilling))
        if (hasModuleAccess(userAccess, "TASKS")) add(QuickAction(if (isAdmin) "Assign Task" else "My Tasks", Icons.Default.Assignment, onTasks))
        if (hasModuleAccess(userAccess, "ADMIN")) add(QuickAction("Add Employee", Icons.Default.PersonAdd, onAdmin))
    }
    if (actions.isEmpty()) {
        Text("No quick actions assigned.", color = ShahMediumGrey, fontSize = 12.sp)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowActions.forEach { action ->
                    OutlinedButton(onClick = action.onClick, modifier = Modifier.weight(1f).height(68.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = ShahWhite, contentColor = ShahGreen), border = BorderStroke(1.dp, ShahGreen.copy(alpha = 0.18f)), contentPadding = PaddingValues(10.dp)) {
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
