package com.shahsurveyors.myapplication.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.theme.*

private fun value(m: Map<String, Any>, vararg keys: String): Double = keys.firstNotNullOfOrNull { (m[it] as? Number)?.toDouble() } ?: 0.0
private fun text(m: Map<String, Any>, vararg keys: String): String = keys.firstNotNullOfOrNull { m[it]?.toString()?.takeIf(String::isNotBlank) } ?: ""

@Composable
private fun ReportHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ShahDarkGreen); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ShahMediumGrey) }
        TextButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null); Spacer(Modifier.width(4.dp)); Text("Back") }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(15.dp)) { Text(title, style = MaterialTheme.typography.labelMedium, color = ShahMediumGrey); Spacer(Modifier.height(5.dp)); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ShahDarkGreen) } }
}

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
    val received = payments.sumOf { value(it, "amount", "paidAmount") }
    val expense = expenses.sumOf { value(it, "amount") }
    val advance = payments.filter { text(it, "type", "paymentType").contains("advance", true) }.sumOf { value(it, "amount", "paidAmount") }
    val outstanding = projects.sumOf { project ->
        val id = text(project, "id"); val name = text(project, "name", "projectName")
        (value(project, "budget", "amount", "projectAmount", "contractAmount") - payments.filter { text(it, "projectId", "project_id") == id || text(it, "projectName", "project") == name }.sumOf { value(it, "amount", "paidAmount") }).coerceAtLeast(0.0)
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Financial Reports") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ReportHeader("Financial Overview", "Project-wise receivables and cash tracking", onBack) }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MetricCard("Advance", "₹%.0f".format(advance), Modifier.weight(1f)); MetricCard("Received", "₹%.0f".format(received), Modifier.weight(1f)) } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MetricCard("Outstanding", "₹%.0f".format(outstanding), Modifier.weight(1f)); MetricCard("Expenses", "₹%.0f".format(expense), Modifier.weight(1f)) } }
            item { SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) { listOf("Weekly", "Monthly", "Yearly").forEachIndexed { i, option -> SegmentedButton(selected = period == option, onClick = { period = option }, shape = SegmentedButtonDefaults.itemShape(i, 3)) { Text(option) } } } }
            item { Text("Project-wise Payments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ShahDarkGreen) }
            items(projects, key = { "project_${text(it, "id").ifBlank { it.hashCode().toString() }}" }) { project ->
                val id = text(project, "id"); val name = text(project, "name", "projectName").ifBlank { "Project" }
                val pp = payments.filter { text(it, "projectId", "project_id") == id || text(it, "projectName", "project") == name }
                val projectReceived = pp.sumOf { value(it, "amount", "paidAmount") }; val projectAdvance = pp.filter { text(it, "type", "paymentType").contains("advance", true) }.sumOf { value(it, "amount", "paidAmount") }; val total = value(project, "budget", "amount", "projectAmount", "contractAmount")
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Advance: ₹%.0f   •   Received: ₹%.0f".format(projectAdvance, projectReceived)); Text("To receive: ₹%.0f".format((total-projectReceived).coerceAtLeast(0.0)), fontWeight = FontWeight.SemiBold); Text("Next payment: ${pp.firstNotNullOfOrNull { text(it, "nextPaymentDate", "nextDueDate") }.orEmpty().ifBlank { text(project, "nextPaymentDate", "nextDueDate") }.ifBlank { "Not scheduled" }}", style = MaterialTheme.typography.bodySmall) } }
            }
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
        db.collection("tasks").get().addOnSuccessListener { tasks = it.documents.map { d -> d.data.orEmpty() } }.addOnFailureListener { error = it.message ?: "Unable to load tasks" }
    }
    val completedProjects = projects.count { project ->
        val id = text(project, "id"); val name = text(project, "name", "projectName")
        val list = tasks.filter { text(it, "projectId", "project_id") == id || text(it, "projectName", "project") == name }
        list.isNotEmpty() && list.all { text(it, "status").equals("Completed", true) || text(it, "status").equals("Complete", true) }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Work Progress") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ReportHeader("Work Progress Report", "Project-wise completion and pending work", onBack) }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MetricCard("Projects", projects.size.toString(), Modifier.weight(1f)); MetricCard("Completed", completedProjects.toString(), Modifier.weight(1f)) } }
            item { Text("Project Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ShahDarkGreen) }
            items(projects, key = { "work_${text(it, "id").ifBlank { it.hashCode().toString() }}" }) { project ->
                val id = text(project, "id")
                val name = text(project, "name", "projectName").ifBlank { "Project" }
                val list = tasks.filter { text(it, "projectId", "project_id") == id || text(it, "projectName", "project") == name }
                val done = list.count { text(it, "status").equals("Completed", true) || text(it, "status").equals("Complete", true) }
                val progress = if (list.isEmpty()) 0f else (done.toFloat() / list.size.toFloat()).coerceIn(0f, 1f)
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Text("${(progress * 100).toInt()}% complete  •  ${list.size - done} pending")
                        if (list.isNotEmpty() && done == list.size) Text("WORK COMPLETED", fontWeight = FontWeight.Bold, color = ShahGreen)
                    }
                }
            }
            if (projects.isEmpty() && error == null) item { Text("No projects found.", color = ShahMediumGrey) }
            item { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }
}
