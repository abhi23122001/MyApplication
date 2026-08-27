package com.shahsurveyors.myapplication.ui.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.*
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentTrackerScreen(viewModel: EquipmentViewModel, onBack: () -> Unit = {}) {
    var showAdd by remember { mutableStateOf(false) }
    var handoverItem by remember { mutableStateOf<EquipmentModel?>(null) }
    var historyItem by remember { mutableStateOf<EquipmentModel?>(null) }
    var returnItem by remember { mutableStateOf<EquipmentModel?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchEquipment() }
    val total = viewModel.equipmentList.size
    val available = viewModel.equipmentList.count { it.status == "AVAILABLE" }
    val inUse = viewModel.equipmentList.count { it.status == "IN_USE" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Leica Fleet Custody", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Equipment & complete custody history", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = ShahGreen, contentColor = ShahWhite) { Icon(Icons.Default.Add, "Add Equipment") } }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Summary("Total", total.toString(), Icons.Default.Inventory2, ShahGreen, Modifier.weight(1f))
                Summary("Available", available.toString(), Icons.Default.CheckCircle, SuccessGreen, Modifier.weight(1f))
                Summary("In Use", inUse.toString(), Icons.Default.PersonPin, WarningAmber, Modifier.weight(1f))
            }
            viewModel.errorMessage?.let { error -> Text(error, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
            if (viewModel.equipmentList.isEmpty() && !viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No equipment found", color = ShahMediumGrey) }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.equipmentList, key = { it.id }) { item ->
                        EquipmentCard(item, onHandover = { handoverItem = item }, onReturn = { returnItem = item }, onHistory = { historyItem = item })
                    }
                }
            }
        }
    }

    if (showAdd) AddEquipmentDialog(viewModel) { showAdd = false }
    handoverItem?.let { HandoverDialog(it, viewModel) { handoverItem = null } }
    returnItem?.let { ReturnDialog(it, viewModel) { returnItem = null } }
    historyItem?.let { HistoryDialog(it, viewModel) { historyItem = null } }
}

