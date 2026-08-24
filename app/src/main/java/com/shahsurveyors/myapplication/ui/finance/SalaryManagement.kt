package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryManagementScreen(
    viewModel: SalaryViewModel,
    onBack: () -> Unit,
    onGenerateSalarySlip: (SalaryData) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchSalaries()
    }

    val filteredSalaries = remember(viewModel.salaryRecords, searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.salaryRecords
        } else {
            viewModel.salaryRecords.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true) ||
                        it.dept.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Salary & Payroll", fontWeight = FontWeight.Bold, color = ShahWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShahWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search Employee...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShahGreen,
                    focusedContainerColor = ShahWhite,
                    unfocusedContainerColor = ShahWhite
                )
            )

            if (viewModel.isLoading && filteredSalaries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (filteredSalaries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No salary records found", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(items = filteredSalaries, key = { it.id }) { salary ->
                        SalaryCard(data = salary, onGenerateSalarySlip = { onGenerateSalarySlip(salary) })
                    }
                }
            }
        }
    }
}

@Composable
fun SalaryCard(data: SalaryData, onGenerateSalarySlip: () -> Unit) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = data.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShahBlack)
                    Text(text = "Dept: ${data.dept} | ID: ${data.id}", style = MaterialTheme.typography.labelSmall, color = ShahMediumGrey)
                }
                Text(text = formatIndianCurrency(data.netSalary), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = ShahGreen)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ShahLightGrey)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(label = "Attendance", value = "${data.presentDays} Days")
                InfoItem(label = "Advances", value = formatIndianCurrency(data.advances))
                InfoItem(label = "Deductions", value = formatIndianCurrency(data.deductions))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGenerateSalarySlip,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite)
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "GENERATE SALARY SLIP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ShahMediumGrey)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ShahBlack)
    }
}

data class SalaryData(
    val id: String = "",
    val name: String = "",
    val dept: String = "",
    val presentDays: Int = 0,
    val advances: Double = 0.0,
    val deductions: Double = 0.0,
    val basicSalary: Double = 0.0,
    val netSalary: Double = 0.0,
    val month: String = "",
    val year: Int = 0,
    val status: String = "PENDING"
)

private fun formatIndianCurrency(amount: Double): String {
    return "₹ " + String.format(java.util.Locale.ENGLISH, "%,.0f", amount)
}
