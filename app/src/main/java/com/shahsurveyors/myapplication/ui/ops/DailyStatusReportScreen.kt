package com.shahsurveyors.myapplication.ui.ops

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.DSRModel
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStatusReportScreen(
    viewModel: DSRViewModel,
    onBack: () -> Unit = {}
) {
    var chainage by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var instrument by remember { mutableStateOf("Leica TS04") }
    var remarks by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showValidationError by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedFileUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Status Report",
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

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShahWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Field Logger Details", color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))

                    DSRTextField(value = chainage, onValueChange = { chainage = it }, label = "Total Chainage / Distance (m)")
                    DSRTextField(value = points, onValueChange = { points = it }, label = "Total Points Collected")
                    DSRTextField(value = area, onValueChange = { area = it }, label = "Total Area Covered (Acres)")

                    Spacer(Modifier.height(16.dp))
                    Text(text = "Instrument Used", style = MaterialTheme.typography.labelMedium, color = ShahMediumGrey, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = instrument == "Leica TS04", onClick = { instrument = "Leica TS04" }, colors = RadioButtonDefaults.colors(selectedColor = ShahGreen))
                        Text("Leica TS04", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(12.dp))
                        RadioButton(selected = instrument == "Leica GS16", onClick = { instrument = "Leica GS16" }, colors = RadioButtonDefaults.colors(selectedColor = ShahGreen))
                        Text("Leica GS16 DGPS", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("General Site Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ShahGreen)
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Upload File")
                        Spacer(Modifier.width(8.dp))
                        Text(text = if (selectedFileUri != null) "FILE SELECTED" else "UPLOAD CAD / KML DATA", fontWeight = FontWeight.Bold)
                    }

                    if (selectedFileUri != null) {
                        Spacer(Modifier.height(8.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), color = ShahGrey, shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, tint = ShahGreen)
                                Spacer(Modifier.width(8.dp))
                                Text(text = "Survey file attached", color = ShahDarkGrey, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val valid = chainage.isNotBlank() && points.isNotBlank() && area.isNotBlank() && remarks.isNotBlank()
                            if (!valid) {
                                showValidationError = true
                            } else {
                                showValidationError = false
                                // Simplified submission logic
                                val dsr = DSRModel(
                                    workDone = "Chainage: $chainage, Points: $points, Area: $area",
                                    remarks = remarks,
                                    equipmentUsed = instrument,
                                    date = com.google.firebase.Timestamp.now()
                                )
                                viewModel.submitDSR(dsr)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite)
                    ) {
                        Text(text = "SUBMIT PRODUCTION DSR", fontWeight = FontWeight.Bold)
                    }

                    if (showValidationError) {
                        Spacer(Modifier.height(8.dp))
                        Text(text = "Please fill all required DSR fields.", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(text = "DSR records will be stored in Firebase and can be linked with the assigned project/site.", color = ShahMediumGrey, fontSize = 11.sp)
        }
    }
}

@Composable
fun DSRTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}
