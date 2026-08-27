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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.EquipmentModel
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentTrackerScreen(viewModel: EquipmentViewModel, onBack: () -> Unit = {}) {
    var showAddDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.fetchEquipment() }
    val total = viewModel.equipmentList.size
    val available = viewModel.equipmentList.count { it.status == "AVAILABLE" }
    val inUse = viewModel.equipmentList.count { it.status == "IN_USE" }

    Scaffold(
        topBar = { TopAppBar(title = { Column { Text("Leica Fleet Custody", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Equipment & handover tracking", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = ShahGreen, contentColor = ShahWhite, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, "Add Equipment") } }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { EquipmentSummary("Total", total.toString(), Icons.Default.Inventory2, ShahGreen, Modifier.weight(1f)); EquipmentSummary("Available", available.toString(), Icons.Default.CheckCircle, SuccessGreen, Modifier.weight(1f)); EquipmentSummary("In Use", inUse.toString(), Icons.Default.PersonPin, WarningAmber, Modifier.weight(1f)) }
            if (viewModel.isLoading && viewModel.equipmentList.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ShahGreen) }
            else if (viewModel.equipmentList.isEmpty()) Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Inventory2, null, tint = ShahMediumGrey, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(10.dp)); Text("No equipment found", fontWeight = FontWeight.Bold); Text("Tap + to add your first survey instrument.", fontSize = 11.sp, color = ShahMediumGrey) } }
            else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(viewModel.equipmentList, key = { it.id }) { EquipmentCard(it) } }
        }
    }
    if (showAddDialog) AddEquipmentDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
}

@Composable
private fun AddEquipmentDialog(viewModel: EquipmentViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Survey Instrument") }
    val canSave = name.isNotBlank() && serial.isNotBlank() && !viewModel.isLoading
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Equipment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Equipment name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(model, { model = it }, label = { Text("Model number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(serial, { serial = it }, label = { Text("Serial number *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (!viewModel.errorMessage.isNullOrBlank()) Text(viewModel.errorMessage.orEmpty(), color = ErrorRed, fontSize = 11.sp)
            }
        },
        confirmButton = { Button(enabled = canSave, onClick = { viewModel.addEquipment(name, model, serial, category) { onDismiss() } }, colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)) { if (viewModel.isLoading) { CircularProgressIndicator(Modifier.size(18.dp), color = ShahWhite, strokeWidth = 2.dp) } else Text("SAVE") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !viewModel.isLoading) { Text("CANCEL") } }
    )
}

@Composable
private fun EquipmentSummary(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: androidx.compose.ui.graphics.Color, modifier: Modifier) { Card(modifier, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite), elevation = CardDefaults.cardElevation(1.dp)) { Column(Modifier.padding(10.dp)) { Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp)); Spacer(Modifier.height(5.dp)); Text(value, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(title, fontSize = 9.sp, color = ShahMediumGrey) } } }

@Composable
fun EquipmentCard(item: EquipmentModel) {
    var expanded by remember { mutableStateOf(false) }
    val statusColor = when (item.status) { "AVAILABLE" -> SuccessGreen; "IN_USE" -> WarningAmber; "MAINTENANCE" -> ErrorRed; else -> ShahMediumGrey }
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = ShahGreen.copy(.10f)) { Icon(Icons.Default.PrecisionManufacturing, null, tint = ShahGreen, modifier = Modifier.padding(10.dp).size(25.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(item.name, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("S/N: ${item.serialNumber}", color = ShahMediumGrey, fontSize = 10.sp); Spacer(Modifier.height(4.dp)); Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(.10f)) { Text(item.status, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) }; item.assignedToName?.takeIf { it.isNotBlank() }?.let { Text("Assigned: $it", color = ShahDarkGrey, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp)) } }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, if (expanded) "Collapse" else "Expand", tint = ShahMediumGrey) }
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp)); HorizontalDivider(color = ShahLightGrey); Spacer(Modifier.height(12.dp)); Text("Handover Checklist", color = ShahGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                listOf("Tripod", "Prism", "Battery x2", "Charger", "Tribrach").forEach { label -> var checked by remember { mutableStateOf(true) }; Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked, onCheckedChange = { checked = it }, colors = CheckboxDefaults.colors(checkedColor = ShahGreen)); Text(label, fontSize = 12.sp) } }
                Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ShahGreen)) { Icon(Icons.Default.Verified, null); Spacer(Modifier.width(7.dp)); Text("CONFIRM HANDOVER", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            }
        }
    }
}
