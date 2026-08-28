package com.shahsurveyors.myapplication.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.ui.theme.*

private const val TEAM_MESSAGES_COLLECTION = "team_messages"

data class StaffLocation(val name: String, val lat: Double, val lon: Double, val battery: Int, val lastUpdate: String)
data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val sender: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    userUid: String = "",
    userName: String = "User",
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LIVE RADAR", "TEAM CHAT")
    Scaffold(topBar = {
        Column {
            TopAppBar(
                title = {
                    Column {
                        Text("Team Sync", fontWeight = FontWeight.Bold, color = ShahWhite)
                        Text("Live team location & communication", fontSize = 10.sp, color = ShahWhite.copy(alpha = .72f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
            TabRow(selectedTabIndex = selectedTab, containerColor = ShahWhite, contentColor = ShahGreen, indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[selectedTab]), color = ShahGreen)
            }) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) })
                }
            }
        }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            if (selectedTab == 0) LiveRadarView() else TeamChatView(userUid = userUid, userName = userName)
        }
    }
}

@Composable
fun LiveRadarView() {
    val context = LocalContext.current
    val staffList = remember {
        listOf(
            StaffLocation("Aditya (DGPS)", 24.123456, 82.654321, 85, "2 min ago"),
            StaffLocation("Verma (TS)", 24.125, 82.656, 42, "Just now"),
            StaffLocation("John (Site)", 24.121, 82.651, 98, "10 min ago")
        )
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = ShahGreen.copy(alpha = .07f)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(11.dp), color = ShahGreen.copy(alpha = .12f)) {
                        Icon(Icons.Default.Radar, null, tint = ShahGreen, modifier = Modifier.padding(9.dp).size(23.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("Team locations", fontWeight = FontWeight.Bold, color = ShahDarkGreen)
                        Text("Tap a member to open their location", fontSize = 10.sp, color = ShahMediumGrey)
                    }
                }
            }
        }
        items(staffList) { staff ->
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite), elevation = CardDefaults.cardElevation(1.dp)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = if (staff.lastUpdate == "Just now") SuccessGreen.copy(alpha=.10f) else WarningAmber.copy(alpha=.10f)) {
                        Icon(Icons.Default.PersonPinCircle, null, tint = if (staff.lastUpdate == "Just now") SuccessGreen else WarningAmber, modifier = Modifier.padding(10.dp).size(23.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(staff.name, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(5.dp))
                        Row {
                            Icon(Icons.Default.BatteryChargingFull, null, Modifier.size(14.dp), tint=SuccessGreen)
                            Spacer(Modifier.width(3.dp))
                            Text("${staff.battery}%", fontSize=10.sp, color=ShahMediumGrey)
                            Spacer(Modifier.width(10.dp))
                            Text(staff.lastUpdate, fontSize=10.sp, color=ShahMediumGrey)
                        }
                    }
                    FilledTonalIconButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${staff.lat},${staff.lon}?q=${staff.lat},${staff.lon}(${staff.name})")))
                    }) { Icon(Icons.Default.Map, "Open in Maps", tint = ShahGreen) }
                }
            }
        }
    }
}

@Composable
fun TeamChatView(userUid: String, userName: String) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val listState = rememberLazyListState()
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val registration: ListenerRegistration = firestore.collection(TEAM_MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(200)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    error = exception.message ?: "Unable to load team chat"
                    return@addSnapshotListener
                }
                messages = snapshot?.documents?.map { doc ->
                    ChatMessage(
                        id = doc.id,
                        senderUid = doc.getString("senderUid") ?: "",
                        sender = doc.getString("senderName") ?: "User",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getTimestamp("timestamp")
                    )
                } ?: emptyList()
            }
        onDispose { registration.remove() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), color=ShahWhite) {
                    Row(Modifier.padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null, tint=ShahGreen)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Team Chat", fontWeight=FontWeight.Bold)
                            Text("Real-time communication with your team", fontSize=10.sp, color=ShahMediumGrey)
                        }
                    }
                }
            }
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderUid == userUid
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                    Surface(
                        color = if (isMe) ShahGreen else ShahWhite,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 1.dp
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(msg.sender, color=if(isMe) ShahWhite else ShahGreen, fontWeight=FontWeight.Bold, fontSize=11.sp)
                            Spacer(Modifier.height(3.dp))
                            Text(msg.message, color=if(isMe) ShahWhite else ShahBlack, fontSize=13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                msg.timestamp?.toDate()?.let { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).format(it) } ?: "",
                                color=if(isMe) ShahWhite.copy(.7f) else ShahMediumGrey,
                                fontSize=9.sp,
                                modifier=Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        if (error != null) {
            Text(error ?: "", color=ErrorRed, fontSize=11.sp, modifier=Modifier.padding(horizontal=12.dp))
        }

        Surface(color=ShahWhite, shadowElevation=10.dp) {
            Row(Modifier.padding(8.dp), verticalAlignment=Alignment.CenterVertically) {
                OutlinedTextField(
                    value=text,
                    onValueChange={text=it; error=null},
                    modifier=Modifier.weight(1f),
                    placeholder={Text("Type a message...", fontSize=12.sp)},
                    singleLine=true,
                    shape=RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(6.dp))
                FilledIconButton(
                    enabled = text.isNotBlank() && !isSending && userUid.isNotBlank(),
                    onClick = {
                        val messageText = text.trim()
                        if (messageText.isBlank()) return@FilledIconButton
                        isSending = true
                        firestore.collection(TEAM_MESSAGES_COLLECTION).add(
                            mapOf(
                                "senderUid" to userUid,
                                "senderName" to userName.ifBlank { "User" },
                                "message" to messageText,
                                "timestamp" to Timestamp.now()
                            )
                        ).addOnSuccessListener {
                            text = ""
                            isSending = false
                        }.addOnFailureListener {
                            error = it.message ?: "Unable to send message"
                            isSending = false
                        }
                    },
                    shape=RoundedCornerShape(14.dp)
                ) {
                    if (isSending) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth=2.dp, color=ShahWhite)
                    else Icon(Icons.AutoMirrored.Filled.Send, "Send")
                }
            }
        }
    }
}
