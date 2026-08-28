package com.shahsurveyors.myapplication.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TEAM_MESSAGES_COLLECTION = "team_messages"
private const val DIRECT_MESSAGES_COLLECTION = "direct_messages"
private const val USERS_COLLECTION = "users"

data class StaffLocation(val uid: String, val name: String, val lat: Double, val lon: Double, val sharing: Boolean, val lastUpdate: Date?)
data class ChatMessage(val id: String = "", val senderUid: String = "", val sender: String = "", val message: String = "", val timestamp: Timestamp? = null)
data class ChatUser(val uid: String, val name: String, val email: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(userUid: String = "", userName: String = "User", isAdmin: Boolean = false, onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LIVE RADAR", "TEAM CHAT", "PERSONAL CHAT")
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Team Sync", fontWeight = FontWeight.Bold, color = ShahWhite)
                            Text("Live team location & communication", fontSize = 10.sp, color = ShahWhite.copy(alpha = .72f))
                        }
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 10.sp) })
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            when (selectedTab) {
                0 -> LiveRadarView(userUid, isAdmin)
                1 -> TeamChatView(userUid, userName)
                else -> PersonalChatView(userUid, userName)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LiveRadarView(userUid: String, isAdmin: Boolean) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val locationClient: FusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var staff by remember { mutableStateOf<List<StaffLocation>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var sharing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        sharing = granted
        if (!granted) error = "Location permission is required to share your live location."
    }

    DisposableEffect(Unit) {
        val registration: ListenerRegistration = firestore.collection(USERS_COLLECTION).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                error = exception.message ?: "Unable to load team locations"
                return@addSnapshotListener
            }
            staff = snapshot?.documents.orEmpty().mapNotNull { doc ->
                val lat = doc.getDouble("latitude") ?: doc.getDouble("lat")
                val lon = doc.getDouble("longitude") ?: doc.getDouble("lon")
                val email = doc.getString("email").orEmpty()
                val name = doc.getString("name")?.takeIf { it.isNotBlank() } ?: email.ifBlank { "Employee" }
                val active = doc.getBoolean("active") ?: true
                if (!active || lat == null || lon == null) null else StaffLocation(
                    uid = doc.id,
                    name = name,
                    lat = lat,
                    lon = lon,
                    sharing = doc.getBoolean("locationSharing") == true,
                    lastUpdate = doc.getTimestamp("lastLocationAt")?.toDate()
                )
            }.filter { it.uid != userUid || isAdmin }.distinctBy { it.uid }.sortedBy { it.name.lowercase(Locale.ENGLISH) }
        }
        onDispose { registration.remove() }
    }

    DisposableEffect(sharing, userUid) {
        if (!sharing || userUid.isBlank()) {
            onDispose { }
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                onDispose { }
            } else {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15_000L).setMinUpdateIntervalMillis(5_000L).build()
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation ?: return
                        firestore.collection(USERS_COLLECTION).document(userUid).set(
                            mapOf("latitude" to location.latitude, "longitude" to location.longitude, "locationSharing" to true, "lastLocationAt" to Timestamp.now()),
                            SetOptions.merge()
                        )
                    }
                }
                locationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                firestore.collection(USERS_COLLECTION).document(userUid).set(mapOf("locationSharing" to true, "lastLocationAt" to Timestamp.now()), SetOptions.merge())
                onDispose {
                    locationClient.removeLocationUpdates(callback)
                    firestore.collection(USERS_COLLECTION).document(userUid).set(mapOf("locationSharing" to false, "lastLocationAt" to Timestamp.now()), SetOptions.merge())
                }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!isAdmin) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (sharing) "Live location is ON" else "Live location is OFF", fontWeight = FontWeight.Bold)
                            Text("Your location is shared with Admin only while enabled.", fontSize = 10.sp, color = ShahMediumGrey)
                        }
                        Button(
                            onClick = {
                                if (sharing) {
                                    sharing = false
                                } else {
                                    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (fine || coarse) sharing = true else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (sharing) ErrorRed else ShahGreen)
                        ) { Text(if (sharing) "STOP" else "START") }
                    }
                }
            }
        }
        item {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = ShahGreen.copy(alpha = .07f)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Radar, null, tint = ShahGreen, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("Team locations", fontWeight = FontWeight.Bold, color = ShahDarkGreen)
                        Text(if (isAdmin) "Employees sharing location appear here." else "Only employees currently sharing are shown.", fontSize = 10.sp, color = ShahMediumGrey)
                    }
                }
            }
        }
        error?.let { message -> item { Text(message, color = ErrorRed, fontSize = 11.sp) } }
        if (staff.isEmpty()) item { Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { Text("No live employee locations", color = ShahMediumGrey) } }
        items(staff, key = { it.uid }) { person ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (person.sharing) Icons.Default.PersonPinCircle else Icons.Default.Person, null, tint = if (person.sharing) SuccessGreen else ShahMediumGrey, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(person.name, fontWeight = FontWeight.Bold)
                        Text(if (person.sharing) "LIVE" else "Last location", color = if (person.sharing) SuccessGreen else ShahMediumGrey, fontSize = 10.sp)
                        person.lastUpdate?.let { Text(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(it), fontSize = 9.sp, color = ShahMediumGrey) }
                    }
                    FilledIconButton(onClick = {
                        val uri = Uri.parse("geo:${person.lat},${person.lon}?q=${person.lat},${person.lon}(${Uri.encode(person.name)})")
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure { error = "No map application is available." }
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
        val registration = firestore.collection(TEAM_MESSAGES_COLLECTION).limitToLast(200).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                error = exception.message ?: "Unable to load team chat"
                return@addSnapshotListener
            }
            messages = snapshot?.documents.orEmpty().map { doc ->
                ChatMessage(doc.id, doc.getString("senderUid").orEmpty(), doc.getString("senderName")?.ifBlank { "User" } ?: "User", doc.getString("message").orEmpty(), doc.getTimestamp("timestamp"))
            }.filter { it.message.isNotBlank() }.sortedWith(compareBy({ it.timestamp?.seconds ?: 0L }, { it.id }))
        }
        onDispose { registration.remove() }
    }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { msg ->
                val mine = msg.senderUid == userUid
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = if (mine) ShahGreen else ShahWhite) {
                    Column(Modifier.padding(12.dp)) {
                        Text(msg.sender, color = if (mine) ShahWhite else ShahGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(msg.message, color = if (mine) ShahWhite else ShahDarkGreen, fontSize = 13.sp)
                        Text(msg.timestamp?.toDate()?.let { SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(it) }.orEmpty(), fontSize = 9.sp, color = if (mine) ShahWhite.copy(alpha = .7f) else ShahMediumGrey, modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }
        error?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
        ChatInput(text, { text = it; error = null }, isSending, userUid.isNotBlank()) { value ->
            isSending = true
            firestore.collection(TEAM_MESSAGES_COLLECTION).add(mapOf("senderUid" to userUid, "senderName" to userName.ifBlank { "User" }, "message" to value, "timestamp" to Timestamp.now()))
                .addOnSuccessListener { text = ""; isSending = false }
                .addOnFailureListener { error = it.message ?: "Unable to send message"; isSending = false }
        }
    }
}

@Composable
private fun ChatInput(text: String, onText: (String) -> Unit, sending: Boolean, canSend: Boolean, onSend: (String) -> Unit) {
    Surface(color = ShahWhite, shadowElevation = 10.dp) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = text, onValueChange = onText, modifier = Modifier.weight(1f), placeholder = { Text("Type a message...", fontSize = 12.sp) }, singleLine = true, shape = RoundedCornerShape(24.dp))
            Spacer(Modifier.width(6.dp))
            FilledIconButton(enabled = text.trim().isNotBlank() && !sending && canSend, onClick = { onSend(text.trim()) }, shape = RoundedCornerShape(14.dp)) {
                if (sending) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Icon(Icons.AutoMirrored.Filled.Send, "Send")
            }
        }
    }
}

