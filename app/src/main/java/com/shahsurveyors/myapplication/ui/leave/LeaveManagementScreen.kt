package com.shahsurveyors.myapplication.ui.leave

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.shahsurveyors.myapplication.data.LeaveRepository
import com.shahsurveyors.myapplication.models.LeaveRequestModel
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveManagementScreen(
    repository: LeaveRepository,
    uid: String,
    userName: String,
    userRole: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isAdmin = userRole.equals("admin", ignoreCase = true)
    val snackbarHostState = remember { SnackbarHostState() }
    var requests by remember { mutableStateOf<List<LeaveRequestModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var leaveType by remember { mutableStateOf("CASUAL") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    fun loadRequests() {
        scope.launch {
            isLoading = true
            requests = if (isAdmin) repository.getAllRequests() else repository.getRequestsForUser(uid)
            isLoading = false
        }
    }

    fun showMessage(text: String) {
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    val selectedDays = remember(fromDate, toDate) {
        runCatching {
            val from = LocalDate.parse(fromDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            val to = LocalDate.parse(toDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            if (to.isBefore(from)) 0 else ChronoUnit.DAYS.between(from, to).toInt() + 1
        }.getOrDefault(0)
    }

    LaunchedEffect(uid, isAdmin) { loadRequests() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Leave Management", color = ShahWhite, fontWeight = FontWeight.Bold)
                        Text(
                            if (isAdmin) "Review and approve team leave" else "Apply and track your leave",
                            color = ShahWhite.copy(alpha = .72f),
                            fontSize = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back", color = ShahWhite) }
                },
                actions = {
                    IconButton(onClick = { loadRequests() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(ShahGrey).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isAdmin) {
                item {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Apply for Leave", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShahBlack)

                            ExposedDropdownMenuBox(
                                expanded = typeExpanded,
                                onExpandedChange = { typeExpanded = !typeExpanded }
                            ) {
                                OutlinedTextField(
                                    value = leaveType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Leave Type") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = typeExpanded,
                                    onDismissRequest = { typeExpanded = false }
                                ) {
                                    listOf("CASUAL", "SICK", "PAID", "UNPAID", "EMERGENCY").forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type) },
                                            onClick = { leaveType = type; typeExpanded = false }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = fromDate,
                                onValueChange = { fromDate = it },
                                label = { Text("From Date") },
                                placeholder = { Text("yyyy-MM-dd") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = toDate,
                                onValueChange = { toDate = it },
                                label = { Text("To Date") },
                                placeholder = { Text("yyyy-MM-dd") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (selectedDays > 0) {
                                Text(
                                    "Total leave: $selectedDays day${if (selectedDays == 1) "" else "s"}",
                                    color = ShahGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                label = { Text("Reason") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (fromDate.isBlank() || toDate.isBlank() || reason.isBlank()) {
                                        showMessage("From date, To date and reason are required.")
                                        return@Button
                                    }
                                    if (selectedDays <= 0) {
                                        showMessage("Please enter valid dates in yyyy-MM-dd format.")
                                        return@Button
                                    }
                                    scope.launch {
                                        try {
                                            repository.submitRequest(
                                                LeaveRequestModel(
                                                    userUid = uid,
                                                    employeeName = userName,
                                                    leaveType = leaveType,
                                                    fromDate = fromDate.trim(),
                                                    toDate = toDate.trim(),
                                                    reason = reason.trim(),
                                                    status = "PENDING",
                                                    createdAt = Timestamp.now()
                                                )
                                            )
                                            fromDate = ""
                                            toDate = ""
                                            reason = ""
                                            showMessage("Leave request submitted for Admin approval.")
                                            loadRequests()
                                        } catch (e: Exception) {
                                            showMessage(e.message ?: "Unable to submit leave request.")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Send, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Submit Leave Request")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    if (isAdmin) "All Leave Requests" else "My Leave Requests",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShahBlack
                )
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (requests.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = ShahGreen)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (isAdmin) "No leave requests yet." else "You have no leave requests yet.",
                                color = ShahMediumGrey
                            )
                        }
                    }
                }
            } else {
                items(requests, key = { it.id }) { request ->
                    LeaveRequestCard(
                        request = request,
                        isAdmin = isAdmin,
                        onStatusChange = { id, status, remark ->
                            scope.launch {
                                try {
                                    repository.updateStatus(id, status, userName, remark)
                                    showMessage("Leave request marked $status.")
                                    loadRequests()
                                } catch (e: Exception) {
                                    showMessage(e.message ?: "Unable to update request.")
                                }
                            }
                        },
                        onCancel = { id ->
                            scope.launch {
                                try {
                                    repository.updateStatus(id, "CANCELLED", userName, "Cancelled by employee")
                                    showMessage("Leave request cancelled.")
                                    loadRequests()
                                } catch (e: Exception) {
                                    showMessage(e.message ?: "Unable to cancel request.")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaveRequestCard(
    request: LeaveRequestModel,
    isAdmin: Boolean,
    onStatusChange: (String, String, String) -> Unit,
    onCancel: (String) -> Unit
) {
    val statusColor = when (request.status) {
        "APPROVED" -> Color(0xFF2E7D32)
        "REJECTED" -> Color(0xFFC62828)
        "CANCELLED" -> Color(0xFF616161)
        else -> Color(0xFFF57C00)
    }
    var adminRemark by remember(request.id) { mutableStateOf(request.adminRemark) }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EventBusy, null, tint = ShahGreen)
                Spacer(Modifier.width(8.dp))
                Text(
                    request.employeeName.ifBlank { "Employee" },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(request.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Text(
                "${request.leaveType} • ${request.fromDate} → ${request.toDate}",
                fontSize = 13.sp,
                color = ShahMediumGrey
            )
            Text(request.reason, fontSize = 13.sp, color = ShahBlack)

            if (request.adminRemark.isNotBlank()) {
                Text("Admin remark: ${request.adminRemark}", fontSize = 12.sp, color = ShahMediumGrey)
            }

            if (isAdmin && request.status == "PENDING") {
                OutlinedTextField(
                    value = adminRemark,
                    onValueChange = { adminRemark = it },
                    label = { Text("Admin remark (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onStatusChange(request.id, "REJECTED", adminRemark) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Reject")
                    }
                    Button(
                        onClick = { onStatusChange(request.id, "APPROVED", adminRemark) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Approve")
                    }
                }
            }

            if (!isAdmin && request.status == "PENDING") {
                OutlinedButton(
                    onClick = { onCancel(request.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cancel, null)
                    Spacer(Modifier.width(5.dp))
                    Text("Cancel Request")
                }
            }
        }
    }
}
