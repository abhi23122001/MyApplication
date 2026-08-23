package com.shahsurveyors.myapplication.ui.survey

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyCalculatorScreen() {
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Enter coordinates to convert") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Survey Utilities") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("WGS84 to UTM Zone 44N", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lon,
                    onValueChange = { lon = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val l = lat.toDoubleOrNull()
                        val n = lon.toDoubleOrNull()
                        if (l != null && n != null) {
                            result = convertToUTM44N(l, n)
                        } else {
                            result = "Invalid Input"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONVERT")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(result, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Simplified WGS84 to UTM Zone 44N conversion
private fun convertToUTM44N(lat: Double, lon: Double): String {
    val zone = 44
    val centralMeridian = (zone * 6 - 183).toDouble()
    
    // This is a highly simplified approximation for demonstration
    // In a real production app, use a library like Proj4J or the official Karney's algorithm
    val easting = 500000 + (lon - centralMeridian) * 111320 * cos(Math.toRadians(lat))
    val northing = lat * 111132
    
    return String.format("Easting: %.2f, Northing: %.2f (UTM 44N)", easting, northing)
}
