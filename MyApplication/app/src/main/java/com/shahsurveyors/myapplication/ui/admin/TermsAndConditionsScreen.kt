package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.data.local.TermConditionEntity
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.theme.DeepMidnightSlate
import com.shahsurveyors.myapplication.ui.theme.ElectricGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val terms by viewModel.allTerms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTermContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnightSlate,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = ElectricGold) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(DeepMidnightSlate).padding(16.dp)) {
            if (terms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No terms added yet", color = Color.Gray)
                }
            }
            
            LazyColumn {
                items(terms) { term ->
                    GlassCard(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(term.content, color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            IconButton(onClick = { viewModel.deleteTerm(term) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Term / Condition") },
            text = {
                OutlinedTextField(
                    value = newTermContent,
                    onValueChange = { newTermContent = it },
                    label = { Text("Condition Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newTermContent.isNotEmpty()) {
                        viewModel.saveTerm(TermConditionEntity(content = newTermContent))
                        newTermContent = ""
                        showAddDialog = false
                    }
                }) {
                    Text("ADD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("CANCEL") }
            }
        )
    }
}
