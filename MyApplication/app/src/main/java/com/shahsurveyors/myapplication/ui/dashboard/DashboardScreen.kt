package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUid: String = "",
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
    onNavigateToSalary: () -> Unit = {},

    isAdmin: Boolean = false,
    isSyncing: Boolean = false,
    onRefresh: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(currentUid, isAdmin) {
        viewModel.startListeningToNotifications(currentUid, isAdmin)
        viewModel.fetchDashboardData()
    }

    LaunchedEffect(isSyncing) {
        if (!isSyncing) {
            pullToRefreshState.endRefresh()
        }
    }

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.fetchDashboardData()
            onRefresh()
        }
    }

    // Live IST Clock
    LaunchedEffect(Unit) {
        while (true) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT+5:30")
            }
            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT+5:30")
            }
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "SHAH Logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "SHAH ERP",
                            style = MaterialTheme.typography.titleLarge,
                            color = ShahWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showNotificationDialog = true }) {
                        val count = viewModel.notificationList.count { !it.isRead }
                        if (count > 0) {
                            BadgedBox(badge = { Badge { Text(count.toString()) } }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ShahWhite)
                            }
                        } else {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ShahWhite)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ShahWhite),
                        contentScale = ContentScale.Inside
                    )

                    Spacer(Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Welcome Card
                item {
                    Column {
                        Text(
                            text = "Welcome back, $userRole",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ShahDarkGrey
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = ShahBlack,
                                modifier = Modifier.weight(1f)
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currentTime,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ShahGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShahMediumGrey
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }

                // Summary Section
                item {
                    SummarySection(
                        viewModel = viewModel,
                        isAdmin = isAdmin
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Admin Broadcast Notice Card
                item {
                    DashboardBroadcastNoticeCard(
                        notice = viewModel.noticeMessage,
                        isAdmin = isAdmin,
                        onNoticeUpdated = { newNotice ->
                            viewModel.updateNotice(newNotice)
                        }
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Quick Actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ShahBlack
                    )

                    Spacer(Modifier.height(12.dp))

                    QuickActionsGrid(
                        onAttendance = onNavigateToAttendance,
                        onExpense = onNavigateToExpense,
                        onAdmin = onNavigateToAdmin,
                        onBilling = onNavigateToBilling,
                        onTasks = onNavigateToTasks,
                        onSalary = onNavigateToSalary,
                        isAdmin = isAdmin
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = ShahWhite,
                contentColor = ShahGreen
            )
        }
    }

    if (showNotificationDialog) {
        NotificationCenterDialog(
            notifications = viewModel.notificationList,
            onDismiss = { showNotificationDialog = false }
        )
    }
}

@Composable
fun SummarySection(
    viewModel: DashboardViewModel,
    isAdmin: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = if (isAdmin) "Total Staff" else "Team Active",
                value = viewModel.totalEmployees.toString(),
                icon = Icons.Default.People,
                color = ShahGreen,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Present Today",
                value = "${viewModel.presentToday} Present",
                icon = Icons.Default.CheckCircle,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "Active Projects",
                value = viewModel.activeProjects.toString(),
                icon = Icons.Default.Work,
                color = ShahDarkGreen,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = if (isAdmin) "Company Expenses" else "My Claims",
                value = viewModel.monthlyExpenses,
                icon = Icons.Default.AccountBalanceWallet,
                color = WarningAmber,
                modifier = Modifier.weight(1f)
            )
        }

        if (isAdmin) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Salary Liability",
                    value = viewModel.salaryLiability,
                    icon = Icons.Default.Payments,
                    color = ErrorRed,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Estimated Margin",
                    value = viewModel.estimatedProfit,
                    icon = Icons.Default.TrendingUp,
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = ShahMediumGrey
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ShahBlack
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onAttendance: () -> Unit,
    onExpense: () -> Unit,
    onAdmin: () -> Unit,
    onBilling: () -> Unit,
    onTasks: () -> Unit,
    onSalary: () -> Unit,
    isAdmin: Boolean
) {
    val actions = buildList {
        add(QuickAction("Punch IN/OUT", onAttendance))
        add(QuickAction("My Tasks", onTasks))
        add(QuickAction("Salary / Slip", onSalary))

        if (isAdmin) {
            add(QuickAction("Payroll Management", onSalary))
            add(QuickAction("Staff & Salary Settings", onAdmin))
            add(QuickAction("Create Quote / Invoice", onBilling))
        } else {
            add(QuickAction("Claim Expense", onExpense))
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(if (actions.size <= 2) 80.dp else if (actions.size <= 4) 150.dp else 220.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(actions) { action ->
            Button(
                onClick = action.action,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShahWhite,
                    contentColor = ShahGreen
                ),
                border = BorderStroke(1.dp, ShahGreen.copy(alpha = 0.3f))
            ) {
                Text(
                    text = action.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private data class QuickAction(
    val title: String,
    val action: () -> Unit
)