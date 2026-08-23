package com.shahsurveyors.myapplication.ui.ops

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStatusReportScreen() {
    var chainage by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var instrument by remember { mutableStateOf("Leica TS04") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily Field DSR") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = chainage, onValueChange = { chainage = it }, label = { Text("Total Chainage (m)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = points, onValueChange = { points = it }, label = { Text("Points Collected") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text("Instrument Used", style = MaterialTheme.typography.labelSmall)
                Row {
                    RadioButton(selected = instrument == "Leica TS04", onClick = { instrument = "Leica TS04" })
                    Text("Total Station", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = instrument == "Leica GS16", onClick = { instrument = "Leica GS16" })
                    Text("DGPS", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { /* Submit DSR */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("SUBMIT DAILY LOG")
                }
            }
        }
    }
}