@Composable
fun PersonalChatView(userUid: String, userName: String) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var users by remember { mutableStateOf<List<ChatUser>>(emptyList()) }
    var selected by remember { mutableStateOf<ChatUser?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        val registration = firestore.collection(USERS_COLLECTION).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                error = exception.message ?: "Unable to load employees"
                return@addSnapshotListener
            }
            users = snapshot?.documents.orEmpty().mapNotNull { doc ->
                if (doc.id == userUid || doc.getBoolean("active") == false) return@mapNotNull null
                val email = doc.getString("email").orEmpty()
                ChatUser(doc.id, doc.getString("name")?.takeIf { it.isNotBlank() } ?: email.ifBlank { "Employee" }, email)
            }.distinctBy { it.uid }.sortedBy { it.name.lowercase(Locale.ENGLISH) }
        }
        onDispose { registration.remove() }
    }
    val selectedUser = selected
    if (selectedUser == null) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Personal Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ShahDarkGreen) }
            error?.let { item { Text(it, color = ErrorRed, fontSize = 11.sp) } }
            if (users.isEmpty()) item { Text("No employees available", color = ShahMediumGrey) }
            items(users, key = { it.uid }) { user ->
                Card(onClick = { selected = user }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = ShahGreen)
                        Spacer(Modifier.width(12.dp))
                        Column { Text(user.name, fontWeight = FontWeight.Bold); Text(user.email, fontSize = 10.sp, color = ShahMediumGrey) }
                    }
                }
            }
        }
    } else {
        DirectConversation(userUid, userName, selectedUser) { selected = null }
    }
}

