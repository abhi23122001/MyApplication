package com.shahsurveyors.myapplication.ui.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.ClientModel
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CRMClientScreen(
    viewModel: ClientViewModel,
    onBack: () -> Unit = {},
    onAddClient: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchClients()
    }

    val filtered = remember(viewModel.clients, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            viewModel.clients.toList()
        } else {
            viewModel.clients.filter {
                it.name.contains(query, true) ||
                    it.contactPerson.contains(query, true) ||
                    it.email.contains(query, true) ||
                    it.phone.contains(query) ||
                    it.whatsapp.contains(query) ||
                    it.address.contains(query, true) ||
                    it.city.contains(query, true) ||
                    it.state.contains(query, true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Client Directory", fontWeight = FontWeight.Bold, color = ShahWhite)
                        Text("Manage survey clients & contacts", fontSize = 10.sp, color = ShahWhite.copy(alpha = 0.72f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClient,
                containerColor = ShahGreen,
                contentColor = ShahWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Client")
            }
        },
        containerColor = ShahGrey
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(ShahGrey)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
                placeholder = { Text("Search client, contact, phone or city") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ShahMediumGrey) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShahGreen,
                    unfocusedBorderColor = ShahLightGrey,
                    focusedContainerColor = ShahWhite,
                    unfocusedContainerColor = ShahWhite
                )
            )
            Text(
                "${filtered.size} client${if (filtered.size == 1) "" else "s"}",
                fontWeight = FontWeight.Bold,
                color = ShahDarkGreen,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No clients found", fontWeight = FontWeight.Bold, color = ShahDarkGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 90.dp)
                ) {
                    items(filtered, key = { it.id }) { client ->
                        ClientCard(client)
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCard(client: ClientModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(50.dp), shape = CircleShape, color = ShahGreen.copy(alpha = 0.10f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(client.name.take(1).uppercase(), color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(client.name, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    if (client.contactPerson.isNotBlank()) Text(client.contactPerson, color = ShahGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(10.dp))
            ClientDetailRow(Icons.Default.Phone, client.phone)
            ClientDetailRow(Icons.Default.Phone, if (client.whatsapp.isNotBlank()) "WhatsApp: ${client.whatsapp}" else "")
            ClientDetailRow(Icons.Default.Email, client.email)
            ClientDetailRow(Icons.Default.LocationOn, listOf(client.address, client.city, client.state, client.pincode).filter { it.isNotBlank() }.joinToString(", "))
            if (!client.gstin.isNullOrBlank()) Text("GSTIN: ${client.gstin}", fontSize = 11.sp, color = ShahMediumGrey, modifier = Modifier.padding(top = 5.dp))
            if (client.pan.isNotBlank()) Text("PAN: ${client.pan}", fontSize = 11.sp, color = ShahMediumGrey)
            if (client.website.isNotBlank()) Text("Website: ${client.website}", fontSize = 11.sp, color = ShahGreen)
            if (client.notes.isNotBlank()) Text(client.notes, fontSize = 11.sp, color = ShahMediumGrey, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun ClientDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    if (text.isNotBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
            Icon(icon, contentDescription = null, tint = ShahMediumGrey, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(9.dp))
            Text(text, color = ShahMediumGrey, fontSize = 12.sp)
        }
    }
}
