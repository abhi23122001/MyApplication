package com.shahsurveyors.myapplication.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("More Modules", fontWeight = FontWeight.Bold, color = ShahWhite)
                        Text("All tools & business modules", fontSize = 10.sp, color = ShahWhite.copy(alpha = .72f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize().background(ShahGrey),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = ShahDarkGreen) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = ShahWhite.copy(alpha = .12f)) {
                            Icon(Icons.Default.Apps, null, tint = ShahWhite, modifier = Modifier.padding(11.dp).size(28.dp))
                        }
                        Spacer(Modifier.width(13.dp))
                        Column {
                            Text("Everything in one place", color = ShahWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text("Choose a module to continue", color = ShahWhite.copy(alpha = .72f), fontSize = 11.sp)
                        }
                    }
                }
            }
            moduleSection("PEOPLE & HR", "Team, attendance & payroll", peopleModules, onNavigate)
            moduleSection("PROJECT & SURVEY", "Sites, survey & field operations", projectModules, onNavigate)
            moduleSection("FINANCE", "Expenses, billing & quotations", financeModules, onNavigate)
            moduleSection("BUSINESS", "Clients & business development", businessModules, onNavigate)
            moduleSection("REPORTS", "Management & operational reports", reportModules, onNavigate)
            moduleSection("ADMINISTRATION", "Company controls & settings", adminModules, onNavigate)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.moduleSection(title: String, subtitle: String, modules: List<ModuleData>, onNavigate: (String) -> Unit) {
    item {
        Column(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 2.dp)) {
            Text(title, fontSize = 12.sp, color = ShahGreen, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Text(subtitle, fontSize = 10.sp, color = ShahMediumGrey)
        }
    }
    items(modules) { module -> ModuleItem(module, onNavigate) }
}

@Composable
fun ModuleItem(module: ModuleData, onNavigate: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigate(module.route) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShahWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = ShahGreen.copy(alpha = .10f)) {
                Icon(module.icon, module.name, tint = ShahGreen, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(module.name, fontWeight = FontWeight.Bold, color = ShahBlack, fontSize = 14.sp)
                Spacer(Modifier.height(3.dp))
                Text(module.description, fontSize = 11.sp, color = ShahMediumGrey)
            }
            Icon(Icons.Default.ChevronRight, "Open", tint = ShahMediumGrey, modifier = Modifier.size(22.dp))
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
    ModuleData("Financial Reports", "Profit & Loss summaries", Icons.Default.Assessment, "reports_finance"),
    ModuleData("Work Progress", "Field and site reports", Icons.Default.Timeline, "reports_work")
)
val adminModules = listOf(
    ModuleData("Admin Hub", "Company and user controls", Icons.Default.AdminPanelSettings, "admin_hub"),
    ModuleData("Settings", "App preferences & Geo-fence", Icons.Default.Settings, "settings")
)
