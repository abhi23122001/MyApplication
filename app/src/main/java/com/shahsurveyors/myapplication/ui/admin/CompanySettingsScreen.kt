package com.shahsurveyors.myapplication.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite
import com.shahsurveyors.myapplication.utils.FileStorageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySettingsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
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

    /*
     * Load existing company profile
     */
    LaunchedEffect(profile) {
        profile?.let { company ->

            name = company.name
            address = company.address
            email = company.email
            phone = company.phone
            footerText = company.footerText

            logoUri = company.logoUri
            sealUri = company.sealUri
            signatureUri = company.signatureUri
        }
    }

    /*
     * Company Logo Picker
     */
    val logoLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let { selectedUri ->

                try {
                    val savedPath =
                        FileStorageHelper.saveImageToInternalStorage(
                            context,
                            selectedUri,
                            "company_logo.png"
                        )

                    logoUri = savedPath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    /*
     * Company Seal Picker
     */
    val sealLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let { selectedUri ->

                try {
                    val savedPath =
                        FileStorageHelper.saveImageToInternalStorage(
                            context,
                            selectedUri,
                            "company_seal.png"
                        )

                    sealUri = savedPath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    /*
     * Authorized Signature Picker
     */
    val signatureLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let { selectedUri ->

                try {
                    val savedPath =
                        FileStorageHelper.saveImageToInternalStorage(
                            context,
                            selectedUri,
                            "company_signature.png"
                        )

                    signatureUri = savedPath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    Scaffold(
        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Company Profile",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

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

            /*
             * GENERAL INFORMATION
             */

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
                        text = "General Information",
                        color = ShahGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    SettingsTextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = "Company Name"
                    )

                    SettingsTextField(
                        value = address,
                        onValueChange = {
                            address = it
                        },
                        label = "Address"
                    )

                    SettingsTextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = "Email"
                    )

                    SettingsTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                        },
                        label = "Phone"
                    )

                    SettingsTextField(
                        value = footerText,
                        onValueChange = {
                            footerText = it
                        },
                        label = "Footer Text"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            /*
             * COMPANY LOGO
             */

            ImageUploadCard(
                label = "Company Logo",
                uri = logoUri,
                onUpload = {
                    logoLauncher.launch("image/*")
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /*
             * COMPANY SEAL
             */

            ImageUploadCard(
                label = "Company Seal / Stamp",
                uri = sealUri,
                onUpload = {
                    sealLauncher.launch("image/*")
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /*
             * AUTHORIZED SIGNATURE
             */

            ImageUploadCard(
                label = "Authorized Signature",
                uri = signatureUri,
                onUpload = {
                    signatureLauncher.launch("image/*")
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            /*
             * SAVE BUTTON
             */

            Button(

                onClick = {

                    val updatedProfile = CompanyProfile(

                        id = 1,

                        name = name.trim(),

                        address = address.trim(),

                        email = email.trim(),

                        phone = phone.trim(),

                        logoUri = logoUri,

                        sealUri = sealUri,

                        signatureUri = signatureUri,

                        footerText = footerText.trim()
                    )

                    viewModel.updateCompanyProfile(
                        updatedProfile
                    )

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
                    text = "SAVE PROFILE",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }
}


/*
 * IMAGE UPLOAD CARD
 */

@Composable
fun ImageUploadCard(
    label: String,
    uri: String?,
    onUpload: () -> Unit
) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(
            containerColor = ShahWhite
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = label,
                    color = ShahBlack,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = if (uri != null) {
                        "Image uploaded"
                    } else {
                        "No image selected"
                    },
                    color = ShahMediumGrey,
                    fontSize = 12.sp
                )
            }

            /*
             * Preview image
             */

            if (!uri.isNullOrBlank()) {

                AsyncImage(

                    model = uri,

                    contentDescription = label,

                    modifier = Modifier
                        .size(60.dp)
                        .padding(4.dp)
                )
            }

            /*
             * Upload button
             */

            IconButton(
                onClick = onUpload
            ) {

                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload $label",
                    tint = ShahGreen
                )
            }
        }
    }
}


/*
 * COMMON SETTINGS TEXT FIELD
 */

@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    OutlinedTextField(

        value = value,

        onValueChange = onValueChange,

        label = {
            Text(label)
        },

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),

        shape = RoundedCornerShape(12.dp),

        singleLine = false
    )
}