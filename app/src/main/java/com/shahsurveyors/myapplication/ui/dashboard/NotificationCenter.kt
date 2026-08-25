package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

private data class DashboardNotification(
    val title: String,
    val message: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenter(
    viewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    val notifications = remember(
        viewModel.presentToday,
        viewModel.noticeMessage,
        viewModel.activeProjects,
        viewModel.monthlyExpenses
    ) {
        buildList {
            if (viewModel.presentToday > 0) {
                add(DashboardNotification(
                    "Attendance updated",
                    "${viewModel.presentToday} employee${if (viewModel.presentToday == 1) " is" else "s are"} marked present today.",
                    Icons.Default.CheckCircle,
                    SuccessGreen
                ))
            }
            if (viewModel.noticeMessage.isNotBlank()) {
                add(DashboardNotification(
                    "Admin announcement",
                    viewModel.noticeMessage,
                    Icons.Default.Campaign,
                    ShahGreen
                ))
            }
            add(DashboardNotification(
                "Project overview",
                "${viewModel.activeProjects} active project${if (viewModel.activeProjects == 1) "" else "s"} currently on dashboard.",
                Icons.Default.Work,
                ShahDarkGreen
            ))
            add(DashboardNotification(
                "Expense overview",
                "This month's recorded expenses: ${viewModel.monthlyExpenses}.",
                Icons.Default.AccountBalanceWallet,
                WarningAmber
            ))
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Latest updates from your ERP", color = ShahMediumGrey, fontSize = 12.sp)
                }
                TextButton(onClick = onDismiss) { Text("Done", color = ShahGreen) }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ShahWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = item.tint.copy(alpha = 0.12f)) {
                                Icon(item.icon, null, tint = item.tint, modifier = Modifier.padding(10.dp).size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, color = ShahBlack)
                                Spacer(Modifier.height(3.dp))
                                Text(item.message, color = ShahMediumGrey, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
