package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCommunicationScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    var tab by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("ALL") }
    var expiryDays by remember { mutableStateOf("7") }
    var status by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }

    fun loadHistory() {
        val collection = if (tab == 0) "notifications" else "announcements"
        db.collection(collection)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { snap -> history = snap.documents.map { it.data.orEmpty() } }
            .addOnFailureListener { status = "Unable to load history: ${it.message ?: "error"}" }
    }

    LaunchedEffect(tab) { loadHistory() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communication Center") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Push Notification") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Announcement") })
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Message") },
                    minLines = 4
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Target") },
                    supportingText = { Text("ALL / EMPLOYEE / ADMIN / USER:UID") },
                    singleLine = true
                )
                if (tab == 1) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expiryDays,
                        onValueChange = { expiryDays = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Expiry in days") },
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val cleanTitle = title.trim()
                        val cleanMessage = message.trim()
                        val cleanTarget = target.trim().uppercase().ifBlank { "ALL" }
                        if (cleanTitle.isBlank() || cleanMessage.isBlank()) {
                            status = "Title and message are required"
                            return@Button
                        }
                        if (cleanTarget !in setOf("ALL", "EMPLOYEE", "ADMIN") && !cleanTarget.startsWith("USER:")) {
                            status = "Target must be ALL, EMPLOYEE, ADMIN or USER:UID"
                            return@Button
                        }
                        scope.launch {
                            val collection = if (tab == 0) "notifications" else "announcements"
                            val data = hashMapOf<String, Any>(
                                "type" to if (tab == 0) "ADMIN_PUSH" else "ANNOUNCEMENT",
                                "title" to cleanTitle,
                                "message" to cleanMessage,
                                "target" to cleanTarget,
                                "route" to if (tab == 0) "communication" else "announcements",
                                "createdAt" to FieldValue.serverTimestamp(),
                                "active" to true
                            )
                            if (tab == 1) {
                                data["expiryDays"] = expiryDays.toLongOrNull()?.coerceAtLeast(1L) ?: 7L
                            }
                            db.collection(collection).add(data)
                                .addOnSuccessListener {
                                    status = if (tab == 0) "Notification sent successfully" else "Announcement published successfully"
                                    title = ""
                                    message = ""
                                    loadHistory()
                                }
                                .addOnFailureListener { status = "Save failed: ${it.message ?: "error"}" }
                        }
                    }
                ) { Text(if (tab == 0) "SEND NOTIFICATION" else "PUBLISH ANNOUNCEMENT") }
                if (status.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(status, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                Text("Recent", style = MaterialTheme.typography.titleMedium)
            }
            items(history) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(item["title"]?.toString().orEmpty(), style = MaterialTheme.typography.titleMedium)
                        Text(item["message"]?.toString().orEmpty())
                        Text("Target: ${item["target"]?.toString().orEmpty()}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
