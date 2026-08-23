package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(onBack: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Management", fontWeight = FontWeight.Bold, color = ShahWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO */ }, containerColor = ShahGreen, contentColor = ShahWhite) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search by name or ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShahGreen, containerColor = ShahWhite)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(dummyEmployees) { employee ->
                    EmployeeCard(employee)
                }
            }
        }
    }
}

@Composable
fun EmployeeCard(employee: EmployeeData) {
    Card(
        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(50.dp).clip(CircleShape),
                color = ShahGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(employee.name.take(1), fontWeight = FontWeight.Bold, color = ShahGreen, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("ID: ${employee.id} | ${employee.role}", fontSize = 12.sp, color = ShahMediumGrey)
                Text(employee.dept, color = ShahGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            IconButton(onClick = { /* TODO: Edit */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = ShahMediumGrey)
            }
        }
    }
}

data class EmployeeData(val name: String, val id: String, val dept: String, val role: String)

val dummyEmployees = listOf(
    EmployeeData("Rahul Kumar", "EMP001", "SURVEY", "STAFF"),
    EmployeeData("Amit Singh", "EMP002", "FINANCE", "ADMIN"),
    EmployeeData("Suresh Yadav", "EMP003", "MARKETING", "STAFF"),
    EmployeeData("Priya Sharma", "EMP004", "SURVEY", "STAFF"),
    EmployeeData("Vikram Singh", "EMP005", "OPERATIONS", "STAFF")
)
