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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberLauncherForActivityResult
import androidx.compose.runtime.setValue
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
import com.shahsurveyors.myapplication.ui.theme.ErrorRed
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite
import com.shahsurveyors.myapplication.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TEAM_MESSAGES_COLLECTION = "team_messages"
private const val DIRECT_MESSAGES_COLLECTION = "direct_messages"
private const val USERS_COLLECTION = "users"

data class StaffLocation(
    val uid: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val sharing: Boolean,
    val lastUpdate: Date?
)

data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val sender: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null
)

data class ChatUser(val uid: String, val name: String, val email: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    userUid: String = "",
    userName: String = "User",
    isAdmin: Boolean = false,
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LIVE RADAR", "TEAM CHAT", "PERSONAL CHAT")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Team Sync", fontWeight = FontWeight.Bold, color = ShahWhite)
                            Text(
                                "Live team location & communication",
                                fontSize = 10.sp,
                                color = ShahWhite.copy(alpha = 0.72f)
                            )
                        }
                    },
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
                    indicator = { positions ->
                        if (selectedTab in positions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                                color = ShahGreen
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ShahGrey)
        ) {
            when (selectedTab) {
                0 -> LiveRadarView(userUid = userUid, isAdmin = isAdmin)
                1 -> TeamChatView(userUid = userUid, userName = userName)
                else -> PersonalChatView(userUid = userUid, userName = userName)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LiveRadarView(userUid: String, isAdmin: Boolean) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val locationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var staff by remember { mutableStateOf<List<StaffLocation>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var sharing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        sharing = granted
        if (!granted) {
            error = "Location permission is required to share your live location."
        }
    }

    DisposableEffect(Unit) {
        val registration: ListenerRegistration = firestore
            .collection(USERS_COLLECTION)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    error = exception.message ?: "Unable to load team locations"
                    return@addSnapshotListener
                }

                staff = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val latitude = doc.getDouble("latitude") ?: doc.getDouble("lat")
                    val longitude = doc.getDouble("longitude") ?: doc.getDouble("lon")
                    val email = doc.getString("email").orEmpty()
                    val name = doc.getString("name")?.takeIf { it.isNotBlank() } ?: email.ifBlank { "Employee" }
                    val active = doc.getBoolean("active") ?: true
                    if (!active || latitude == null || longitude == null) {
                        null
                    } else {
                        StaffLocation(
                            uid = doc.id,
                            name = name,
                            lat = latitude,
                            lon = longitude,
                            sharing = doc.getBoolean("locationSharing") == true,
                            lastUpdate = doc.getTimestamp("lastLocationAt")?.toDate()
                        )
                    }
                }
                    .filter { it.uid != userUid || isAdmin }
                    .sortedBy { it.name.lowercase(Locale.ENGLISH) }
            }
        onDispose { registration.remove() }
    }

    DisposableEffect(sharing, userUid) {
        if (!sharing || userUid.isBlank()) {
            onDispose { }
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                onDispose { }
            } else {
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    15_000L
                ).setMinUpdateIntervalMillis(5_000L).build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation ?: return
                        firestore.collection(USERS_COLLECTION).document(userUid).set(
                            mapOf(
                                "latitude" to location.latitude,
                                "longitude" to location.longitude,
                                "locationSharing" to true,
                                "lastLocationAt" to Timestamp.now()
                            ),
                            SetOptions.merge()
                        )
                    }
                }

                locationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                firestore.collection(USERS_COLLECTION).document(userUid).set(
                    mapOf("locationSharing" to true, "lastLocationAt" to Timestamp.now()),
                    SetOptions.merge()
                )

                onDispose {
                    locationClient.removeLocationUpdates(callback)
                    firestore.collection(USERS_COLLECTION).document(userUid).set(
                        mapOf("locationSharing" to false, "lastLocationAt" to Timestamp.now()),
                        SetOptions.merge()
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ShahWhite)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (sharing) "Live location is ON" else "Live location is OFF",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Your location is shared with Admin only while enabled.",
                                fontSize = 10.sp,
                                color = ShahMediumGrey
                            )
                        }
                        Button(
                            onClick = {
                                if (sharing) {
                                    sharing = false
                                } else {
                                    val fine = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    val coarse = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (fine || coarse) {
                                        sharing = true
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sharing) ErrorRed else ShahGreen
                            )
                        ) {
                            Text(if (sharing) "STOP" else "START")
                        }
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ShahGreen.copy(alpha = 0.07f)
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Radar, contentDescription = null, tint = ShahGreen, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("Team locations", fontWeight = FontWeight.Bold, color = ShahDarkGreen)
                        Text(
                            if (isAdmin) "Employees sharing location appear here."
                            else "Only employees currently sharing are shown.",
                            fontSize = 10.sp,
                            color = ShahMediumGrey
                        )
                    }
                }
            }
        }

        error?.let { message ->
            item { Text(message, color = ErrorRed, fontSize = 11.sp) }
        }

        if (staff.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No live employee locations", color = ShahMediumGrey)
                }
            }
        }

        items(staff, key = { it.uid }) { person ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ShahWhite)
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (person.sharing) Icons.Default.PersonPinCircle else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (person.sharing) SuccessGreen else ShahMediumGrey,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(person.name, fontWeight = FontWeight.Bold)
                        Text(
                            if (person.sharing) "LIVE" else "Last location",
                            color = if (person.sharing) SuccessGreen else ShahMediumGrey,
                            fontSize = 10.sp
                        )
                        person.lastUpdate?.let { date ->
                            Text(
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(date),
                                fontSize = 9.sp,
                                color = ShahMediumGrey
                            )
                        }
                    }
                    FilledIconButton(
                        onClick = {
                            val uri = Uri.parse(
                                "geo:${person.lat},${person.lon}?q=${person.lat},${person.lon}(${Uri.encode(person.name)})"
                            )
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }.onFailure {
                                error = "No map application is available."
                            }
                        }
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Open in Maps", tint = ShahGreen)
                    }
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
        val registration: ListenerRegistration = firestore
            .collection(TEAM_MESSAGES_COLLECTION)
            .limitToLast(200)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    error = exception.message ?: "Unable to load team chat"
                    return@addSnapshotListener
                }
                messages = snapshot?.documents.orEmpty()
                    .map { doc ->
                        ChatMessage(
                            id = doc.id,
                            senderUid = doc.getString("senderUid").orEmpty(),
                            sender = doc.getString("senderName")?.ifBlank { "User" } ?: "User",
                            message = doc.getString("message").orEmpty(),
                            timestamp = doc.getTimestamp("timestamp")
                        )
                    }
                    .filter { it.message.isNotBlank() }
                    .sortedWith(compareBy({ it.timestamp?.seconds ?: 0L }, { it.id }))
            }
        onDispose { registration.remove() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    ChatComposer(
        messages = messages,
        listState = listState,
        userUid = userUid,
        text = text,
        onText = {
            text = it
            error = null
        },
        isSending = isSending,
        error = error,
        onSend = { value ->
            if (userUid.isBlank()) {
                error = "You must be logged in to send messages."
            } else {
                isSending = true
                firestore.collection(TEAM_MESSAGES_COLLECTION)
                    .add(
                        mapOf(
                            "senderUid" to userUid,
                            "senderName" to userName.ifBlank { "User" },
                            "message" to value,
                            "timestamp" to Timestamp.now()
                        )
                    )
                    .addOnSuccessListener {
                        text = ""
                        isSending = false
                    }
                    .addOnFailureListener {
                        error = it.message ?: "Unable to send message"
                        isSending = false
                    }
            }
        }
    )
}

