package com.shahsurveyors.myapplication.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("SURVEY", "MARKETING")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Task Hub") })
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Assigned Tasks for ${tabs[selectedTab]}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("• Site Survey - Waidhan Project (HIGH)", color = MaterialTheme.colorScheme.primary)
                Text("• DSR Submission - chainage 1200m", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
