package com.shahsurveyors.myapplication.ui.tasks

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.TaskModel
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    viewModel: TaskViewModel = viewModel(),
    currentUid: String = "",
    currentUserName: String = "Staff User",
    isAdmin: Boolean = false,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskToUpdate by remember { mutableStateOf<TaskModel?>(null) }

    LaunchedEffect(currentUid, isAdmin) {
        viewModel.loadTasks(currentUid, isAdmin)
    }

    LaunchedEffect(viewModel.statusMessage) {
        viewModel.statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAdmin) "Task Management & Assignment" else "My Assigned Tasks",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showCreateTaskDialog = true },
                    containerColor = ShahGreen,
                    contentColor = ShahWhite
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Assign Task")
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
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (viewModel.taskList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isAdmin) "No active tasks. Tap + to assign a task." else "No tasks assigned to you right now.",
                        color = ShahMediumGrey,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.taskList, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            isAdmin = isAdmin,
                            onStatusClick = { selectedTaskToUpdate = task }
                        )
                    }
                }
            }
        }
    }

    if (showCreateTaskDialog) {
        CreateTaskDialog(
            adminUid = currentUid,
            adminName = currentUserName,
            onDismiss = { showCreateTaskDialog = false },
            onCreate = { title, desc, site, empUid, empName, empDept, deadline, priority ->
                viewModel.createTask(
                    title = title,
                    description = desc,
                    projectSite = site,
                    assignedToUid = empUid,
                    assignedToName = empName,
                    assignedToDept = empDept,
                    deadline = deadline,
                    priority = priority,
                    adminUid = currentUid,
                    adminName = currentUserName
                )
                showCreateTaskDialog = false
            }
        )
    }

    selectedTaskToUpdate?.let { task ->
        UpdateTaskStatusDialog(
            task = task,
            onDismiss = { selectedTaskToUpdate = null },
            onUpdate = { newStatus, notes ->
                viewModel.updateTaskStatus(
                    taskId = task.id,
                    status = newStatus,
                    notes = notes,
                    userUid = currentUid,
                    isAdmin = isAdmin
                )
                selectedTaskToUpdate = null
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskModel,
    isAdmin: Boolean,
    onStatusClick: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "URGENT" -> ErrorRed
        "HIGH" -> WarningAmber
        "LOW" -> ShahMediumGrey
        else -> ShahGreen
    }

    val statusBg = when (task.status) {
        "COMPLETED" -> SuccessGreen.copy(alpha = 0.15f)
        "IN_PROGRESS" -> WarningAmber.copy(alpha = 0.15f)
        else -> ShahGrey
    }

    val statusColor = when (task.status) {
        "COMPLETED" -> SuccessGreen
        "IN_PROGRESS" -> WarningAmber
        else -> ShahDarkGrey
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ShahBlack,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = priorityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = task.priority,
                        color = priorityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = task.description, fontSize = 13.sp, color = ShahDarkGrey)
            }

            if (task.projectSite.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = ShahMediumGrey, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Site: ${task.projectSite}", fontSize = 12.sp, color = ShahMediumGrey)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAdmin) "Assigned to: ${task.assignedToName} (${task.assignedToDept})" else "Assigned by: ${task.assignedByName}",
                        fontSize = 11.sp,
                        color = ShahMediumGrey,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Deadline: ${task.deadline.ifBlank { "No deadline" }}",
                        fontSize = 11.sp,
                        color = ShahMediumGrey
                    )
                }

                Surface(
                    modifier = Modifier.clickable { onStatusClick() },
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.status,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Update Status",
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTaskDialog(
    adminUid: String,
    adminName: String,
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, site: String, empUid: String, empName: String, empDept: String, deadline: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf(LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var priority by remember { mutableStateOf("MEDIUM") }

    var selectedEmployeeUid by remember { mutableStateOf("") }
    var selectedEmployeeName by remember { mutableStateOf("") }
    var selectedEmployeeDept by remember { mutableStateOf("SURVEY") }

    val employees = remember { mutableStateListOf<Pair<String, String>>() } // uid to name
    val firestore = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("users").get().await()
            employees.clear()
            for (doc in snapshot.documents) {
                val name = doc.getString("name") ?: "Employee"
                employees.add(doc.id to name)
            }
            if (employees.isNotEmpty()) {
                selectedEmployeeUid = employees.first().first
                selectedEmployeeName = employees.first().second
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = ShahWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Assign New Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShahBlack)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Scope") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = site,
                    onValueChange = { site = it },
                    label = { Text("Project Site / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Employee Selection Dropdown / Buttons
                Text("Assign To Employee:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShahDarkGreen)
                if (employees.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        employees.take(3).forEach { (uid, name) ->
                            FilterChip(
                                selected = selectedEmployeeUid == uid,
                                onClick = {
                                    selectedEmployeeUid = uid
                                    selectedEmployeeName = name
                                },
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("LOW", "MEDIUM", "HIGH", "URGENT").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && selectedEmployeeUid.isNotBlank()) {
                            onCreate(
                                title.trim(),
                                desc.trim(),
                                site.trim(),
                                selectedEmployeeUid,
                                selectedEmployeeName,
                                selectedEmployeeDept,
                                deadline.trim(),
                                priority
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ASSIGN TASK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UpdateTaskStatusDialog(
    task: TaskModel,
    onDismiss: () -> Unit,
    onUpdate: (newStatus: String, notes: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(task.status) }
    var notes by remember { mutableStateOf(task.completionNotes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = ShahWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Update Task Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShahBlack)
                Text(task.title, fontSize = 14.sp, color = ShahDarkGrey, fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PENDING", "IN_PROGRESS", "COMPLETED").forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Completion Notes / Remarks") },
                    placeholder = { Text("e.g. Field survey completed for Block A") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = { onUpdate(selectedStatus, notes.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SAVE STATUS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}