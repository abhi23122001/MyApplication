package com.shahsurveyors.myapplication.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.theme.*

private data class ProjectItem(val id: String = "", val name: String = "", val client: String = "", val location: String = "", val status: String = "Active")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var projects by remember { mutableStateOf<List<ProjectItem>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadProjects() {
        db.collection("projects").get()
            .addOnSuccessListener { snap ->
                projects = snap.documents.map { d ->
                    ProjectItem(
                        id = d.id,
                        name = d.getString("name") ?: "",
                        client = d.getString("clientName") ?: d.getString("client") ?: "",
                        location = d.getString("location") ?: d.getString("siteLocation") ?: "",
                        status = d.getString("status") ?: "Active"
                    )
                }
            }
            .addOnFailureListener { error = it.message ?: "Unable to load projects" }
    }

    fun addProject() {
        if (name.isBlank()) {
            error = "Project name is required"
            return
        }
        db.collection("projects").document().set(
            mapOf(
                "name" to name.trim(),
                "clientName" to client.trim(),
                "location" to location.trim(),
                "status" to "Active"
            )
        ).addOnSuccessListener {
            name = ""
            client = ""
            location = ""
            error = null
            loadProjects()
        }.addOnFailureListener { error = it.message ?: "Unable to save project" }
    }

    LaunchedEffect(Unit) { loadProjects() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Projects", fontWeight = FontWeight.Bold)
                        Text("Project management & site overview", fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = ::addProject) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ShahDarkGreen)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(projects.size.toString(), style = MaterialTheme.typography.headlineMedium, color = ShahWhite, fontWeight = FontWeight.Bold)
                        Text("Total Projects", color = ShahWhite.copy(alpha = .8f))
                    }
                }
            }
            item {
                Text("Add New Project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Project Name *") }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(client, { client = it }, Modifier.fillMaxWidth(), label = { Text("Client") }, singleLine = true)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("Site Location") }, singleLine = true)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp)) }
            }
            item { Text("All Projects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(projects, key = { it.id }) { project ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(project.name.ifBlank { "Unnamed Project" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (project.client.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(project.client)
                        }
                        if (project.location.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(project.location)
                        }
                        AssistChip(onClick = {}, label = { Text(project.status) })
                    }
                }
            }
        }
    }
}
