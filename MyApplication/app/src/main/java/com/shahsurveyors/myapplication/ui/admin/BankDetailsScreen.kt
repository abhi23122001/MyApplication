package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.theme.DeepMidnightSlate
import com.shahsurveyors.myapplication.ui.theme.ElectricGold
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// I need to add bankDetails to AdminViewModel as well
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val bankDetails by viewModel.bankDetails.collectAsState()
    
    var bankName by remember { mutableStateOf("") }
    var accNo by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }

    LaunchedEffect(bankDetails) {
        bankDetails?.let {
            bankName = it.bankName
            accNo = it.accountNumber
            ifsc = it.ifscCode
            gstin = it.gstin
            branch = it.branchAddress
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnightSlate,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DeepMidnightSlate)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Account Information", color = ElectricGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsTextField(value = bankName, onValueChange = { bankName = it }, label = "Bank Name")
                SettingsTextField(value = accNo, onValueChange = { accNo = it }, label = "Account Number")
                SettingsTextField(value = ifsc, onValueChange = { ifsc = it }, label = "IFSC Code")
                SettingsTextField(value = gstin, onValueChange = { gstin = it }, label = "GSTIN")
                SettingsTextField(value = branch, onValueChange = { branch = it }, label = "Branch Address")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val updated = BankDetails(
                        bankName = bankName,
                        accountNumber = accNo,
                        ifscCode = ifsc,
                        gstin = gstin,
                        branchAddress = branch
                    )
                    viewModel.updateBankDetails(updated)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGold, contentColor = Color.Black)
            ) {
                Text("SAVE BANK DETAILS", fontWeight = FontWeight.Bold)
            }
        }
    }
}
