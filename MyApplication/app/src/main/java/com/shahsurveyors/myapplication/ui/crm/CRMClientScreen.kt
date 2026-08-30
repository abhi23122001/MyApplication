package com.shahsurveyors.myapplication.ui.crm

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Client(
    val id: String = "",
    val name: String,
    val phone: String,
    val company: String,
    val email: String,
    val location: String,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CRMClientScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val clientList = remember { mutableStateListOf<Client>() }
    var showAddClientDialog by remember { mutableStateOf(false) }

    fun loadClients() {
        coroutineScope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("clients").get().await()
                clientList.clear()
                if (snapshot.isEmpty) {
                    clientList.add(
                        Client(
                            id = "1",
                            name = "Aditya Singh",
                            phone = "+91 9876543210",
                            company = "NTPC Singrauli",
                            email = "aditya@ntpc.co.in",
                            location = "Shakti Nagar",
                            notes = "Regular Survey Client"
                        )
                    )
                    clientList.add(
                        Client(
                            id = "2",
                            name = "Rajesh Verma",
                            phone = "+91 8877665544",
                            company = "Northern Coalfields",
                            email = "rajesh@ncl.gov.in",
                            location = "Jayant Project",
                            notes = "Soil Testing requirement"
                        )
                    )
                } else {
                    for (doc in snapshot.documents) {
                        clientList.add(
                            Client(
                                id = doc.id,
                                name = doc.getString("name") ?: "Client",
                                phone = doc.getString("phone") ?: "",
                                company = doc.getString("company") ?: "",
                                email = doc.getString("email") ?: "",
                                location = doc.getString("location") ?: "",
                                notes = doc.getString("notes") ?: ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadClients()
    }

    val filteredClients = remember(clientList, searchQuery) {
        if (searchQuery.isBlank()) {
            clientList.toList()
        } else {
            clientList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.company.contains(searchQuery, ignoreCase = true) ||
                        it.location.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clients CRM & Directory",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddClientDialog = true },
                containerColor = ShahGreen,
                contentColor = ShahWhite
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Client")
            }
        },
        containerColor = ShahGrey
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShahGrey)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by client name, company, site...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShahGreen,
                    unfocusedBorderColor = ShahMediumGrey,
                    focusedContainerColor = ShahWhite,
                    unfocusedContainerColor = ShahWhite
                )
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (filteredClients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No clients found", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredClients, key = { it.id }) { client ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ShahWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape),
                                        color = ShahGreen.copy(alpha = 0.12f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = client.name.firstOrNull()?.uppercase() ?: "C",
                                                fontWeight = FontWeight.Bold,
                                                color = ShahGreen,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = client.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = ShahBlack
                                        )
                                        Text(
                                            text = client.company,
                                            fontSize = 13.sp,
                                            color = ShahDarkGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (client.phone.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${client.phone}")
                                                }
                                                context.startActivity(intent)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = "Call",
                                                tint = SuccessGreen
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = ShahLightGrey
                                )

                                if (client.email.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = ShahMediumGrey, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = client.email, fontSize = 12.sp, color = ShahDarkGrey)
                                    }
                                }

                                if (client.location.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ShahMediumGrey, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = client.location, fontSize = 12.sp, color = ShahDarkGrey)
                                    }
                                }

                                if (client.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Note: ${client.notes}",
                                        fontSize = 11.sp,
                                        color = ShahMediumGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddClientDialog) {
        var name by remember { mutableStateOf("") }
        var company by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddClientDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShahWhite,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Add New Client", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShahBlack)

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Client Name *") }, singleLine = true)
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company / Org *") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true)
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Site / Location") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Remarks / Project Scope") }, singleLine = true)

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && company.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val newDoc = firestore.collection("clients").document()
                                        val data = hashMapOf(
                                            "name" to name.trim(),
                                            "company" to company.trim(),
                                            "phone" to phone.trim(),
                                            "email" to email.trim(),
                                            "location" to location.trim(),
                                            "notes" to notes.trim(),
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        newDoc.set(data).await()
                                        Toast.makeText(context, "Client added", Toast.LENGTH_SHORT).show()
                                        showAddClientDialog = false
                                        loadClients()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE CLIENT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}