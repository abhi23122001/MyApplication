package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val bankDetails by viewModel.bankDetails.collectAsState()

    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var branchAddress by remember { mutableStateOf("") }

    LaunchedEffect(bankDetails) {
        bankDetails?.let { details ->
            bankName = details.bankName
            accountNumber = details.accountNumber
            ifscCode = details.ifscCode
            gstin = details.gstin
            branchAddress = details.branchAddress
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bank Details",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        },
        containerColor = ShahGrey
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShahGrey)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ShahWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Account Information",
                        color = ShahGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = "Bank Name"
                    )

                    SettingsTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = "Account Number"
                    )

                    SettingsTextField(
                        value = ifscCode,
                        onValueChange = { ifscCode = it },
                        label = "IFSC Code"
                    )

                    SettingsTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = "GSTIN"
                    )

                    SettingsTextField(
                        value = branchAddress,
                        onValueChange = { branchAddress = it },
                        label = "Branch Address"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                    val updatedBankDetails = BankDetails(
                        id = 1,
                        bankName = bankName.trim(),
                        accountNumber = accountNumber.trim(),
                        ifscCode = ifscCode.trim().uppercase(),
                        gstin = gstin.trim().uppercase(),
                        branchAddress = branchAddress.trim()
                    )

                    viewModel.updateBankDetails(updatedBankDetails)

                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShahGreen,
                    contentColor = ShahWhite
                )
            ) {

                Text(
                    text = "SAVE BANK DETAILS",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}