package com.shahsurveyors.myapplication.ui.ops

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shahsurveyors.myapplication.ui.components.GlobalAsyncLoader
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseClaimsScreen(
    viewModel: ExpenseViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    var category by remember { mutableStateOf("Fuel") }
    var amount by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            validationError = null
        }
    }

    LaunchedEffect(viewModel.statusMessage) {
        if (viewModel.statusMessage == "Expense submitted successfully") {
            amount = ""
            remarks = ""
            capturedBitmap = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Expense Claims",
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
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========================================================
            // 1. SUBMISSION FORM CARD
            // ========================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ShahWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Submit New Claim",
                            color = ShahGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // AMOUNT
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.all { it.isDigit() || it == '.' }) {
                                    amount = value
                                    validationError = null
                                }
                            },
                            label = { Text("Amount (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            prefix = { Text("₹ ") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // CATEGORY
                        Text(
                            text = "Expense Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = ShahMediumGrey,
                            fontWeight = FontWeight.Bold
                        )

                        val categories = listOf("Fuel", "Food/DA", "Rent", "Travel", "Other")
                        val selectedIndex = categories.indexOf(category).coerceAtLeast(0)

                        ScrollableTabRow(
                            selectedTabIndex = selectedIndex,
                            edgePadding = 0.dp,
                            containerColor = ShahWhite,
                            contentColor = ShahGreen,
                            indicator = { tabPositions ->
                                if (selectedIndex < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                        color = ShahGreen
                                    )
                                }
                            }
                        ) {
                            categories.forEach { cat ->
                                Tab(
                                    selected = category == cat,
                                    onClick = {
                                        category = cat
                                        validationError = null
                                    },
                                    text = { Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // REMARKS
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = {
                                remarks = it
                                validationError = null
                            },
                            label = { Text("Remarks / Notes") },
                            placeholder = { Text("e.g. Fuel for Site vehicle, Team lunch") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // RECEIPT CAPTURE
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (capturedBitmap != null) ShahGreen.copy(alpha = 0.15f) else ShahLightGrey,
                                contentColor = if (capturedBitmap != null) ShahGreen else ShahBlack
                            )
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Capture Receipt")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (capturedBitmap == null) "CAPTURE RECEIPT" else "RE-CAPTURE RECEIPT",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (capturedBitmap != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Receipt photo attached ✓",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        validationError?.let { error ->
                            Text(
                                text = error,
                                color = ErrorRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // SUBMIT BUTTON
                        Button(
                            onClick = {
                                when {
                                    amount.isBlank() -> validationError = "Please enter expense amount."
                                    amount.toDoubleOrNull() == null || amount.toDouble() <= 0 -> validationError = "Please enter a valid amount."
                                    remarks.isBlank() -> validationError = "Please enter remarks."
                                    else -> {
                                        validationError = null
                                        viewModel.submitClaim(
                                            amount = amount,
                                            category = category,
                                            remarks = remarks,
                                            receiptBitmap = capturedBitmap
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShahGreen,
                                contentColor = ShahWhite,
                                disabledContainerColor = ShahGreen.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = if (viewModel.isLoading) "SUBMITTING..." else "SUBMIT FOR APPROVAL",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ========================================================
            // 2. MY EXPENSE HISTORY HEADER
            // ========================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Expense History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ShahBlack
                    )
                    Text(
                        text = "${viewModel.expensesList.size} claims",
                        fontSize = 12.sp,
                        color = ShahMediumGrey
                    )
                }
            }

            // ========================================================
            // 3. EXPENSE LIST ITEMS
            // ========================================================
            if (viewModel.expensesList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ShahWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = ShahMediumGrey,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No expense claims submitted yet.",
                                color = ShahMediumGrey,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(viewModel.expensesList, key = { it.id }) { item ->
                    val statusColor = when (item.status) {
                        "APPROVED" -> SuccessGreen
                        "REJECTED" -> ErrorRed
                        else -> WarningAmber
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ShahWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = ShahGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            color = ShahDarkGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.dateStr,
                                        fontSize = 11.sp,
                                        color = ShahMediumGrey
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.remarks,
                                    fontSize = 13.sp,
                                    color = ShahDarkGrey
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹ ${item.amount.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ShahBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = statusColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.status,
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        GlobalAsyncLoader(
            isLoading = viewModel.isLoading,
            statusText = viewModel.statusMessage ?: "Submitting expense..."
        )
    }
}