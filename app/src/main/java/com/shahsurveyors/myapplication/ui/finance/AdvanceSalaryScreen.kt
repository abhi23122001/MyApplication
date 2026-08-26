package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.data.AdvanceSalaryRepository
import com.shahsurveyors.myapplication.models.AdvanceSalaryRequest
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvanceSalaryScreen(
    repository: AdvanceSalaryRepository,
    uid: String,
    userName: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var requests by remember { mutableStateOf<List<AdvanceSalaryRequest>>(emptyList()) }
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var salaryMonth by remember { mutableStateOf("") }
    var installments by remember { mutableStateOf("1") }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            requests = repository.getForUser(uid)
            loading = false
        }
    }
    LaunchedEffect(uid) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Advance Salary", color = ShahWhite, fontWeight = FontWeight.Bold); Text("Request and track salary advance", color = ShahWhite.copy(.72f), fontSize = 10.sp) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().background(ShahGrey),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ShahWhite)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Request Advance Salary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShahBlack)
                        OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(salaryMonth, { salaryMonth = it }, label = { Text("Salary Month") }, placeholder = { Text("2026-09") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(installments, { installments = it.filter(Char::isDigit) }, label = { Text("Deduction Installments") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(reason, { reason = it }, label = { Text("Reason") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                val value = amount.toDoubleOrNull() ?: 0.0
                                val count = installments.toIntOrNull() ?: 1
                                when {
                                    value <= 0.0 -> message = "Enter a valid advance amount."
                                    salaryMonth.trim().isBlank() -> message = "Salary month is required."
                                    count < 1 -> message = "Installments must be at least 1."
                                    reason.trim().isBlank() -> message = "Reason is required."
                                    else -> scope.launch {
                                        try {
                                            repository.createRequest(AdvanceSalaryRequest(uid = uid, userName = userName, amount = value, reason = reason.trim(), salaryMonth = salaryMonth.trim(), installments = count))
                                            amount = ""; reason = ""; salaryMonth = ""; installments = "1"
                                            message = "Request submitted for Admin approval."
                                            load()
                                        } catch (e: Exception) { message = e.localizedMessage ?: "Unable to submit request." }
                                    }
                                }
                            },
                            Modifier.fillMaxWidth()
                        ) { Icon(Icons.Default.Send, null); Spacer(Modifier.width(7.dp)); Text("Submit for Admin Approval") }
                        message?.let { Text(it, color = if (it.startsWith("Request")) ShahGreen else MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            item { Text("My Advance Requests", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ShahBlack) }
            if (loading) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ShahGreen) } }
            else if (requests.isEmpty()) item { Text("No advance salary requests yet.", color = ShahMediumGrey, modifier = Modifier.padding(12.dp)) }
            else items(requests, key = { it.id }) { request -> AdvanceRequestCard(request) }
        }
    }
}

@Composable
private fun AdvanceRequestCard(request: AdvanceSalaryRequest) {
    val statusColor = when (request.status) { "APPROVED" -> SuccessGreen; "REJECTED" -> ErrorRed; else -> WarningAmber }
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, null, tint = ShahGreen)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) { Text("₹${String.format(Locale.ENGLISH, "%,.0f", request.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp); Text("Salary month: ${request.salaryMonth}", fontSize = 10.sp, color = ShahMediumGrey) }
                Text(request.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Text("Installments: ${request.installments}", fontSize = 11.sp, color = ShahBlack)
            Text(request.reason, fontSize = 11.sp, color = ShahMediumGrey)
            if (request.status == "APPROVED") Text("Approved amount: ₹${String.format(Locale.ENGLISH, "%,.0f", request.approvedAmount)}", color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            if (request.adminName.isNotBlank()) Text("Decision by: ${request.adminName}", fontSize = 10.sp, color = ShahMediumGrey)
        }
    }
}