@Composable
private fun AddEquipmentDialog(viewModel: EquipmentViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }; var model by remember { mutableStateOf("") }; var serial by remember { mutableStateOf("") }; var category by remember { mutableStateOf("Survey Instrument") }
    AlertDialog(
        onDismissRequest = { if (!viewModel.isLoading) onDismiss() }, title = { Text("Add Equipment", fontWeight = FontWeight.Bold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(name, { name = it }, label = { Text("Equipment name *") }, singleLine = true); OutlinedTextField(model, { model = it }, label = { Text("Model number") }, singleLine = true); OutlinedTextField(serial, { serial = it }, label = { Text("Serial number *") }, singleLine = true); OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true) } },
        confirmButton = { Button(enabled = name.isNotBlank() && serial.isNotBlank() && !viewModel.isLoading, onClick = { viewModel.addEquipment(name, model, serial, category, onDismiss) }, colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)) { Text("SAVE") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandoverDialog(item: EquipmentModel, viewModel: EquipmentViewModel, onDismiss: () -> Unit) {
    var employee by remember { mutableStateOf<UserProfile?>(null) }; var project by remember { mutableStateOf<ProjectModel?>(null) }; var location by remember { mutableStateOf("") }; var other by remember { mutableStateOf("") }; var employeeMenu by remember { mutableStateOf(false) }; var projectMenu by remember { mutableStateOf(false) }
    val checks = remember { mutableStateMapOf("Prism" to false, "Tribrach" to false, "Battery x2" to false, "Tripod" to false, "Charger" to false) }
    LaunchedEffect(Unit) { viewModel.clearError(); viewModel.loadHandoverOptions() }
    val accessories = checks.filterValues { it }.keys.toMutableList().apply { addAll(other.split(',').map { it.trim() }.filter { it.isNotBlank() }) }.distinct()
    val canConfirm = employee?.uid?.isNotBlank() == true && project?.id?.isNotBlank() == true && location.isNotBlank() && !viewModel.isLoading

    AlertDialog(
        onDismissRequest = { if (!viewModel.isLoading) onDismiss() },
        title = { Text("Confirm Equipment Handover", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                ExposedDropdownMenuBox(expanded = employeeMenu, onExpandedChange = { employeeMenu = !employeeMenu }) {
                    OutlinedTextField(employee?.name.orEmpty(), {}, readOnly = true, label = { Text("Handover to *") }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = employeeMenu, onDismissRequest = { employeeMenu = false }) {
                        if (viewModel.employees.isEmpty()) DropdownMenuItem(text = { Text("No active employees found") }, onClick = { employeeMenu = false })
                        viewModel.employees.forEach { user -> DropdownMenuItem(text = { Text(user.name) }, onClick = { employee = user; employeeMenu = false }) }
                    }
                }
                ExposedDropdownMenuBox(expanded = projectMenu, onExpandedChange = { projectMenu = !projectMenu }) {
                    OutlinedTextField(project?.name.orEmpty(), {}, readOnly = true, label = { Text("Project *") }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                        if (viewModel.projects.isEmpty()) DropdownMenuItem(text = { Text("No active projects found") }, onClick = { projectMenu = false })
                        viewModel.projects.forEach { p -> DropdownMenuItem(text = { Text(p.name) }, onClick = { project = p; projectMenu = false }) }
                    }
                }
                OutlinedTextField(location, { location = it }, label = { Text("Handover location *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Accessories", fontWeight = FontWeight.Bold, color = ShahGreen)
                checks.keys.forEach { accessory -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = checks[accessory] == true, onCheckedChange = { checks[accessory] = it }); Text(accessory, fontSize = 11.sp) } }
                OutlinedTextField(other, { other = it }, label = { Text("Other accessories (comma separated)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                viewModel.errorMessage?.let { Text(it, color = ErrorRed, fontSize = 11.sp) }
            }
        },
        confirmButton = {
            Button(enabled = canConfirm, onClick = { viewModel.handoverEquipment(item.id, employee!!, project!!, location, accessories, onDismiss) }, colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)) { Text(if (viewModel.isLoading) "SAVING..." else "CONFIRM HANDOVER") }
        },
        dismissButton = { TextButton(enabled = !viewModel.isLoading, onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
private fun ReturnDialog(item: EquipmentModel, viewModel: EquipmentViewModel, onDismiss: () -> Unit) {
    var location by remember { mutableStateOf("") }; var remarks by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = { if (!viewModel.isLoading) onDismiss() }, title = { Text("Return Equipment") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Return from: ${item.assignedToName.orEmpty()}"); Text("Project: ${item.assignedProjectName.orEmpty()}"); OutlinedTextField(location, { location = it }, label = { Text("Return location *") }, singleLine = true); OutlinedTextField(remarks, { remarks = it }, label = { Text("Condition / remarks") }, minLines = 2); viewModel.errorMessage?.let { Text(it, color = ErrorRed, fontSize = 11.sp) } } }, confirmButton = { Button(enabled = location.isNotBlank() && !viewModel.isLoading, onClick = { viewModel.returnEquipment(item.id, location, remarks); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)) { Text("CONFIRM RETURN") } }, dismissButton = { TextButton(enabled = !viewModel.isLoading, onClick = onDismiss) { Text("CANCEL") } })
}

@Composable
private fun HistoryDialog(item: EquipmentModel, viewModel: EquipmentViewModel, onDismiss: () -> Unit) {
    var history by remember { mutableStateOf<List<EquipmentHandoverRecord>>(emptyList()) }; var loading by remember { mutableStateOf(true) }
    LaunchedEffect(item.id) { loading = true; viewModel.getHistory(item.id) { result -> history = result; loading = false } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Complete Handover History") }, text = { if (loading) Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } else if (history.isEmpty()) Text("No history found") else LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(history, key = { it.id }) { h -> Card(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) { Column(Modifier.padding(10.dp)) { Text("${h.action} • ${h.employeeName}", fontWeight = FontWeight.Bold); Text("Project: ${h.projectName.ifBlank { "-" }}", fontSize = 10.sp); Text("Location: ${h.location.ifBlank { "-" }}", fontSize = 10.sp); Text("Date/Time: ${h.actionAt?.toDate()?.let { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(it) } ?: "-"}", fontSize = 10.sp); Text("Accessories: ${h.accessories.joinToString().ifBlank { "None" }}", fontSize = 10.sp); if (h.remarks.isNotBlank()) Text("Remarks: ${h.remarks}", fontSize = 10.sp); Text("Recorded by: ${h.actionByName.ifBlank { h.actionByUid.ifBlank { "Admin" } }}", fontSize = 9.sp, color = ShahMediumGrey) } } } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("CLOSE") } })
}

@Composable
private fun EquipmentCard(item: EquipmentModel, onHandover: () -> Unit, onReturn: () -> Unit, onHistory: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val tint = when (item.status) { "AVAILABLE" -> SuccessGreen; "IN_USE" -> WarningAmber; "MAINTENANCE" -> ErrorRed; else -> ShahMediumGrey }
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(item.name, fontWeight = FontWeight.Bold); Text("S/N: ${item.serialNumber}", fontSize = 10.sp, color = ShahMediumGrey); Surface(shape = RoundedCornerShape(8.dp), color = tint.copy(.10f)) { Text(item.status, color = tint, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp)) }; item.assignedToName?.takeIf { it.isNotBlank() }?.let { Text("Assigned: $it", fontSize = 10.sp) }; item.assignedProjectName?.takeIf { it.isNotBlank() }?.let { Text("Project: $it", fontSize = 10.sp) } }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null) }
            }
            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 7.dp))
                item.handoverLocation?.let { Text("Current location: $it", fontSize = 10.sp) }
                item.handoverAt?.let { Text("Current handover: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(it.toDate())}", fontSize = 10.sp) }
                if (item.handoverAccessories.isNotEmpty()) Text("Current accessories: ${item.handoverAccessories.joinToString()}", fontSize = 10.sp)
                item.lastReturnLocation?.takeIf { it.isNotBlank() }?.let { Text("Last return location: $it", fontSize = 10.sp) }
                item.lastReturnAt?.let { Text("Last returned: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(it.toDate())}", fontSize = 10.sp) }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (item.status == "AVAILABLE") Button(onClick = onHandover, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)) { Text("HANDOVER") }
                    if (item.status == "IN_USE") OutlinedButton(onClick = onReturn, Modifier.weight(1f)) { Text("RETURN") }
                    OutlinedButton(onClick = onHistory, Modifier.weight(1f)) { Text("HISTORY") }
                }
            }
        }
    }
}

@Composable
private fun Summary(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier) {
    Card(modifier, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) { Column(Modifier.padding(10.dp)) { Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp)); Text(value, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(title, fontSize = 9.sp, color = ShahMediumGrey) } }
}
