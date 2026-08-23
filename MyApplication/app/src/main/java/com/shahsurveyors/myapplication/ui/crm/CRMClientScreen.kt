package com.shahsurveyors.myapplication.ui.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.theme.DeepMidnightSlate
import com.shahsurveyors.myapplication.ui.theme.ElectricGold

data class Client(
    val name: String,
    val phone: String,
    val company: String,
    val email: String,
    val location: String,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CRMClientScreen(onBack: () -> Unit = {}) {
    val clients = remember {
        mutableStateListOf(
            Client("Aditya Singh", "+91 9876543210", "NTPC Singrauli", "aditya@ntpc.co.in", "Shakti Nagar", "Regular Survey Client"),
            Client("Rajesh Verma", "+91 8877665544", "Northern Coalfields", "rajesh@ncl.gov.in", "Jayant Project", "Soil Testing requirement")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client Directory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnightSlate,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add Client */ }, containerColor = ElectricGold, contentColor = Color.Black) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(DeepMidnightSlate)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(clients) { client ->
                    ClientCard(client)
                }
            }
        }
    }
}

@Composable
fun ClientCard(client: Client) {
    GlassCard(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = ElectricGold.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(client.name.take(1), color = ElectricGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(client.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(client.company, color = ElectricGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = { /* Call */ }) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color.Green)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(client.location, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(client.email, color = Color.Gray, fontSize = 12.sp)
        }
        
        if (client.notes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    client.notes, 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 11.sp, 
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
