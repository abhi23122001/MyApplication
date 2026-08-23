package com.shahsurveyors.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAttendance: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Enterprise Hub") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Welcome, Shah Surveyors & Consultancy")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateToAttendance, modifier = Modifier.fillMaxWidth()) {
                    Text("PUNCH ATTENDANCE")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateToEquipment, modifier = Modifier.fillMaxWidth()) {
                    Text("EQUIPMENT FLEET")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateToTasks, modifier = Modifier.fillMaxWidth()) {
                    Text("TASK MANAGEMENT")
                }
            }
        }
    }
}
