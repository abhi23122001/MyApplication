package com.shahsurveyors.myapplication.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

data class StaffLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val battery: Int,
    val lastUpdate: String
)

data class ChatMessage(val sender: String, val message: String, val time: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LIVE RADAR", "TEAM CHAT")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Team Sync", fontWeight = FontWeight.Bold, color = ShahWhite) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShahWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ShahWhite,
                    contentColor = ShahGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ShahGreen
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index, 
                            onClick = { selectedTab = index }, 
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            if (selectedTab == 0) {
                LiveRadarView()
            } else {
                TeamChatView()
            }
        }
    }
}

@Composable
fun LiveRadarView() {
    val context = LocalContext.current
    val staffList = remember {
        listOf(
            StaffLocation("Aditya (DGPS)", 24.123456, 82.654321, 85, "2 min ago"),
            StaffLocation("Verma (TS)", 24.125000, 82.656000, 42, "Just now"),
            StaffLocation("John (Site)", 24.121000, 82.651000, 98, "10 min ago")
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(staffList) { staff ->
            Card(
                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShahWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(staff.name, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(12.dp), tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${staff.battery}% Battery", color = ShahMediumGrey, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Last sync: ${staff.lastUpdate}", color = ShahMediumGrey, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = {
                        val uri = "geo:${staff.lat},${staff.lon}?q=${staff.lat},${staff.lon}(${staff.name})"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Map, contentDescription = "Open in Maps", tint = ShahGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun TeamChatView() {
    val messages = remember {
        listOf(
            ChatMessage("Admin", "All teams: Report to Site A by 09:00", "08:30 AM"),
            ChatMessage("Aditya", "DGPS base setup complete at BM-01", "08:45 AM")
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
            items(messages) { msg ->
                val isMe = msg.sender == "Admin"
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isMe) ShahGreen else ShahWhite,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(msg.sender, color = if (isMe) ShahWhite else ShahGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(msg.message, color = if (isMe) ShahWhite else ShahBlack, fontSize = 14.sp)
                            Text(msg.time, color = if (isMe) ShahWhite.copy(alpha = 0.7f) else ShahMediumGrey, fontSize = 9.sp, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
        
        var text by remember { mutableStateOf("") }
        Surface(color = ShahWhite, tonalElevation = 8.dp) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* Upload attachment */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = ShahMediumGrey)
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...", color = ShahMediumGrey) },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { if(text.isNotBlank()) text = "" }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = ShahGreen)
                }
            }
        }
    }
}
