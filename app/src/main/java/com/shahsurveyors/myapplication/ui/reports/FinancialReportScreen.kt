package com.shahsurveyors.myapplication.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

private fun value(m: Map<String, Any>, vararg keys: String): Double = keys.firstNotNullOfOrNull { (m[it] as? Number)?.toDouble() } ?: 0.0
private fun text(m: Map<String, Any>, vararg keys: String): String = keys.firstNotNullOfOrNull { m[it]?.toString()?.takeIf { s -> s.isNotBlank() } } ?: ""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var period by remember { mutableStateOf("Monthly") }
    var projects by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var payments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var expenses by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        db.collection("projects").get().addOnSuccessListener { projects = it.documents.map { d -> d.data.orEmpty() + mapOf("id" to d.id) } }.addOnFailureListener { error = it.message ?: "Unable to load projects" }
        db.collection("payments").get().addOnSuccessListener { payments = it.documents.map { d -> d.data.orEmpty() } }.addOnFailureListener { error = it.message ?: "Unable to load payments" }
        db.collection("expenses").get().addOnSuccessListener { expenses = it.documents.map { d -> d.data.orEmpty() } }.addOnFailureListener { error = it.message ?: "Unable to load expenses" }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Financial Reports") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Weekly", "Monthly", "Yearly").forEach { TextButton(onClick = { period = it }) { Text(if (period == it) "[$it]" else it) } } } }
            item { Text("Project-wise Receivables", style = MaterialTheme.typography.titleLarge) }
            items(projects) { project ->
                val id = text(project, "id")
                val name = text(project, "name", "projectName").ifBlank { "Project" }
                val pp = payments.filter { text(it, "projectId", "project_id") == id || text(it, "projectName", "project") == name }
                val advance = pp.filter { text(it, "type", "paymentType").contains("advance", true) }.sumOf { value(it, "amount", "paidAmount") }
                val received = pp.sumOf { value(it, "amount", "paidAmount") }
                val total = value(project, "budget", "amount", "projectAmount", "contractAmount")
                val outstanding = (total - received).coerceAtLeast(0.0)
                val nextDate = pp.firstNotNullOfOrNull { text(it, "nextPaymentDate", "nextDueDate") }.orEmpty().ifBlank { text(project, "nextPaymentDate", "nextDueDate") }
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text("Advance received: ₹%.2f".format(advance))
                    Text("Total received: ₹%.2f".format(received))
                    Text("Payment to receive: ₹%.2f".format(outstanding))
                    Text("Next payment promised: ${nextDate.ifBlank { "Not set" }}")
                } }
            }
            item { val received = payments.sumOf { value(it, "amount", "paidAmount") }; val expense = expenses.sumOf { value(it, "amount") }; Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("$period Overall", style = MaterialTheme.typography.titleLarge); Text("Payments received: ₹%.2f".format(received)); Text("Expenses: ₹%.2f".format(expense)); Text("Net tracked cash: ₹%.2f".format(received - expense)) } } }
            item { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkProgressReportScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var projects by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var tasks by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        db.collection("projects").get().addOnSuccessListener { projects = it.documents.map { d -> d.data.orEmpty() + mapOf("id" to d.id) } }.addOnFailureListener { error = it.message ?: "Unable to load projects" }
        db.collection("tasks").get().addOnSuccessListener { tasks = it.documents.map { d -> d.data.orEmpty() } }.addOnFailureListener { error = it.message ?: "Unable to load work" }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Work Progress") }, navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }) }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Project-wise Work Report", style = MaterialTheme.typography.titleLarge) }
            items(projects) { project ->
                val id = text(project, "id")
                val name = text(project, "name", "projectName").ifBlank { "Project" }
                val list = tasks.filter { text(it, "projectId", "project_id") == id || text(it, "projectName", "project") == name }
                val completed = list.count { text(it, "status").equals("Completed", true) || text(it, "status").equals("Complete", true) }
                val inProgress = list.count { text(it, "status").contains("progress", true) }
                val pending = (list.size - completed - inProgress).coerceAtLeast(0)
                val percent = if (list.isEmpty()) 0 else completed * 100 / list.size
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text("Total work: ${list.size}")
                    Text("Completed: $completed ($percent%)")
                    Text("In Progress: $inProgress")
                    Text("Pending: $pending")
                    if (list.isNotEmpty() && completed == list.size) Text("WORK COMPLETED")
                } }
            }
            item { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }
}
