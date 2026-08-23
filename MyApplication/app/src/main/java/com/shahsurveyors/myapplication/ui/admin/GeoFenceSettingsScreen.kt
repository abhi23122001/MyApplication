package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoFenceSettingsScreen(onBack: () -> Unit) {
    var radius by remember { mutableStateOf("200") }
    var enableGeoFence by remember { mutableStateOf(true) }
    var allowRemoteAttendance by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geo-Fence Settings", fontWeight = FontWeight.Bold, color = ShahWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Save */ }, containerColor = ShahGreen, contentColor = ShahWhite) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ShahGrey)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShahWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Enable Geo-Fencing", fontWeight = FontWeight.Bold)
                        Switch(checked = enableGeoFence, onCheckedChange = { enableGeoFence = it }, colors = SwitchDefaults.colors(checkedThumbColor = ShahGreen))
                    }
                    Text("Attendance will be restricted to assigned sites.", style = MaterialTheme.typography.bodySmall, color = ShahMediumGrey)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = radius,
                        onValueChange = { radius = it },
                        label = { Text("Allowed Radius (meters)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Allow Remote Attendance", fontWeight = FontWeight.Bold)
                        Switch(checked = allowRemoteAttendance, onCheckedChange = { allowRemoteAttendance = it }, colors = SwitchDefaults.colors(checkedThumbColor = ShahGreen))
                    }
                    Text("Allow employees to punch from any location (e.g. for marketing).", style = MaterialTheme.typography.bodySmall, color = ShahMediumGrey)
                }
            }
        }
    }
}
