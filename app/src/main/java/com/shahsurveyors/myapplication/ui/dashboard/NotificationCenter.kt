package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

private data class DashboardNotification(
    val title: String,
    val message: String,
    val time: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenter(viewModel: DashboardViewModel, onDismiss: () -> Unit) {
    val notifications = remember(viewModel.presentToday, viewModel.noticeMessage, viewModel.activeProjects, viewModel.monthlyExpenses) {
        buildList {
            if (viewModel.presentToday > 0) add(DashboardNotification("Attendance updated", "${viewModel.presentToday} employee${if (viewModel.presentToday == 1) " is" else "s are"} marked present today.", "Today", Icons.Default.CheckCircle, SuccessGreen))
            if (viewModel.noticeMessage.isNotBlank()) add(DashboardNotification("Admin announcement", viewModel.noticeMessage, "New", Icons.Default.Campaign, ShahGreen))
            add(DashboardNotification("Project overview", "${viewModel.activeProjects} active project${if (viewModel.activeProjects == 1) "" else "s"} currently on dashboard.", "Today", Icons.Default.Work, ShahDarkGreen))
            add(DashboardNotification("Expense overview", "This month's recorded expenses: ${viewModel.monthlyExpenses}.", "This month", Icons.Default.AccountBalanceWallet, WarningAmber))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ShahGrey,
        dragHandle = {
            Surface(Modifier.padding(top = 8.dp), RoundedCornerShape(50), color = ShahMediumGrey.copy(alpha = .35f)) {
                Spacer(Modifier.width(38.dp).height(4.dp))
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = ShahGreen.copy(alpha = .10f)) {
                    Icon(Icons.Default.NotificationsActive, null, tint = ShahGreen, modifier = Modifier.padding(10.dp).size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Latest updates from your ERP", color = ShahMediumGrey, fontSize = 11.sp)
                }
                TextButton(onClick = onDismiss) { Text("Done", color = ShahGreen) }
            }
            Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${notifications.size} updates", color = ShahMediumGrey, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = ShahGreen.copy(alpha = .08f)) {
                    Text("LIVE", color = ShahGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Text("TODAY", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = ShahGreen, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 2.dp)) }
                items(notifications) { item ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                            Box {
                                Surface(shape = RoundedCornerShape(12.dp), color = item.tint.copy(alpha = .11f)) {
                                    Icon(item.icon, null, tint = item.tint, modifier = Modifier.padding(10.dp).size(22.dp))
                                }
                                Box(Modifier.size(7.dp).clip(CircleShape).background(item.tint).align(Alignment.TopEnd))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.title, fontWeight = FontWeight.Bold, color = ShahBlack, modifier = Modifier.weight(1f))
                                    Text(item.time, color = ShahMediumGrey, fontSize = 9.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(item.message, color = ShahMediumGrey, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
