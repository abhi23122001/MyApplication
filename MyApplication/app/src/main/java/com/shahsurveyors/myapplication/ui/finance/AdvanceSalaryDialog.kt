package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shahsurveyors.myapplication.models.AdvanceSalaryRequest
import com.shahsurveyors.myapplication.ui.theme.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun RequestAdvanceSalaryDialog(
    currentYearMonth: String,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, installments: Int, month: String, reason: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var installmentsText by remember { mutableStateOf("3") }
    var startingMonth by remember { mutableStateOf(currentYearMonth) }
    var reasonText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val installmentsValue = installmentsText.toIntOrNull() ?: 1
    val monthlyEmi = if (installmentsValue > 0 && amountValue > 0) {
        amountValue / installmentsValue
    } else {
        0.0
    }

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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = ShahGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Request Advance Salary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShahBlack
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ShahMediumGrey)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Required Advance Amount (₹)") },
                    placeholder = { Text("e.g. 10000") },
                    leadingIcon = { Text(" ₹ ", fontWeight = FontWeight.Bold, color = ShahGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = installmentsText,
                        onValueChange = { installmentsText = it },
                        label = { Text("Installments (Months)") },
                        placeholder = { Text("e.g. 3") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = startingMonth,
                        onValueChange = { startingMonth = it },
                        label = { Text("Starting Month") },
                        placeholder = { Text("YYYY-MM") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // EMI Preview
                if (amountValue > 0 && installmentsValue > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ShahGreen.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated Monthly EMI:",
                                fontSize = 12.sp,
                                color = ShahDarkGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹ ${String.format(java.util.Locale.ENGLISH, "%,.0f", monthlyEmi)}/mo",
                                fontSize = 14.sp,
                                color = ShahGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason for Advance") },
                    placeholder = { Text("e.g. Medical emergency, urgent personal need") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = validationError ?: "", color = ErrorRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (amountValue <= 0) {
                            validationError = "Please enter a valid advance amount."
                            return@Button
                        }
                        if (installmentsValue <= 0) {
                            validationError = "Installments must be at least 1 month."
                            return@Button
                        }
                        if (reasonText.isBlank()) {
                            validationError = "Please specify a reason."
                            return@Button
                        }
                        onSubmit(
                            amountValue,
                            installmentsValue,
                            startingMonth.trim(),
                            reasonText.trim()
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SUBMIT REQUEST", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminAdvanceDecisionDialog(
    request: AdvanceSalaryRequest,
    onDismiss: () -> Unit,
    onDecide: (status: String, approvedAmount: Double, installments: Int, note: String) -> Unit
) {
    var approvedAmountText by remember { mutableStateOf(request.requestedAmount.toInt().toString()) }
    var installmentsText by remember { mutableStateOf(request.installments.toString()) }
    var adminNoteText by remember { mutableStateOf("") }

    val approvedAmount = approvedAmountText.toDoubleOrNull() ?: request.requestedAmount
    val installments = installmentsText.toIntOrNull() ?: request.installments
    val monthlyEmi = if (installments > 0) approvedAmount / installments else 0.0

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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Review Advance Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShahBlack
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ShahMediumGrey)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Request summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ShahGrey),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = request.employeeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ShahBlack
                        )
                        Text(
                            text = "Requested: ₹ ${request.requestedAmount.toInt()} (${request.installments} installments)",
                            fontSize = 12.sp,
                            color = ShahGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Reason: ${request.reason}",
                            fontSize = 12.sp,
                            color = ShahDarkGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = approvedAmountText,
                    onValueChange = { approvedAmountText = it },
                    label = { Text("Approved Amount (₹)") },
                    leadingIcon = { Text(" ₹ ", fontWeight = FontWeight.Bold, color = ShahGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = installmentsText,
                    onValueChange = { installmentsText = it },
                    label = { Text("Approved Installments (Months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deduction Schedule: ₹ ${monthlyEmi.toInt()}/month starting ${request.requestedMonth}",
                    fontSize = 11.sp,
                    color = ShahMediumGrey
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminNoteText,
                    onValueChange = { adminNoteText = it },
                    label = { Text("Admin Remarks / Note") },
                    placeholder = { Text("e.g. Approved as per policy") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDecide("REJECTED", 0.0, 1, adminNoteText.trim())
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("REJECT", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onDecide("APPROVED", approvedAmount, installments, adminNoteText.trim())
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("APPROVE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
