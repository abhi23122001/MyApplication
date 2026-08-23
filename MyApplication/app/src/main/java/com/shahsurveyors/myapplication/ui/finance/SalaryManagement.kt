package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryManagementScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salary & Payroll", fontWeight = FontWeight.Bold, color = ShahWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            // Search & Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search Employee...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ShahGreen, containerColor = ShahWhite)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(dummySalaries) { salary ->
                    SalaryCard(salary)
                }
            }
        }
    }
}

@Composable
fun SalaryCard(data: SalaryData) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(data.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Dept: ${data.dept} | ID: ${data.id}", style = MaterialTheme.typography.labelSmall, color = ShahMediumGrey)
                }
                Text("₹ ${data.netSalary}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = ShahGreen)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShahLightGrey)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem("Attendance", "${data.presentDays} Days")
                InfoItem("Advances", "₹ ${data.advances}")
                InfoItem("Deductions", "₹ ${data.deductions}")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* TODO: Generate PDF Slip */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE SALARY SLIP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = ShahMediumGrey)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ShahBlack)
    }
}

data class SalaryData(
    val name: String,
    val id: String,
    val dept: String,
    val presentDays: Int,
    val advances: Int,
    val deductions: Int,
    val netSalary: Int
)

val dummySalaries = listOf(
    SalaryData("Rahul Kumar", "EMP001", "SURVEY", 26, 2000, 500, 24500),
    SalaryData("Amit Singh", "EMP002", "FINANCE", 24, 0, 200, 28000),
    SalaryData("Suresh Yadav", "EMP003", "MARKETING", 22, 5000, 1000, 19000)
)
