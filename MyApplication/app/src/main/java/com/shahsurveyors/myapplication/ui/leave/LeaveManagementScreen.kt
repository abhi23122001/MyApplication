package com.shahsurveyors.myapplication.ui.leave

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shahsurveyors.myapplication.data.LeaveRepository
import com.shahsurveyors.myapplication.models.LeaveRequest
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveManagementScreen(
    currentUid: String = "",
    currentUserName: String = "Staff User",
    currentUserEmpId: String = "EMP001",
    currentUserDept: String = "SURVEY",
    isAdmin: Boolean = false,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val leaveRepository = remember { LeaveRepository() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val leaveList = remember { mutableStateListOf<LeaveRequest>() }

    var showApplyLeaveDialog by remember { mutableStateOf(false) }
    var selectedLeaveForApproval by remember { mutableStateOf<LeaveRequest?>(null) }

    fun loadLeaves() {
        coroutineScope.launch {
            isLoading = true
            try {
                leaveList.clear()
                val list = if (isAdmin) {
                    leaveRepository.getAllLeaves()
                } else {
                    leaveRepository.getLeavesForEmployee(currentUid)
                }
                leaveList.addAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentUid, isAdmin) {
        loadLeaves()
    }

    val pendingCount = leaveList.count { it.status == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAdmin) "Leave Approvals & Records" else "My Leave Applications",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = {
            if (!isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showApplyLeaveDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Apply Leave") },
                    containerColor = ShahGreen,
                    contentColor = ShahWhite
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
        ) {
            if (isAdmin) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ShahWhite,
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
                        text = {
                            Text(
                                text = if (pendingCount > 0) "Pending Requests ($pendingCount)" else "Pending Requests",
                                fontWeight = FontWeight.Bold,
                                color = if (pendingCount > 0) WarningAmber else ShahDarkGrey
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("All History (${leaveList.size})", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            val displayedLeaves = if (isAdmin) {
                if (selectedTab == 0) leaveList.filter { it.status == "PENDING" } else leaveList.toList()
            } else {
                leaveList.toList()
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (displayedLeaves.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isAdmin) "No leave requests in this section." else "No leave applications yet. Tap Apply Leave to submit.",
                        color = ShahMediumGrey
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedLeaves, key = { it.id }) { leave ->
                        val statusColor = when (leave.status) {
                            "APPROVED" -> SuccessGreen
                            "REJECTED" -> ErrorRed
                            else -> WarningAmber
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isAdmin && leave.status == "PENDING") {
                                        selectedLeaveForApproval = leave
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ShahWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${leave.leaveType} Leave (${leave.totalDays} Day${if (leave.totalDays > 1) "s" else ""})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ShahBlack
                                        )
                                        Text(
                                            text = if (isAdmin) "${leave.employeeName} (ID: ${leave.employeeId})" else "Applied: ${leave.startDate} to ${leave.endDate}",
                                            fontSize = 12.sp,
                                            color = ShahMediumGrey
                                        )
                                    }

                                    Surface(
                                        color = statusColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = leave.status,
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (isAdmin) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Duration: ${leave.startDate} to ${leave.endDate}",
                                        fontSize = 12.sp,
                                        color = ShahDarkGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Reason: ${leave.reason}", fontSize = 13.sp, color = ShahDarkGrey)

                                if (isAdmin && leave.status == "PENDING") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { selectedLeaveForApproval = leave },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("REVIEW & DECIDE", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showApplyLeaveDialog) {
        ApplyLeaveDialog(
            onDismiss = { showApplyLeaveDialog = false },
            onSubmit = { start, end, days, type, reason ->
                coroutineScope.launch {
                    val req = LeaveRequest(
                        employeeUid = currentUid,
                        employeeName = currentUserName,
                        employeeId = currentUserEmpId,
                        department = currentUserDept,
                        startDate = start,
                        endDate = end,
                        totalDays = days,
                        leaveType = type,
                        reason = reason,
                        status = "PENDING"
                    )
                    val success = leaveRepository.applyLeave(req)
                    if (success) {
                        Toast.makeText(context, "Leave application submitted", Toast.LENGTH_SHORT).show()
                        showApplyLeaveDialog = false
                        loadLeaves()
                    } else {
                        Toast.makeText(context, "Failed to submit leave", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    selectedLeaveForApproval?.let { leave ->
        var adminNote by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedLeaveForApproval = null },
            title = { Text("Review Leave Request", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${leave.employeeName} (${leave.department})", fontWeight = FontWeight.Bold, color = ShahBlack)
                    Text("Duration: ${leave.startDate} to ${leave.endDate} (${leave.totalDays} days)")
                    Text("Type: ${leave.leaveType}")
                    Text("Reason: ${leave.reason}", color = ShahDarkGrey)
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Admin Remarks (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            leaveRepository.decideLeave(
                                leaveId = leave.id,
                                status = "APPROVED",
                                adminUid = currentUid,
                                adminName = currentUserName,
                                note = adminNote.trim()
                            )
                            Toast.makeText(context, "Leave Approved", Toast.LENGTH_SHORT).show()
                            selectedLeaveForApproval = null
                            loadLeaves()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite)
                ) {
                    Text("APPROVE")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            leaveRepository.decideLeave(
                                leaveId = leave.id,
                                status = "REJECTED",
                                adminUid = currentUid,
                                adminName = currentUserName,
                                note = adminNote.trim()
                            )
                            Toast.makeText(context, "Leave Rejected", Toast.LENGTH_SHORT).show()
                            selectedLeaveForApproval = null
                            loadLeaves()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Text("REJECT")
                }
            }
        )
    }
}

@Composable
fun ApplyLeaveDialog(
    onDismiss: () -> Unit,
    onSubmit: (startDate: String, endDate: String, totalDays: Int, type: String, reason: String) -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    val today = remember { sdf.format(Date()) }
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today) }
    var leaveType by remember { mutableStateOf("CASUAL") }
    var reason by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = ShahWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Apply For Leave", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShahBlack)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Leave Type:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShahDarkGreen)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASUAL", "SICK", "PAID", "UNPAID").forEach { t ->
                        FilterChip(
                            selected = leaveType == t,
                            onClick = { leaveType = t },
                            label = { Text(t, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Leave *") },
                    placeholder = { Text("e.g. Family function, medical rest") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(validationError ?: "", color = ErrorRed, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (reason.isBlank()) {
                            validationError = "Please enter reason."
                            return@Button
                        }
                        val days = try {
                            val d1 = sdf.parse(startDate.trim())?.time ?: System.currentTimeMillis()
                            val d2 = sdf.parse(endDate.trim())?.time ?: System.currentTimeMillis()
                            val diffDays = ((d2 - d1) / (1000 * 60 * 60 * 24)).toInt()
                            maxOf(1, diffDays + 1)
                        } catch (e: Exception) {
                            1
                        }

                        onSubmit(startDate.trim(), endDate.trim(), days, leaveType, reason.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SUBMIT LEAVE APPLICATION", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
