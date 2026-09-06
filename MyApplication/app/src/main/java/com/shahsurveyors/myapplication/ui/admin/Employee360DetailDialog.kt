package com.shahsurveyors.myapplication.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shahsurveyors.myapplication.models.DailyPunchLog
import com.shahsurveyors.myapplication.models.Employee360Report
import com.shahsurveyors.myapplication.ui.theme.*

@Composable
fun Employee360DetailDialog(
    report: Employee360Report?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onGenerateSlip: (() -> Unit)? = null
) {
    if (report == null && !isLoading) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ShahWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Close Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!report?.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = report?.photoUrl,
                                contentDescription = report?.name,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, ShahGreen, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(ShahDarkGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (report?.name?.take(1) ?: "U").uppercase(),
                                    color = ShahWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = report?.name ?: "Loading Details...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = ShahBlack
                            )
                            Text(
                                text = "ID: ${report?.employeeId ?: ""} • ${report?.department ?: ""} (${report?.month ?: ""})",
                                fontSize = 12.sp,
                                color = ShahMediumGrey
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ShahDarkGrey)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ShahLightGrey)

                if (isLoading || report == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ShahGreen)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. ATTENDANCE BREAKDOWN SUMMARY CARD
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = ShahGreen.copy(alpha = 0.06f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "📊 Attendance Summary (${report.month})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ShahDarkGreen
                                        )
                                        Text(
                                            "Working Days: ${report.totalWorkingDays}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ShahDarkGrey
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        AttendanceStatBox("Present (Full)", "${report.presentDaysCount} Days", SuccessGreen)
                                        AttendanceStatBox("Half Days", "${report.halfDaysCount} Days", WarningYellow)
                                        AttendanceStatBox("Absent", "${report.absentDaysCount} Days", ErrorRed)
                                        AttendanceStatBox("Leaves", "${report.approvedLeaveDaysCount} Days", InfoBlue)
                                    }
                                }
                            }
                        }

                        // 2. SALARY & EARNINGS CARD
                        item {
                            val payroll = report.payroll
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = ShahCardBg)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "💰 Salary & Net Payable",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ShahDarkGreen
                                        )
                                        if (payroll != null) {
                                            Text(
                                                "Net: ₹ ${payroll.netSalary.toInt()}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = ShahGreen
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (payroll != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Base Rate: ₹ ${payroll.baseMonthlySalary.toInt()} (₹ ${payroll.dailyRate.toInt()}/day)", fontSize = 12.sp, color = ShahDarkGrey)
                                            Text("Gross Earned: ₹ ${payroll.grossSalaryEarned.toInt()}", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Advance EMI Deducted: -₹ ${payroll.advanceDeduction.toInt()}", fontSize = 12.sp, color = ErrorRed)
                                            Text("Absence Cut: -₹ ${payroll.absenceDeduction.toInt()}", fontSize = 12.sp, color = ErrorRed)
                                        }

                                        if (onGenerateSlip != null) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            OutlinedButton(
                                                onClick = onGenerateSlip,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ShahDarkGreen)
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Download Salary Slip PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. ADVANCE & EXPENSES STATUS CARD
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = ShahCardBg)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        "💳 Advance Salary & Expense Claims",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ShahDarkGreen
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Advance Taken: ₹ ${report.totalAdvanceApproved.toInt()}", fontSize = 12.sp, color = ShahDarkGrey)
                                        Text("Remaining Bal: ₹ ${report.advanceRemainingBalance.toInt()}", fontSize = 12.sp, color = ErrorRed, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Expenses Claimed: ₹ ${report.totalExpensesClaimed.toInt()}", fontSize = 12.sp, color = ShahDarkGrey)
                                        Text("Expenses Approved: ₹ ${report.totalExpensesApproved.toInt()}", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // 4. DAILY PUNCH LOGS & GPS CROSS-CHECK SECTION
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "📍 Daily Punches & GPS Map Logs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ShahBlack
                                )
                                Text(
                                    "${report.dailyPunchLogs.size} Records",
                                    fontSize = 12.sp,
                                    color = ShahMediumGrey
                                )
                            }
                        }

                        if (report.dailyPunchLogs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No punch records found for this month.", color = ShahMediumGrey, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(report.dailyPunchLogs, key = { it.date }) { log ->
                                DailyPunchLogCard(log = log, context = context)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatBox(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        Text(label, fontSize = 10.sp, color = ShahDarkGrey)
    }
}

@Composable
private fun DailyPunchLogCard(log: DailyPunchLog, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ShahDarkGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.date,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ShahBlack
                    )
                }

                val badgeColor = when (log.dayStatus) {
                    "PRESENT" -> SuccessGreen
                    "HALF_DAY" -> WarningYellow
                    else -> ErrorRed
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.dayStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "IN: ${log.punchInTime.ifBlank { "Missed" }}",
                    fontSize = 12.sp,
                    color = if (log.punchInTime.isNotBlank()) SuccessGreen else ErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "OUT: ${log.punchOutTime.ifBlank { "Missed / Active" }}",
                    fontSize = 12.sp,
                    color = if (log.punchOutTime.isNotBlank()) ShahDarkGreen else WarningYellow,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = log.workArea,
                    fontSize = 11.sp,
                    color = ShahDarkGrey,
                    maxLines = 1
                )
            }

            if (log.punchInLat != 0.0 && log.punchInLng != 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GPS: ${String.format("%.5f", log.punchInLat)}, ${String.format("%.5f", log.punchInLng)}",
                        fontSize = 11.sp,
                        color = ShahMediumGrey
                    )

                    Button(
                        onClick = {
                            try {
                                val gmmIntentUri = Uri.parse("geo:${log.punchInLat},${log.punchInLng}?q=${log.punchInLat},${log.punchInLng}(Site+Punch)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                try {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=${log.punchInLat},${log.punchInLng}"))
                                    context.startActivity(webIntent)
                                } catch (e2: Exception) {
                                    Toast.makeText(context, "Cannot open map: ${e2.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen.copy(alpha = 0.15f), contentColor = ShahDarkGreen),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View on Map", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
