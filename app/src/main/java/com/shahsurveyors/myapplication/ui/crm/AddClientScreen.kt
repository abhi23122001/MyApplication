package com.shahsurveyors.myapplication.ui.crm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.models.ClientModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientScreen(viewModel: ClientViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var gst by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Client") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            item {
                Text("Client / Company Details", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Company / Client Name *") })
                OutlinedTextField(contact, { contact = it }, Modifier.fillMaxWidth(), label = { Text("Contact Person") })
                OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Mobile / Phone") })
                OutlinedTextField(whatsapp, { whatsapp = it }, Modifier.fillMaxWidth(), label = { Text("WhatsApp") })
                OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") })
                OutlinedTextField(address, { address = it }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("Company Address") })
                OutlinedTextField(city, { city = it }, Modifier.fillMaxWidth(), label = { Text("City") })
                OutlinedTextField(state, { state = it }, Modifier.fillMaxWidth(), label = { Text("State") })
                OutlinedTextField(pincode, { pincode = it }, Modifier.fillMaxWidth(), label = { Text("Pincode") })
                OutlinedTextField(gst, { gst = it }, Modifier.fillMaxWidth(), label = { Text("GSTIN") })
                OutlinedTextField(pan, { pan = it }, Modifier.fillMaxWidth(), label = { Text("PAN") })
                OutlinedTextField(website, { website = it }, Modifier.fillMaxWidth(), label = { Text("Website") })
                OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("Notes") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(6.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (name.isBlank()) {
                            error = "Company / Client Name is required"
                            return@Button
                        }
                        viewModel.saveClient(
                            ClientModel(
                                name = name.trim(), contactPerson = contact.trim(), email = email.trim(),
                                phone = phone.trim(), whatsapp = whatsapp.trim(), address = address.trim(),
                                city = city.trim(), state = state.trim(), pincode = pincode.trim(),
                                gstin = gst.trim().ifBlank { null }, pan = pan.trim(),
                                website = website.trim(), notes = notes.trim()
                            )
                        )
                        onSaved()
                    }
                ) { Text("Save Client") }
            }
        }
    }
}
