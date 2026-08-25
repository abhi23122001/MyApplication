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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    LaunchedEffect(Unit) { viewModel.fetchClients() }

    val filteredClients = remember(viewModel.clients, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) viewModel.clients else viewModel.clients.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.contactPerson.contains(q, ignoreCase = true) ||
                it.email.contains(q, ignoreCase = true) ||
                it.address.contains(q, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Client Directory", fontWeight = FontWeight.Bold, color = ShahWhite)
                        Text("Manage survey clients & contacts", fontSize = 10.sp, color = ShahWhite.copy(.72f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite)
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
            ) { Icon(Icons.Default.PersonAdd, "Add Client") }
        },
        containerColor = ShahGrey
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize().padding(paddingValues).background(ShahGrey)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                placeholder = { Text("Search clients, contact or email") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ShahMediumGrey) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShahGreen,
                    unfocusedBorderColor = ShahLightGrey,
                    focusedContainerColor = ShahWhite,
                    unfocusedContainerColor = ShahWhite
                )
            )

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${filteredClients.size} client${if (filteredClients.size == 1) "" else "s"}", fontWeight = FontWeight.Bold, color = ShahDarkGreen)
                if (searchQuery.isNotBlank()) TextButton(onClick = { searchQuery = "" }) { Text("Clear", color = ShahGreen) }
            }

            when {
                viewModel.isLoading && viewModel.clients.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ShahGreen) }
                }
                filteredClients.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(shape = CircleShape, color = ShahGreen.copy(.10f)) { Icon(Icons.Default.PersonAdd, null, tint = ShahGreen, modifier = Modifier.padding(16.dp).size(30.dp)) }
                            Spacer(Modifier.height(12.dp))
                            Text(if (searchQuery.isBlank()) "No clients found" else "No matching clients", fontWeight = FontWeight.Bold, color = ShahDarkGreen)
                            Text(if (searchQuery.isBlank()) "Add your first client to get started." else "Try a different name or contact.", fontSize = 12.sp, color = ShahMediumGrey)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp)
                    ) {
                        items(filteredClients, key = { it.id }) { client -> ClientCard(client) }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCard(client: ClientModel) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(50.dp), shape = CircleShape, color = ShahGreen.copy(.10f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(client.name.trim().take(1).uppercase(), color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(client.name, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    if (client.contactPerson.isNotBlank()) Text(client.contactPerson, color = ShahGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = { /* Call action can be wired to navigation/intent later. */ }) {
                    Icon(Icons.Default.Phone, "Call", tint = SuccessGreen)
                }
            }
            Spacer(Modifier.height(12.dp))
            ClientDetailRow(Icons.Default.LocationOn, client.address)
            if (client.email.isNotBlank()) ClientDetailRow(Icons.Default.Email, client.email)
        }
    }
}

@Composable
private fun ClientDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    if (text.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Icon(icon, null, tint = ShahMediumGrey, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(9.dp))
        Text(text, color = ShahMediumGrey, fontSize = 12.sp)
    }
}
