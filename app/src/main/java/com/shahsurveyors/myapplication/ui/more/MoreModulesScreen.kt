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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.admin.hasModuleAccess
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesScreen(onNavigate: (String) -> Unit, onLogout: () -> Unit = {}) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    var access by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(auth.currentUser?.uid) {
        loaded = false
        val uid = auth.currentUser?.uid
        if (uid == null) {
            loaded = true
            return@LaunchedEffect
        }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                access = doc.getString("access") ?: ""
                role = doc.getString("role") ?: ""
                loaded = true
            }
            .addOnFailureListener { loaded = true }
    }

    val isAdmin = role.equals("admin", ignoreCase = true)
    fun allowed(module: ModuleData): Boolean = isAdmin || hasModuleAccess(access, module.permission)
    val visiblePeople = peopleModules.filter(::allowed)
    val visibleProjects = projectModules.filter(::allowed)
    val visibleFinance = financeModules.filter(::allowed)
    val visibleBusiness = businessModules.filter(::allowed)
    val visibleReports = reportModules.filter(::allowed)
    val visibleAdmin = adminModules.filter(::allowed)

    Scaffold(topBar = { TopAppBar(title = { Column { Text("More Modules", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Assigned modules only", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)) }) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues).fillMaxSize().background(ShahGrey), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = ShahDarkGreen) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = RoundedCornerShape(14.dp), color = ShahWhite.copy(.12f)) { Icon(Icons.Default.Apps, null, tint = ShahWhite, modifier = Modifier.padding(11.dp).size(28.dp)) }; Spacer(Modifier.width(13.dp)); Column { Text("Everything in one place", color = ShahWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(3.dp)); Text(if (loaded) "Only assigned modules are shown" else "Loading permissions...", color = ShahWhite.copy(.72f), fontSize = 11.sp) } } } }
            if (loaded) {
                if (visiblePeople.isNotEmpty()) moduleSection("PEOPLE & HR", "Team, attendance & payroll", visiblePeople, onNavigate)
                if (visibleProjects.isNotEmpty()) moduleSection("PROJECT & SURVEY", "Sites, survey & field operations", visibleProjects, onNavigate)
                if (visibleFinance.isNotEmpty()) moduleSection("FINANCE", "Expenses, billing & quotations", visibleFinance, onNavigate)
                if (visibleBusiness.isNotEmpty()) moduleSection("BUSINESS", "Clients & business development", visibleBusiness, onNavigate)
                if (visibleReports.isNotEmpty()) moduleSection("REPORTS", "Management & operational reports", visibleReports, onNavigate)
                if (visibleAdmin.isNotEmpty()) moduleSection("ADMINISTRATION", "Company controls & settings", visibleAdmin, onNavigate)
                if (visiblePeople.isEmpty() && visibleProjects.isEmpty() && visibleFinance.isEmpty() && visibleBusiness.isEmpty() && visibleReports.isEmpty() && visibleAdmin.isEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) { Text("No modules assigned. Ask Admin to enable access.", color = ShahMediumGrey) } }
                }
            } else {
                item { Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ShahGreen) } }
            }
            item { Card(Modifier.fillMaxWidth().clickable { showLogoutDialog = true }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = RoundedCornerShape(12.dp), color = ErrorRed.copy(.10f)) { Icon(Icons.Default.Logout, null, tint = ErrorRed, modifier = Modifier.padding(10.dp).size(22.dp)) }; Spacer(Modifier.width(13.dp)); Column { Text("Logout", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 14.sp); Text("Sign out of this device", fontSize = 11.sp, color = ShahMediumGrey) } } } }
        }
    }
    if (showLogoutDialog) AlertDialog(onDismissRequest = { showLogoutDialog = false }, title = { Text("Logout?") }, text = { Text("Are you sure you want to sign out?") }, confirmButton = { TextButton(onClick = { showLogoutDialog = false; onLogout() }) { Text("LOGOUT", color = ErrorRed, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("CANCEL") } })
}

