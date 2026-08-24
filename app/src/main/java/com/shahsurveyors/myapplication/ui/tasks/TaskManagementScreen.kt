package com.shahsurveyors.myapplication.ui.tasks

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.TaskModel
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    viewModel: TaskViewModel,
    uid: String,
    onBack: () -> Unit = {},
    onAddTask: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OPEN", "COMPLETED")

    LaunchedEffect(uid) {
        viewModel.fetchTasks(uid)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Task Hub",
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ShahWhite,
                    contentColor = ShahGreen,
                    indicator = { positions ->
                        if (selectedTab < positions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(positions[selectedTab]),
                                color = ShahGreen
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = ShahGreen,
                contentColor = ShahWhite
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
        ) {
            val filteredTasks = viewModel.tasks.filter { task ->
                if (selectedTab == 0) task.status != "COMPLETED" else task.status == "COMPLETED"
            }

            if (viewModel.isLoading && filteredTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (filteredTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No tasks available", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(items = filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onComplete = { viewModel.updateStatus(task.id, "COMPLETED", uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskModel,
    onComplete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = task.title, color = ShahBlack, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        PriorityBadge(priority = task.priority)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = ShahMediumGrey)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = task.projectName, color = ShahMediumGrey, fontSize = 12.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val dateStr = task.dueDate?.let {
                        SimpleDateFormat("dd MMM", Locale.ENGLISH).format(it.toDate())
                    } ?: "No date"
                    
                    Text(text = dateStr, color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    if (task.status != "COMPLETED") {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(text = if (expanded) "HIDE" else "COMPLETE", fontSize = 11.sp, color = ShahGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ShahLightGrey)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Completion Proof (Mocked)", color = ShahMediumGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = ShahWhite)
                ) {
                    Text(text = "SUBMIT AS COMPLETED", fontWeight = FontWeight.Bold)
                }
            }
        }
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
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = priority.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = badgeColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
