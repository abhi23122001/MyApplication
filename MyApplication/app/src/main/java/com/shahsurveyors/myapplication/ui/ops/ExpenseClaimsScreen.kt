package com.shahsurveyors.myapplication.ui.ops

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseClaimsScreen() {
    var category by remember { mutableStateOf("Fuel") }
    var amount by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reimbursement Claims") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Submit New Claim", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (INR)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text("Category", style = MaterialTheme.typography.labelSmall)
                val categories = listOf("Fuel", "Food/DA", "Travel", "Equipment")
                ScrollableTabRow(selectedTabIndex = categories.indexOf(category), edgePadding = 0.dp) {
                    categories.forEach { cat ->
                        Tab(selected = category == cat, onClick = { category = cat }, text = { Text(cat) })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { /* Upload Receipt */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("CAPTURE RECEIPT & SUBMIT")
                }
            }
        }
    }
}