private fun androidx.compose.foundation.lazy.LazyListScope.moduleSection(title: String, subtitle: String, modules: List<ModuleData>, onNavigate: (String) -> Unit) { item { Column(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 2.dp)) { Text(title, fontSize = 12.sp, color = ShahGreen, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp); Text(subtitle, fontSize = 10.sp, color = ShahMediumGrey) } }; items(modules, key = { it.route }) { module -> ModuleItem(module, onNavigate) } }

@Composable
fun ModuleItem(module: ModuleData, onNavigate: (String) -> Unit) { Card(Modifier.fillMaxWidth().clickable { onNavigate(module.route) }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite), elevation = CardDefaults.cardElevation(1.dp)) { Row(Modifier.padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = RoundedCornerShape(12.dp), color = ShahGreen.copy(.10f)) { Icon(module.icon, module.name, tint = ShahGreen, modifier = Modifier.padding(10.dp).size(22.dp)) }; Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(module.name, fontWeight = FontWeight.Bold, color = ShahBlack, fontSize = 14.sp); Spacer(Modifier.height(3.dp)); Text(module.description, fontSize = 11.sp, color = ShahMediumGrey) }; Icon(Icons.Default.ChevronRight, "Open", tint = ShahMediumGrey, modifier = Modifier.size(22.dp)) } } }

data class ModuleData(val name: String, val description: String, val icon: ImageVector, val route: String, val permission: String)
val peopleModules = listOf(
    ModuleData("Employees", "Manage team and staff details", Icons.Default.Groups, "employees", "ADMIN"),
    ModuleData("Attendance", "Track daily presence & logs", Icons.Default.PunchClock, "attendance", "ATTENDANCE"),
    ModuleData("Leave", "Approve and track leave requests", Icons.Default.EventBusy, "leave", "LEAVE"),
    ModuleData("Advance Salary", "Request salary advance for approval", Icons.Default.Payments, "advance_salary", "ADVANCE"),
    ModuleData("Salary & Payroll", "Generate salary slips", Icons.Default.Payments, "salary", "SALARY")
)
val projectModules = listOf(
    ModuleData("Projects", "Monitor active project sites", Icons.Default.Architecture, "projects", "PROJECTS"),
    ModuleData("Survey Tools", "Calculators and data entry", Icons.Default.Calculate, "survey", "SURVEY"),
    ModuleData("Equipment", "Leica and survey inventory", Icons.Default.PrecisionManufacturing, "equipment", "EQUIPMENT"),
    ModuleData("DSR Reports", "Daily status report logs", Icons.Default.Description, "dsr", "DSR")
)
val financeModules = listOf(
    ModuleData("Expenses", "Manage and approve claims", Icons.Default.Receipt, "expense", "EXPENSE"),
    ModuleData("Billing & Invoices", "Create and track invoices", Icons.AutoMirrored.Filled.ReceiptLong, "billing", "BILLING"),
    ModuleData("Quotations", "Generate official quotes", Icons.Default.RequestQuote, "quotes", "BILLING")
)
val businessModules = listOf(
    ModuleData("Clients", "CRM and client management", Icons.Default.ContactPage, "clients", "CLIENTS"),
    ModuleData("Marketing", "Leads and conversion", Icons.Default.Campaign, "marketing", "MARKETING")
)
val reportModules = listOf(
    ModuleData("Employee Attendance + Expense PDF", "Employee-wise monthly report", Icons.Default.PictureAsPdf, "employee_reports", "REPORTS"),
    ModuleData("Financial Reports", "Profit & Loss summaries", Icons.Default.Assessment, "reports_finance", "REPORTS"),
    ModuleData("Work Progress", "Field and site reports", Icons.Default.Timeline, "reports_work", "REPORTS")
)
val adminModules = listOf(
    ModuleData("Admin Hub", "Company and user controls", Icons.Default.AdminPanelSettings, "admin_hub", "ADMIN"),
    ModuleData("Settings", "App preferences & Geo-fence", Icons.Default.Settings, "settings", "ADMIN")
)
