package com.shahsurveyors.myapplication.ui.ops

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
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

    var category by remember {
        mutableStateOf("Fuel")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var remarks by remember {
        mutableStateOf("")
    }

    var capturedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var validationError by remember {
        mutableStateOf<String?>(null)
    }


    // ============================================================
    // CAMERA
    // ============================================================

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (bitmap != null) {
                capturedBitmap = bitmap
                validationError = null
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

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription =
                                "Back",

                            tint =
                                ShahWhite
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            ShahDarkGreen
                    )
            )
        }

    ) { paddingValues ->

        Column(

            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(ShahGrey)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(16.dp)
        ) {

            // ========================================================
            // CLAIM CARD
            // ========================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(12.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            ShahWhite
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
            ) {

                Column(

                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Text(

                        text =
                            "Submit New Claim",

                        color =
                            ShahGreen,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            18.sp
                    )


                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )


                    // ====================================================
                    // AMOUNT
                    // ====================================================

                    OutlinedTextField(

                        value =
                            amount,

                        onValueChange = { value ->

                            if (
                                value.isEmpty() ||
                                value.all {
                                    it.isDigit() ||
                                            it == '.'
                                }
                            ) {
                                amount = value
                                validationError = null
                            }
                        },

                        label = {
                            Text("Amount (INR)")
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        singleLine =
                            true,

                        prefix = {
                            Text("₹ ")
                        },

                        shape =
                            RoundedCornerShape(12.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )


                    // ====================================================
                    // CATEGORY
                    // ====================================================

                    Text(

                        text =
                            "Category",

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        color =
                            ShahMediumGrey,

                        fontWeight =
                            FontWeight.Bold
                    )


                    val categories =
                        listOf(
                            "Fuel",
                            "Food/DA",
                            "Rent",
                            "Travel",
                            "Other"
                        )


                    val selectedIndex =
                        categories.indexOf(category)
                            .coerceAtLeast(0)


                    ScrollableTabRow(

                        selectedTabIndex =
                            selectedIndex,

                        edgePadding =
                            0.dp,

                        containerColor =
                            ShahWhite,

                        contentColor =
                            ShahGreen,

                        indicator = { tabPositions ->

                            if (
                                selectedIndex <
                                tabPositions.size
                            ) {

                                TabRowDefaults
                                    .SecondaryIndicator(

                                        Modifier
                                            .tabIndicatorOffset(
                                                tabPositions[
                                                    selectedIndex
                                                ]
                                            ),

                                        color =
                                            ShahGreen
                                    )
                            }
                        }
                    ) {

                        categories.forEach { cat ->

                            Tab(

                                selected =
                                    category == cat,

                                onClick = {

                                    category =
                                        cat

                                    validationError =
                                        null
                                },

                                text = {

                                    Text(
                                        text =
                                            cat,

                                        fontSize =
                                            10.sp
                                    )
                                }
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )


                    // ====================================================
                    // REMARKS
                    // ====================================================

                    OutlinedTextField(

                        value =
                            remarks,

                        onValueChange = {

                            remarks = it
                            validationError = null
                        },

                        label = {
                            Text("Remarks / Notes")
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        minLines =
                            3,

                        maxLines =
                            6,

                        shape =
                            RoundedCornerShape(12.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )


                    // ====================================================
                    // RECEIPT CAPTURE
                    // ====================================================

                    Button(

                        onClick = {

                            cameraLauncher.launch(
                                null
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(8.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    ShahLightGrey,

                                contentColor =
                                    ShahBlack
                            )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.CameraAlt,

                            contentDescription =
                                "Capture Receipt"
                        )


                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )


                        Text(

                            text =
                                if (
                                    capturedBitmap == null
                                ) {
                                    "CAPTURE RECEIPT"
                                } else {
                                    "RE-CAPTURE RECEIPT"
                                },

                            fontWeight =
                                FontWeight.Bold
                        )
                    }


                    if (
                        capturedBitmap != null
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )


                        Text(

                            text =
                                "Receipt captured successfully ✓",

                            color =
                                SuccessGreen,

                            fontSize =
                                11.sp
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )


                    // ====================================================
                    // VALIDATION
                    // ====================================================

                    validationError?.let { error ->

                        Text(

                            text =
                                error,

                            color =
                                ErrorRed,

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Bold,

                            modifier =
                                Modifier.padding(
                                    bottom = 8.dp
                                )
                        )
                    }


                    // ====================================================
                    // SUBMIT
                    // ====================================================

                    Button(

                        onClick = {

                            when {

                                amount.isBlank() -> {

                                    validationError =
                                        "Please enter expense amount."
                                }

                                amount.toDoubleOrNull() == null ||
                                        amount.toDouble() <= 0 -> {

                                    validationError =
                                        "Please enter a valid amount."
                                }

                                remarks.isBlank() -> {

                                    validationError =
                                        "Please enter remarks."
                                }

                                capturedBitmap == null -> {

                                    validationError =
                                        "Please capture the receipt."
                                }

                                else -> {

                                    validationError =
                                        null

                                    viewModel.submitClaim(

                                        amount =
                                            amount,

                                        category =
                                            category,

                                        remarks =
                                            remarks,

                                        receiptBitmap =
                                            capturedBitmap
                                    )
                                }
                            }
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled =
                            !viewModel.isLoading,

                        shape =
                            RoundedCornerShape(8.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    ShahGreen,

                                contentColor =
                                    ShahWhite,

                                disabledContainerColor =
                                    ShahGreen.copy(
                                        alpha = 0.5f
                                    )
                            )
                    ) {

                        Text(

                            text =
                                if (
                                    viewModel.isLoading
                                ) {
                                    "SUBMITTING..."
                                } else {
                                    "SUBMIT FOR APPROVAL"
                                },

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )


            Text(

                text =
                    "Receipt and expense details will be securely stored in Firebase. The claim will remain pending until Admin approval.",

                color =
                    ShahMediumGrey,

                fontSize =
                    11.sp
            )
        }


        // ============================================================
        // GLOBAL LOADER
        // ============================================================

        GlobalAsyncLoader(

            isLoading =
                viewModel.isLoading,

            statusText =
                viewModel.statusMessage
                    ?: "Submitting expense..."
        )
    }
}