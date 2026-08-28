package com.shahsurveyors.myapplication.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.TaskModel
import com.shahsurveyors.myapplication.models.UserProfile
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    viewModel: TaskViewModel,
    uid: String,
    isAdmin: Boolean = false,
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTask by remember { mutableStateOf(false) }
    val tabs = if (isAdmin) listOf("OPEN", "COMPLETED", "ALL") else listOf("OPEN", "COMPLETED")

    LaunchedEffect(uid, isAdmin) {
        viewModel.fetchTasks(uid, isAdmin)
        if (isAdmin) viewModel.loadAssignmentData()
    }

    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage != null) {
            // Error is displayed below; clear it on the next successful reload.
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Task Hub", fontWeight = FontWeight.Bold, color = ShahWhite)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ShahWhite,
                    contentColor = ShahGreen
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddTask = true },
                    containerColor = ShahGreen,
                    contentColor = ShahWhite
                ) {
                    Icon(Icons.Default.Add, "Assign Task")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
        ) {
            if (viewModel.errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    color = ErrorRed.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = viewModel.errorMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = ErrorRed,
                        fontSize = 12.sp
                    )
                }
            }

            val filteredTasks = when {
                selectedTab == 0 -> viewModel.tasks.filter { it.status.uppercase() != "COMPLETED" }
                selectedTab == 1 -> viewModel.tasks.filter { it.status.uppercase() == "COMPLETED" }
                else -> viewModel.tasks
            }

            if (viewModel.isLoading && filteredTasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (filteredTasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks available", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            isAdmin = isAdmin,
                            onStatusChange = { status ->
                                viewModel.updateStatus(task.id, status, uid, isAdmin)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddTask) {
        AddTaskDialog(
            employees = viewModel.employees,
            projects = viewModel.projects,
            isSaving = viewModel.isSaving,
            onDismiss = { if (!viewModel.isSaving) showAddTask = false },
            onSave = { task ->
                viewModel.saveTask(task, uid, true)
                showAddTask = false
            }
        )
    }
}

@Composable
fun TaskCard(
    task: TaskModel,
    isAdmin: Boolean,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(task.title, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(Modifier.width(8.dp))
                        PriorityBadge(task.priority)
                    }
                    Spacer(Modifier.height(5.dp))
                    if (task.description.isNotBlank()) {
                        Text(task.description, color = ShahMediumGrey, fontSize = 13.sp)
                        Spacer(Modifier.height(5.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), tint = ShahMediumGrey)
                        Spacer(Modifier.width(4.dp))
                        Text(task.projectName.ifBlank { "No project" }, color = ShahMediumGrey, fontSize = 12.sp)
                    }
                    if (isAdmin && task.assignedToName.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Assigned to: ${task.assignedToName}", color = ShahMediumGrey, fontSize = 12.sp)
                    }
                }

                val dateStr = task.dueDate?.let {
                    SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(it.toDate())
                } ?: "No due date"
                Text(dateStr, color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(Modifier.height(10.dp))
            StatusBadge(task.status)

            if (!isAdmin && task.status.uppercase() != "COMPLETED") {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (task.status.uppercase() == "OPEN") {
                        OutlinedButton(onClick = { onStatusChange("IN_PROGRESS") }, modifier = Modifier.weight(1f)) {
                            Text("START", fontSize = 11.sp)
                        }
                    }
                    if (task.status.uppercase() == "OPEN" || task.status.uppercase() == "IN_PROGRESS") {
                        OutlinedButton(onClick = { onStatusChange("BLOCKED") }, modifier = Modifier.weight(1f)) {
                            Text("BLOCK", fontSize = 11.sp)
                        }
                    }
                    if (task.status.uppercase() == "IN_PROGRESS" || task.status.uppercase() == "BLOCKED") {
                        Button(
                            onClick = { onStatusChange("COMPLETED") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("COMPLETE", fontSize = 11.sp, color = ShahWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val normalized = status.uppercase()
    val badgeColor = when (normalized) {
        "COMPLETED" -> SuccessGreen
        "BLOCKED" -> ErrorRed
        "IN_PROGRESS" -> WarningAmber
        else -> ShahGreen
    }
    Surface(
        color = badgeColor.copy(alpha = 0.10f),
        shape = RoundedCornerShape(5.dp)
    ) {
        Text(
            normalized.replace('_', ' '),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val badgeColor = when (priority.uppercase()) {
        "HIGH" -> ErrorRed
        "MEDIUM" -> WarningAmber
        else -> SuccessGreen
    }
    Surface(
        color = badgeColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            priority.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = badgeColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    employees: List<UserProfile>,
    projects: List<ProjectModel>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (TaskModel) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmployee by remember { mutableStateOf<UserProfile?>(null) }
    var selectedProject by remember { mutableStateOf<ProjectModel?>(null) }
    var priority by remember { mutableStateOf("MEDIUM") }
    var dueDateText by remember { mutableStateOf("") }
    var employeeExpanded by remember { mutableStateOf(false) }
    var projectExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                ExposedDropdownMenuBox(
                    expanded = employeeExpanded,
                    onExpandedChange = { employeeExpanded = !employeeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedEmployee?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign employee *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = employeeExpanded,
                        onDismissRequest = { employeeExpanded = false }
                    ) {
                        employees.forEach { employee ->
                            DropdownMenuItem(
                                text = { Text(employee.name.ifBlank { employee.email }) },
                                onClick = { selectedEmployee = employee; employeeExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = projectExpanded,
                    onExpandedChange = { projectExpanded = !projectExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProject?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Project *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = projectExpanded,
                        onDismissRequest = { projectExpanded = false }
                    ) {
                        projects.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(project.name) },
                                onClick = { selectedProject = project; projectExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded }
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { value ->
                            DropdownMenuItem(
                                text = { Text(value) },
                                onClick = { priority = value; priorityExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = { Text("Due date (dd/MM/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (validationError.isNotBlank()) {
                    Text(validationError, color = ErrorRed, fontSize = 12.sp)
                }
                if (employees.isEmpty() || projects.isEmpty()) {
                    Text(
                        "Active employees/projects are required before assigning a task.",
                        color = WarningAmber,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    val employee = selectedEmployee
                    val project = selectedProject
                    if (title.isBlank() || employee == null || project == null) {
                        validationError = "Title, employee and project are required."
                        return@Button
                    }
                    val parsedDate = if (dueDateText.isBlank()) {
                        null
                    } else {
                        try {
                            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).apply { isLenient = false }
                                .parse(dueDateText)?.let { Timestamp(it) }
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (dueDateText.isNotBlank() && parsedDate == null) {
                        validationError = "Enter due date as dd/MM/yyyy."
                        return@Button
                    }
                    onSave(
                        TaskModel(
                            title = title.trim(),
                            description = description.trim(),
                            assignedToUid = employee.uid,
                            assignedToName = employee.name.ifBlank { employee.email },
                            projectId = project.id,
                            projectName = project.name,
                            dueDate = parsedDate,
                            priority = priority,
                            status = "OPEN"
                        )
                    )
                }
            ) {
                Text(if (isSaving) "SAVING..." else "ASSIGN")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
