package com.shahsurveyors.myapplication.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var period by remember { mutableStateOf("Monthly") }
    var income by remember { mutableStateOf(0.0) }
    var expense by remember { mutableStateOf(0.0) }
    var error by remember { mutableStateOf<String?>(null) }
    fun load() {
        val now = Calendar.getInstance()
        when (period) { "Weekly" -> now.add(Calendar.DAY_OF_YEAR, -7); "Yearly" -> now.add(Calendar.YEAR, -1); else -> now.add(Calendar.MONTH, -1) }
        val from = now.time
        db.collection("expenses").get().addOnSuccessListener { s -> expense = s.documents.sumOf { it.getDouble("amount") ?: 0.0 } }.addOnFailureListener { error = it.message }
        db.collection("invoices").get().addOnSuccessListener { s -> income = s.documents.sumOf { (it.getDouble("total") ?: it.getDouble("amount") ?: 0.0) } }.addOnFailureListener { error = it.message }
    }
    LaunchedEffect(period) { load() }
    val profit = income - expense
    Scaffold(topBar = { TopAppBar(title = { Text("Financial Reports") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Weekly", "Monthly", "Yearly").forEach { TextButton(onClick = { period = it }) { Text(if (period == it) "[$it]" else it) } } } }
            item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("$period Summary", style = MaterialTheme.typography.titleLarge); Text("Income: ₹%.2f".format(income)); Text("Expenses: ₹%.2f".format(expense)); Text("Estimated Profit: ₹%.2f".format(profit)) } } }
            item { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkProgressReportScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var total by remember { mutableStateOf(0) }
    var completed by remember { mutableStateOf(0) }
    var inProgress by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { db.collection("tasks").get().addOnSuccessListener { s -> total = s.size(); completed = s.documents.count { (it.getString("status") ?: "").equals("Completed", true) }; inProgress = s.documents.count { (it.getString("status") ?: "").equals("In Progress", true) } }.addOnFailureListener { error = it.message } }
    Scaffold(topBar = { TopAppBar(title = { Text("Work Progress") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Work Progress Summary", style = MaterialTheme.typography.titleLarge); Text("Total tasks: $total"); Text("Completed: $completed"); Text("In Progress: $inProgress") } }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
