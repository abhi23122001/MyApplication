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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.EquipmentModel
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentTrackerScreen(
    viewModel: EquipmentViewModel,
    onBack: () -> Unit = {},
    onAddEquipment: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchEquipment()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Leica Fleet Custody",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEquipment,
                containerColor = ShahGreen,
                contentColor = ShahWhite
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Equipment")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
        ) {
            if (viewModel.isLoading && viewModel.equipmentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (viewModel.equipmentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No equipment found", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(items = viewModel.equipmentList, key = { it.id }) { equipment ->
                        EquipmentCard(item = equipment)
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentCard(item: EquipmentModel) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (item.status) {
        "AVAILABLE" -> SuccessGreen
        "IN_USE" -> WarningAmber
        "MAINTENANCE" -> ErrorRed
        else -> ShahMediumGrey
    }

    Card(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ShahGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(32.dp),
                        tint = ShahGreen
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "S/N: ${item.serialNumber}", color = ShahMediumGrey, fontSize = 12.sp)
                    Text(text = item.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    item.assignedToName?.let {
                        if (it.isNotBlank()) {
                            Text(text = "Assigned: $it", color = ShahDarkGrey, fontSize = 11.sp)
                        }
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = ShahMediumGrey
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = ShahLightGrey)
                Spacer(Modifier.height(12.dp))
                
                // Simplified checklist for now
                val checklist = listOf("Tripod", "Prism", "Battery x2", "Charger", "Tribrach")
                
                Text(text = "Handover Checklist", color = ShahGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                checklist.forEach { checklistItem ->
                    var checked by remember { mutableStateOf(true) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            colors = CheckboxDefaults.colors(checkedColor = ShahGreen)
                        )
                        Text(text = checklistItem, color = ShahBlack, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { /* TODO: Implement handover logic */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite)
                    ) {
                        Text(text = "CONFIRM HANDOVER", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
