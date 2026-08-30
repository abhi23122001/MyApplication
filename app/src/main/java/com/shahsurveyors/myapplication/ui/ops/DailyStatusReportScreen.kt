package com.shahsurveyors.myapplication.ui.ops

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.shahsurveyors.myapplication.models.DSRModel
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStatusReportScreen(viewModel: DSRViewModel, onBack: () -> Unit = {}) {
    var selectedProject by remember { mutableStateOf<ProjectModel?>(null) }
    var projectMenu by remember { mutableStateOf(false) }
    var chainage by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var manpower by remember { mutableStateOf("") }
    var instrument by remember { mutableStateOf("Leica TS04") }
    var workDone by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var showValidation by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.loadProjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Status Report", fontWeight = FontWeight.Bold, color = ShahWhite)
                        Text("Production • project-wise field report", fontSize = 10.sp, color = ShahWhite.copy(.72f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().background(ShahGrey).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                Column(Modifier.padding(17.dp)) {
                    Text("Report Details", color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    ExposedDropdownMenuBox(expanded = projectMenu, onExpandedChange = { projectMenu = !projectMenu }) {
                        OutlinedTextField(
                            value = selectedProject?.name.orEmpty(), onValueChange = {}, readOnly = true,
                            label = { Text("Select Project *") }, placeholder = { Text("Choose active project") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(projectMenu) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                            viewModel.projects.forEach { project ->
                                DropdownMenuItem(text = { Text(project.name) }, onClick = {
                                    selectedProject = project
                                    projectMenu = false
                                    viewModel.fetchDSR(project.id)
                                })
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    DSRTextField(chainage, { chainage = it }, "Total Chainage / Distance (m)")
                    DSRTextField(points, { points = it }, "Total Points Collected")
                    DSRTextField(area, { area = it }, "Total Area Covered (Acres)")
                    DSRTextField(manpower, { manpower = it }, "Manpower Count")
                    Spacer(Modifier.height(5.dp))
                    Text("Instrument Used", fontSize = 11.sp, color = ShahMediumGrey, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(instrument == "Leica TS04", { instrument = "Leica TS04" }, colors = RadioButtonDefaults.colors(selectedColor = ShahGreen))
                        Text("Leica TS04", fontSize = 12.sp)
                        RadioButton(instrument == "Leica GS16", { instrument = "Leica GS16" }, colors = RadioButtonDefaults.colors(selectedColor = ShahGreen))
                        Text("Leica GS16 DGPS", fontSize = 12.sp)
                    }
                    OutlinedTextField(workDone, { workDone = it }, label = { Text("Work Done / Progress *") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(remarks, { remarks = it }, label = { Text("Site Remarks") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6, shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(6.dp))
                    Button(
                        enabled = selectedProject != null && workDone.isNotBlank() && !viewModel.isLoading,
                        onClick = {
                            val valid = selectedProject != null && workDone.isNotBlank() && chainage.isNotBlank() && points.isNotBlank() && area.isNotBlank()
                            showValidation = !valid
                            if (valid) {
                                val user = FirebaseAuth.getInstance().currentUser
                                viewModel.submitDSR(DSRModel(
                                    uid = user?.uid.orEmpty(), userName = user?.displayName.orEmpty(),
                                    projectId = selectedProject!!.id, projectName = selectedProject!!.name,
                                    manpowerCount = manpower.toIntOrNull() ?: 0,
                                    workDone = "Chainage: $chainage, Points: $points, Area: $area\n$workDone",
                                    remarks = remarks, equipmentUsed = instrument, date = Timestamp.now()
                                )) { submitted = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)
                    ) {
                        Icon(Icons.Default.Send, null)
                        Spacer(Modifier.width(7.dp))
                        Text(if (viewModel.isLoading) "SUBMITTING..." else "SUBMIT PRODUCTION DSR", fontWeight = FontWeight.ExtraBold)
                    }
                    if (showValidation) Text("Project, chainage, points, area and work details are required.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    viewModel.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    if (submitted) Text("DSR submitted successfully and linked to ${selectedProject?.name}.", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("DSR records are stored in Firebase under the project and protected against duplicate same-day submissions.", color = ShahMediumGrey, fontSize = 10.sp)
        }
    }
}

@Composable
fun DSRTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShahGreen))
}