package com.shahsurveyors.myapplication.ui.ops

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.shahsurveyors.myapplication.models.ExpenseRecord
import com.shahsurveyors.myapplication.ui.components.GlobalAsyncLoader
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseClaimsScreen(
    viewModel: ExpenseViewModel,
    uid: String,
    userName: String,
    onBack: () -> Unit = {}
) {
    var category by remember { mutableStateOf("Fuel") }
    var amount by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) { capturedBitmap = bitmap; validationError = null }
    }

    LaunchedEffect(uid) { viewModel.loadMyExpenses(uid) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Expense Claims", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Submit • capture • track approval", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().background(ShahGrey),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = ShahDarkGreen) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = ShahWhite.copy(.12f)) { Icon(Icons.Default.ReceiptLong, null, tint = ShahWhite, modifier = Modifier.padding(10.dp).size(24.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column { Text("New Expense Claim", color = ShahWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("Keep amount, category and receipt together", fontSize = 10.sp, color = ShahWhite.copy(.72f)) }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(17.dp)) {
                        Text("Claim Details", color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("All required information is reviewed by Admin", fontSize = 10.sp, color = ShahMediumGrey)
                        Spacer(Modifier.height(13.dp))
                        OutlinedTextField(amount, { v -> if (v.isEmpty() || v.all { it.isDigit() || it == '.' }) { amount = v; validationError = null } }, label = { Text("Amount (INR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text("₹ ") }, shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Payments, null) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShahGreen))
                        Spacer(Modifier.height(14.dp))
                        Text("Expense Category", fontSize = 11.sp, color = ShahMediumGrey, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        val categories = listOf("Fuel", "Food/DA", "Rent", "Travel", "Other")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { categories.forEach { cat -> FilterChip(selected = category == cat, onClick = { category = cat; validationError = null }, label = { Text(cat, fontSize = 10.sp) }, leadingIcon = if (category == cat) ({ Icon(Icons.Default.Check, null, Modifier.size(15.dp)) }) else null) } }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(remarks, { remarks = it; validationError = null }, label = { Text("Remarks / Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShahGreen))
                        Spacer(Modifier.height(15.dp))
                        Text("Receipt Verification", fontSize = 11.sp, color = ShahMediumGrey, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(7.dp))
                        OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = ShahGreen)) { Icon(if (capturedBitmap == null) Icons.Default.CameraAlt else Icons.Default.CheckCircle, null); Spacer(Modifier.width(7.dp)); Text(if (capturedBitmap == null) "CAPTURE RECEIPT" else "RE-CAPTURE RECEIPT", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        capturedBitmap?.let { Spacer(Modifier.height(7.dp)); Surface(Modifier.fillMaxWidth(), RoundedCornerShape(10.dp), color = ShahGreen.copy(.06f)) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Verified, null, tint = SuccessGreen, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)); Text("Receipt captured successfully", fontSize = 11.sp, color = ShahDarkGreen, fontWeight = FontWeight.Bold) } } }
                        validationError?.let { Spacer(Modifier.height(10.dp)); Surface(Modifier.fillMaxWidth(), RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer) { Text(it, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp)) } }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                when {
                                    amount.isBlank() -> validationError = "Please enter expense amount."
                                    amount.toDoubleOrNull() == null || amount.toDouble() <= 0 -> validationError = "Please enter a valid amount."
                                    remarks.isBlank() -> validationError = "Please enter remarks."
                                    capturedBitmap == null -> validationError = "Please capture the receipt."
                                    else -> { validationError = null; viewModel.submitClaim(uid, userName, amount, category, remarks, capturedBitmap) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !viewModel.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, disabledContainerColor = ShahGreen.copy(.5f))
                        ) { if (viewModel.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = ShahWhite, strokeWidth = 2.dp) else { Icon(Icons.Default.Send, null); Spacer(Modifier.width(7.dp)); Text("SUBMIT FOR APPROVAL", fontWeight = FontWeight.ExtraBold) } }
                    }
                }
            }
            item {
                Text("My Expense History", color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (viewModel.myExpenses.isEmpty()) {
                item { Surface(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), color = ShahWhite) { Text("No expense claims submitted yet.", Modifier.padding(16.dp), color = ShahMediumGrey, fontSize = 11.sp) } }
            } else {
                items(viewModel.myExpenses, key = { it.id }) { expense -> ExpenseHistoryCard(expense) }
            }
            item {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), color = ShahWhite) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Security, null, tint = ShahGreen, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)); Text("Receipt and claim details are securely stored and remain pending until Admin approval.", fontSize = 10.sp, color = ShahMediumGrey) }
                }
            }
        }
    }
    GlobalAsyncLoader(isLoading = viewModel.isLoading, statusText = viewModel.statusMessage ?: "Submitting expense...")
}

@Composable
private fun ExpenseHistoryCard(expense: ExpenseRecord) {
    val statusColor = when (expense.status) { "APPROVED" -> SuccessGreen; "REJECTED" -> ErrorRed; else -> WarningAmber }
    val dateText = expense.date?.let { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(it.toDate()) } ?: "Date unavailable"
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(expense.category.ifBlank { "Expense" }, fontWeight = FontWeight.Bold, color = ShahBlack); Text(dateText, fontSize = 9.sp, color = ShahMediumGrey) }
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = .12f)) { Text(expense.status, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor) }
            }
            Spacer(Modifier.height(8.dp))
            Text("₹ ${"%.2f".format(Locale.US, expense.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            if (expense.projectName.isNotBlank()) Text("Project: ${expense.projectName}", fontSize = 10.sp, color = ShahMediumGrey)
            if (expense.description.isNotBlank()) Text(expense.description, fontSize = 10.sp, color = ShahMediumGrey, modifier = Modifier.padding(top = 4.dp))
            if (expense.status == "APPROVED") Text("Payment: ${expense.paymentStatus}", fontSize = 10.sp, color = if (expense.paymentStatus == "PAID") SuccessGreen else WarningAmber, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
            if (expense.adminRemark.isNotBlank()) Text("Admin: ${expense.adminRemark}", fontSize = 10.sp, color = ShahMediumGrey, modifier = Modifier.padding(top = 5.dp))
        }
    }
}
