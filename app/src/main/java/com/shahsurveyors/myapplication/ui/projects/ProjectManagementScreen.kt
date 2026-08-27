package com.shahsurveyors.myapplication.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

private data class ProjectItem(val id: String = "", val name: String = "", val client: String = "", val location: String = "", val status: String = "Active")

@Composable
fun ProjectManagementScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var items by remember { mutableStateOf<List<ProjectItem>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        db.collection("projects").get().addOnSuccessListener { snap ->
            items = snap.documents.map { d -> ProjectItem(d.id, d.getString("name") ?: "", d.getString("clientName") ?: d.getString("client") ?: "", d.getString("location") ?: "", d.getString("status") ?: "Active") }
        }.addOnFailureListener { error = it.message }
    }
    LaunchedEffect(Unit) { load() }

    Scaffold(topBar = { TopAppBar(title = { Text("Projects") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }, floatingActionButton = {
        FloatingActionButton(onClick = {
            if (name.isBlank()) { error = "Project name is required"; return@FloatingActionButton }
            db.collection("projects").add(mapOf("name" to name.trim(), "clientName" to client.trim(), "location" to location.trim(), "status" to "Active"))
                .addOnSuccessListener { name = ""; client = ""; location = ""; load() }
                .addOnFailureListener { error = it.message }
        }) { Text("+") }
    }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Project Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(client, { client = it }, Modifier.fillMaxWidth(), label = { Text("Client") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("Location") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            items(items) { item ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    if (item.client.isNotBlank()) Text("Client: ${item.client}")
                    if (item.location.isNotBlank()) Text("Location: ${item.location}")
                    Text("Status: ${item.status}")
                } }
            }
        }
    }
}