@Composable
private fun ChatComposer(
    messages: List<ChatMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    userUid: String,
    text: String,
    onText: (String) -> Unit,
    isSending: Boolean,
    error: String?,
    onSend: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val mine = msg.senderUid == userUid
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (mine) ShahGreen else ShahWhite
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            msg.sender,
                            color = if (mine) ShahWhite else ShahGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            msg.message,
                            color = if (mine) ShahWhite else ShahWhite.copy(alpha = 0.05f).let { ShahGreen },
                            fontSize = 13.sp
                        )
                        Text(
                            msg.timestamp?.toDate()?.let {
                                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(it)
                            }.orEmpty(),
                            fontSize = 9.sp,
                            color = if (mine) ShahWhite.copy(alpha = 0.7f) else ShahMediumGrey,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        error?.let {
            Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        }

        Surface(color = ShahWhite, shadowElevation = 10.dp) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onText,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(6.dp))
                FilledIconButton(
                    enabled = text.trim().isNotBlank() && !isSending && userUid.isNotBlank(),
                    onClick = { onSend(text.trim()) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
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
            users = snapshot?.documents.orEmpty()
                .mapNotNull { doc ->
                    if (doc.id == userUid || doc.getBoolean("active") == false) return@mapNotNull null
                    val email = doc.getString("email").orEmpty()
                    val name = doc.getString("name")?.takeIf { it.isNotBlank() } ?: email.ifBlank { "Employee" }
                    ChatUser(doc.id, name, email)
                }
                .distinctBy { it.uid }
                .sortedBy { it.name.lowercase(Locale.ENGLISH) }
        }
        onDispose { registration.remove() }
    }

    val selectedUser = selected
    if (selectedUser == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Personal Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ShahDarkGreen)
            }
            error?.let { message ->
                item { Text(message, color = ErrorRed, fontSize = 11.sp) }
            }
            if (users.isEmpty()) {
                item { Text("No employees available", color = ShahMediumGrey) }
            }
            items(users, key = { it.uid }) { user ->
                Card(
                    onClick = { selected = user },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ShahWhite)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ShahGreen)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(user.name, fontWeight = FontWeight.Bold)
                            Text(user.email, fontSize = 10.sp, color = ShahMediumGrey)
                        }
                    }
                }
            }
        }
    } else {
        DirectConversation(
            userUid = userUid,
            userName = userName,
            other = selectedUser,
            onBack = { selected = null }
        )
    }
}

