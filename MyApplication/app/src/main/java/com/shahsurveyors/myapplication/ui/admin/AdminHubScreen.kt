package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.components.GlobalAsyncLoader
import com.shahsurveyors.myapplication.ui.theme.DeepMidnightSlate
import com.shahsurveyors.myapplication.ui.theme.ElectricGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminHubScreen(viewModel: AdminViewModel = viewModel(), onBack: () -> Unit) {
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.fetchAdminData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gatekeeper Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnightSlate,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateUserDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Create User") },
                containerColor = ElectricGold,
                contentColor = Color.Black
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(DeepMidnightSlate)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepMidnightSlate,
                contentColor = ElectricGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElectricGold
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Logins") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Expenses") })
            }

            if (selectedTab == 0) {
                PendingLoginsList(viewModel)
            } else {
                PendingExpensesList(viewModel)
            }
        }
    }

    if (showCreateUserDialog) {
        CreateUserDialog(onDismiss = { showCreateUserDialog = false }, onCreate = { name, phone, pass, dept, role, access ->
            viewModel.createUser(name, phone, pass, dept, role, access)
            showCreateUserDialog = false
        })
    }

    GlobalAsyncLoader(isLoading = viewModel.isLoading)
}

@Composable
fun PendingLoginsList(viewModel: AdminViewModel) {
    if (viewModel.pendingUsers.isEmpty() && !viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending login requests", color = Color.Gray)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(viewModel.pendingUsers) { user ->
            GlassCard(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(user.phone, color = Color.Gray, fontSize = 12.sp)
                        Text("Dept: ${user.dept}", color = ElectricGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { /* viewModel.rejectUser(user.phone) */ }) {
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red)
                    }
                    IconButton(onClick = { viewModel.approveUser(user.phone, user.access) }) {
                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color.Green)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Module Access Permissions", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                val modules = listOf("ATTENDANCE", "TASKS", "CALCULATOR", "DSR", "CLIENTS", "CHAT", "BILLING", "EQUIPMENT", "EXPENSE")
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    modules.forEach { module ->
                        var checked by remember { mutableStateOf(user.access.contains(module)) }
                        FilterChip(
                            selected = checked,
                            onClick = { 
                                checked = !checked
                                val list = user.access.split(",").toMutableList()
                                if (checked) list.add(module) else list.remove(module)
                                user.access = list.filter { it.isNotEmpty() }.joinToString(",")
                            },
                            label = { Text(module, fontSize = 9.sp) },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricGold,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingExpensesList(viewModel: AdminViewModel) {
    if (viewModel.pendingExpenses.isEmpty() && !viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending expense claims", color = Color.Gray)
        }
    }
    // Similar implementation for expenses
}

@Composable
fun CreateUserDialog(onDismiss: () -> Unit, onCreate: (String, String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("SURVEY") }
    var role by remember { mutableStateOf("STAFF") }
    var access by remember { mutableStateOf("ATTENDANCE,TASKS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New User") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile/Phone") })
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") })
                // Simple TextFields for now, can be Dropdowns
                OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") })
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role (ADMIN/STAFF)") })
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(name, phone, pass, dept, role, access) }) {
                Text("CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
