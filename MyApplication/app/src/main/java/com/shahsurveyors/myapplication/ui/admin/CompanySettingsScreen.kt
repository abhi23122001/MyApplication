package com.shahsurveyors.myapplication.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.theme.DeepMidnightSlate
import com.shahsurveyors.myapplication.ui.theme.ElectricGold
import com.shahsurveyors.myapplication.utils.FileStorageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySettingsScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val profile by viewModel.companyProfile.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var footerText by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<String?>(null) }
    var sealUri by remember { mutableStateOf<String?>(null) }
    var signatureUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            address = it.address
            email = it.email
            phone = it.phone
            footerText = it.footerText
            logoUri = it.logoUri
            sealUri = it.sealUri
            signatureUri = it.signatureUri
        }
    }

    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = FileStorageHelper.saveImageToInternalStorage(context, it, "company_logo.png")
            logoUri = path
        }
    }

    val sealLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = FileStorageHelper.saveImageToInternalStorage(context, it, "company_seal.png")
            sealUri = path
        }
    }

    val signLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = FileStorageHelper.saveImageToInternalStorage(context, it, "company_signature.png")
            signatureUri = path
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Company Profile", fontWeight = FontWeight.Bold) },
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
                Text("General Information", color = ElectricGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsTextField(value = name, onValueChange = { name = it }, label = "Company Name")
                SettingsTextField(value = address, onValueChange = { address = it }, label = "Address")
                SettingsTextField(value = email, onValueChange = { email = it }, label = "Email")
                SettingsTextField(value = phone, onValueChange = { phone = it }, label = "Phone")
                SettingsTextField(value = footerText, onValueChange = { footerText = it }, label = "Footer Text")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ImageUploadCard("Company Logo", logoUri) { logoLauncher.launch("image/*") }
            Spacer(modifier = Modifier.height(12.dp))
            ImageUploadCard("Company Seal / Stamp", sealUri) { sealLauncher.launch("image/*") }
            Spacer(modifier = Modifier.height(12.dp))
            ImageUploadCard("Authorized Signature", signatureUri) { signLauncher.launch("image/*") }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val updatedProfile = CompanyProfile(
                        name = name,
                        address = address,
                        email = email,
                        phone = phone,
                        footerText = footerText,
                        logoUri = logoUri,
                        sealUri = sealUri,
                        signatureUri = signatureUri
                    )
                    viewModel.updateCompanyProfile(updatedProfile)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGold, contentColor = Color.Black)
            ) {
                Text("SAVE PROFILE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = ElectricGold,
            unfocusedBorderColor = Color.Gray
        )
    )
}

@Composable
fun ImageUploadCard(label: String, uri: String?, onUpload: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold)
                Text(if (uri != null) "Image uploaded" else "No image selected", color = Color.Gray, fontSize = 12.sp)
            }
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).padding(4.dp)
                )
            }
            IconButton(onClick = onUpload) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ElectricGold)
            }
        }
    }
}
