package com.shahsurveyors.myapplication.ui.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard

data class Equipment(val name: String, val serial: String, val status: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentTrackerScreen() {
    val equipmentList = remember {
        listOf(
            Equipment("Leica DGPS GS16", "SN12345", "Available"),
            Equipment("Leica Total Station TS04 (Unit 1)", "SN001", "In Use"),
            Equipment("Precision AutoLevel Set", "SN999", "Available")
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Equipment Fleet") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(equipmentList) { item ->
                GlassCard(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    Text("Serial: ${item.serial}", style = MaterialTheme.typography.bodySmall)
                    Text("Status: ${item.status}", color = if (item.status == "Available") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
