package com.shahsurveyors.myapplication.ui.dashboard

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
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("SHAH ERP", style = MaterialTheme.typography.titleLarge, color = ShahWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ShahWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ShahWhite)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ShahWhite),
                        contentScale = ContentScale.Inside
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ShahGrey),
            contentPadding = PaddingValues(16.dp)
        ) {
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
                            Text(currentTime.value, style = MaterialTheme.typography.labelLarge, color = ShahGreen, fontWeight = FontWeight.Bold)
                            Text(currentDate.value, style = MaterialTheme.typography.labelSmall, color = ShahMediumGrey)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SummarySection(viewModel, isAdmin)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                BroadcastSection(viewModel.noticeMessage.ifBlank { "No new admin announcements." })
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                QuickActionsGrid(
                    onNavigateToAttendance,
                    onNavigateToExpense,
                    onNavigateToAdmin,
                    onNavigateToBilling,
                    isAdmin
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SummarySection(viewModel: DashboardViewModel, isAdmin: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard("Employees", viewModel.totalEmployees.toString(), Icons.Default.People, ShahGreen, Modifier.weight(1f))
            SummaryCard("Attendance", "${viewModel.presentToday} Present", Icons.Default.CheckCircle, SuccessGreen, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard("Projects", viewModel.activeProjects.toString(), Icons.Default.Work, ShahDarkGreen, Modifier.weight(1f))
            SummaryCard("Expenses", viewModel.monthlyExpenses, Icons.Default.AccountBalanceWallet, WarningAmber, Modifier.weight(1f))
        }

        if (isAdmin) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = ShahMediumGrey)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ShahBlack)
        }
    }
}

@Composable
fun BroadcastSection(message: String) {
    Surface(
        color = ShahGreen.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ShahGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Campaign, contentDescription = null, tint = ShahGreen, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Admin Broadcast", style = MaterialTheme.typography.labelLarge, color = ShahGreen, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = ShahBlack)
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onAttendance: () -> Unit,
    onExpense: () -> Unit,
    onAdmin: () -> Unit,
    onBilling: () -> Unit,
    isAdmin: Boolean
) {
    val actions = mutableListOf(
        Pair("Punch IN/OUT", onAttendance),
        Pair("Add Expense", onExpense),
        Pair("Create Quote", onBilling)
    )
    if (isAdmin) actions.add(Pair("Add Employee", onAdmin))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val chunkedActions = actions.chunked(2)
        chunkedActions.forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowActions.forEach { action ->
                    Button(
                        onClick = action.second,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahWhite, contentColor = ShahGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShahGreen.copy(alpha = 0.3f))
                    ) {
                        Text(action.first, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
                if (rowActions.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