@Composable
private fun DirectConversation(userUid: String, userName: String, other: ChatUser, onBack: () -> Unit) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val listState = rememberLazyListState()
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }
    val conversationId = remember(userUid, other.uid) { listOf(userUid, other.uid).sorted().joinToString("_") }

    DisposableEffect(conversationId) {
        val registration = firestore.collection(DIRECT_MESSAGES_COLLECTION).whereArrayContains("participants", userUid).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                error = exception.message ?: "Unable to load chat"
                return@addSnapshotListener
            }
            messages = snapshot?.documents.orEmpty().filter { (it.get("participants") as? List<*>)?.contains(other.uid) == true }.map { doc ->
                ChatMessage(doc.id, doc.getString("senderUid").orEmpty(), doc.getString("senderName")?.ifBlank { "User" } ?: "User", doc.getString("message").orEmpty(), doc.getTimestamp("timestamp"))
            }.filter { it.message.isNotBlank() }.sortedWith(compareBy({ it.timestamp?.seconds ?: 0L }, { it.id }))
        }
        onDispose { registration.remove() }
    }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Column { Text(other.name, fontWeight = FontWeight.Bold); Text("Personal chat", fontSize = 10.sp, color = ShahMediumGrey) }
        }
        LazyColumn(Modifier.weight(1f), state = listState, contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(messages, key = { it.id }) { msg ->
                val mine = msg.senderUid == userUid
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = if (mine) ShahGreen else ShahWhite) {
                    Column(Modifier.padding(11.dp)) {
                        Text(msg.message, color = if (mine) ShahWhite else ShahDarkGreen)
                        Text(msg.timestamp?.toDate()?.let { SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(it) }.orEmpty(), color = if (mine) ShahWhite.copy(alpha = .7f) else ShahMediumGrey, fontSize = 9.sp, modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }
        error?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(8.dp)) }
        Surface(color = ShahWhite, shadowElevation = 10.dp) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = text, onValueChange = { text = it; error = null }, modifier = Modifier.weight(1f), placeholder = { Text("Message ${other.name}...") }, singleLine = true, shape = RoundedCornerShape(24.dp))
                Spacer(Modifier.width(6.dp))
                FilledIconButton(enabled = text.trim().isNotBlank() && !sending && userUid.isNotBlank(), onClick = {
                    val value = text.trim()
                    if (value.isBlank()) return@FilledIconButton
                    sending = true
                    firestore.collection(DIRECT_MESSAGES_COLLECTION).add(mapOf("conversationId" to conversationId, "participants" to listOf(userUid, other.uid), "senderUid" to userUid, "senderName" to userName.ifBlank { "User" }, "receiverUid" to other.uid, "message" to value, "timestamp" to Timestamp.now()))
                        .addOnSuccessListener { text = ""; sending = false }
                        .addOnFailureListener { error = it.message ?: "Unable to send message"; sending = false }
                }) {
                    if (sending) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Icon(Icons.AutoMirrored.Filled.Send, "Send")
                }
            }
        }
    }
}
