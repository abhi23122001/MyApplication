package com.shahsurveyors.myapplication.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesScreen(
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More Modules", fontWeight = FontWeight.Bold, color = ShahWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(ShahGrey),
            contentPadding = PaddingValues(16.dp)
        ) {
            item { CategoryHeader("👥 PEOPLE & HR") }
            items(peopleModules) { module -> ModuleItem(module, onNavigate) }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { CategoryHeader("🏗️ PROJECT & SURVEY") }
            items(projectModules) { module -> ModuleItem(module, onNavigate) }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { CategoryHeader("💰 FINANCE") }
            items(financeModules) { module -> ModuleItem(module, onNavigate) }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { CategoryHeader("🤝 BUSINESS") }
            items(businessModules) { module -> ModuleItem(module, onNavigate) }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { CategoryHeader("📊 REPORTS") }
            items(reportModules) { module -> ModuleItem(module, onNavigate) }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { CategoryHeader("⚙️ ADMINISTRATION") }
            items(adminModules) { module -> ModuleItem(module, onNavigate) }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = ShahGreen,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun ModuleItem(module: ModuleData, onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onNavigate(module.route) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ShahGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    tint = ShahGreen,
                    modifier = Modifier.padding(8.dp).size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(module.name, fontWeight = FontWeight.Bold, color = ShahBlack)
                Text(module.description, fontSize = 11.sp, color = ShahMediumGrey)
            }
            Icon(Icons.AutoMirrored.Filled.ChevronRight, contentDescription = null, tint = ShahLightGrey)
        }
    }
}

data class ModuleData(val name: String, val description: String, val icon: ImageVector, val route: String)

val peopleModules = listOf(
    ModuleData("Employees", "Manage team and staff details", Icons.Default.Groups, "employees"),
    ModuleData("Attendance", "Track daily presence & logs", Icons.Default.PunchClock, "attendance"),
    ModuleData("Leave", "Approve and track leave requests", Icons.Default.EventBusy, "leave"),
    ModuleData("Salary & Payroll", "Generate salary slips", Icons.Default.Payments, "salary")
)

val projectModules = listOf(
    ModuleData("Projects", "Monitor active project sites", Icons.Default.Architecture, "projects"),
    ModuleData("Survey Tools", "Calculators and data entry", Icons.Default.Calculate, "survey"),
    ModuleData("Equipment", "Leica and survey inventory", Icons.Default.PrecisionManufacturing, "equipment"),
    ModuleData("DSR Reports", "Daily status report logs", Icons.Default.Description, "dsr")
)

val financeModules = listOf(
    ModuleData("Expenses", "Manage and approve claims", Icons.Default.Receipt, "expense"),
    ModuleData("Billing & Invoices", "Create and track invoices", Icons.AutoMirrored.Filled.ReceiptLong, "billing"),
    ModuleData("Quotations", "Generate official quotes", Icons.Default.RequestQuote, "quotes")
)

val businessModules = listOf(
    ModuleData("Clients", "CRM and client management", Icons.Default.ContactPage, "clients"),
    ModuleData("Marketing", "Leads and conversion", Icons.Default.Campaign, "marketing")
)

val reportModules = listOf(
    ModuleData("Financial Reports", "Profit & Loss summaries", Icons.AutoMirrored.Filled.Assessment, "reports_finance"),
    ModuleData("Work Progress", "Field and site reports", Icons.Default.Timeline, "reports_work")
)

val adminModules = listOf(
    ModuleData("Admin Hub", "Company and user controls", Icons.Default.AdminPanelSettings, "admin_hub"),
    ModuleData("Settings", "App preferences & Geo-fence", Icons.Default.Settings, "settings")
)
