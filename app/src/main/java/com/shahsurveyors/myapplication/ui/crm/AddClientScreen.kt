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
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text("Add Client") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Client / Company Details", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Company / Client Name *") })
                OutlinedTextField(contactPerson, { contactPerson = it }, Modifier.fillMaxWidth(), label = { Text("Contact Person") })
                OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Mobile / Phone") })
                OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") })
                OutlinedTextField(address, { address = it }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("Company Address") })
                OutlinedTextField(gstin, { gstin = it }, Modifier.fillMaxWidth(), label = { Text("GSTIN / Tax ID") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(8.dp))
                Button(Modifier.fillMaxWidth(), onClick = {
                    if (name.isBlank()) { error = "Company / Client Name is required"; return@Button }
                    viewModel.addClient(ClientModel(name = name.trim(), contactPerson = contactPerson.trim(), email = email.trim(), phone = phone.trim(), address = address.trim(), gstin = gstin.trim().ifBlank { null }))
                    onSaved()
                }) { Text("Save Client") }
            }
        }
    }
}
