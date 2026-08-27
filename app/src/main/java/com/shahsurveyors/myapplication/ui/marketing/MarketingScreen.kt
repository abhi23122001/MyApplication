package com.shahsurveyors.myapplication.ui.marketing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

private data class MarketingItem(val id: String = "", val title: String = "", val client: String = "", val status: String = "Active", val notes: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var items by remember { mutableStateOf<List<MarketingItem>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        db.collection("marketing").get().addOnSuccessListener { snap ->
            items = snap.documents.map { d -> MarketingItem(d.id, d.getString("title") ?: "", d.getString("client") ?: "", d.getString("status") ?: "Active", d.getString("notes") ?: "") }
        }.addOnFailureListener { error = it.message ?: "Unable to load marketing data" }
    }
    LaunchedEffect(Unit) { load() }

    Scaffold(topBar = { TopAppBar(title = { Text("Marketing") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }, floatingActionButton = {
        FloatingActionButton(onClick = {
            if (title.isBlank()) { error = "Title is required"; return@FloatingActionButton }
            db.collection("marketing").document().set(mapOf("title" to title.trim(), "client" to client.trim(), "notes" to notes.trim(), "status" to "Active"))
                .addOnSuccessListener { title = ""; client = ""; notes = ""; error = null; load() }
                .addOnFailureListener { error = it.message ?: "Unable to save marketing activity" }
        }) { Text("+") }
    }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Campaign / Activity") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(client, { client = it }, Modifier.fillMaxWidth(), label = { Text("Client") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Notes") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            items(items) { item ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    if (item.client.isNotBlank()) Text("Client: ${item.client}")
                    Text("Status: ${item.status}")
                    if (item.notes.isNotBlank()) Text(item.notes)
                } }
            }
        }
    }
}