@Composable
private fun DirectConversation(
    userUid: String,
    userName: String,
    other: ChatUser,
    onBack: () -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val listState = rememberLazyListState()
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }
    val conversationId = remember(userUid, other.uid) {
        listOf(userUid, other.uid).sorted().joinToString("_")
    }

    DisposableEffect(conversationId) {
        val registration = firestore
            .collection(DIRECT_MESSAGES_COLLECTION)
            .whereArrayContains("participants", userUid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    error = exception.message ?: "Unable to load chat"
                    return@addSnapshotListener
                }
                messages = snapshot?.documents.orEmpty()
                    .filter { (it.get("participants") as? List<*>)?.contains(other.uid) == true }
                    .map { doc ->
                        ChatMessage(
                            id = doc.id,
                            senderUid = doc.getString("senderUid").orEmpty(),
                            sender = doc.getString("senderName")?.ifBlank { "User" } ?: "User",
                            message = doc.getString("message").orEmpty(),
                            timestamp = doc.getTimestamp("timestamp")
                        )
                    }
                    .filter { it.message.isNotBlank() }
                    .sortedWith(compareBy({ it.timestamp?.seconds ?: 0L }, { it.id }))
            }
        onDispose { registration.remove() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(other.name, fontWeight = FontWeight.Bold)
                Text("Personal chat", fontSize = 10.sp, color = ShahMediumGrey)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val mine = msg.senderUid == userUid
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (mine) ShahGreen else ShahWhite
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        Text(
                            msg.message,
                            color = if (mine) ShahWhite else ShahGreen
                        )
                        Text(
                            msg.timestamp?.toDate()?.let {
                                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(it)
                            }.orEmpty(),
                            color = if (mine) ShahWhite.copy(alpha = 0.7f) else ShahMediumGrey,
                            fontSize = 9.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        error?.let {
            Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
        }

        Surface(color = ShahWhite, shadowElevation = 10.dp) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = null
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message ${other.name}...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(6.dp))
                FilledIconButton(
                    enabled = text.trim().isNotBlank() && !sending && userUid.isNotBlank(),
                    onClick = {
                        val value = text.trim()
                        if (value.isBlank()) return@FilledIconButton
                        sending = true
                        firestore.collection(DIRECT_MESSAGES_COLLECTION)
                            .add(
                                mapOf(
                                    "conversationId" to conversationId,
                                    "participants" to listOf(userUid, other.uid),
                                    "senderUid" to userUid,
                                    "senderName" to userName.ifBlank { "User" },
                                    "receiverUid" to other.uid,
                                    "message" to value,
                                    "timestamp" to Timestamp.now()
                                )
                            )
                            .addOnSuccessListener {
                                text = ""
                                sending = false
                            }
                            .addOnFailureListener {
                                error = it.message ?: "Unable to send message"
                                sending = false
                            }
                    }
                ) {
                    if (sending) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
