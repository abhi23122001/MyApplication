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
fun MoreModulesScreen(
    userRole: String = "employee",
    userAccess: String = "ATTENDANCE,TASKS,CHAT",
    onNavigate: (String) -> Unit
) {
    val isAdmin = userRole.equals("admin", ignoreCase = true)
    val accessUpper = userAccess.uppercase()
    val hasFullAccess = isAdmin || accessUpper.contains("ALL")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAdmin) "ERP Management Modules" else "My Modules",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey),
            contentPadding = PaddingValues(16.dp)
        ) {
            // ====================================================
            // 1. HR & SALARY (Filtered)
            // ====================================================
            val permittedPeople = buildList {
                if (hasFullAccess) {
                    add(
                        ModuleData(
                            name = "Employee & Salary Settings",
                            description = "Configure staff salaries and permissions",
                            icon = Icons.Default.Groups,
                            route = "employees"
                        )
                    )
                    add(
                        ModuleData(
                            name = "Payroll & Salary Slips",
                            description = "Calculate payroll and export slips",
                            icon = Icons.Default.Payments,
                            route = "salary"
                        )
                    )
                } else {
                    add(
                        ModuleData(
                            name = "My Salary & Payslip",
                            description = "View net salary, EMIs and download slip",
                            icon = Icons.Default.Payments,
                            route = "salary"
                        )
                    )
                }
                add(
                    ModuleData(
                        name = "Attendance Punch",
                        description = "GPS & Selfie attendance logs",
                        icon = Icons.Default.PunchClock,
                        route = "attendance"
                    )
                )
                add(
                    ModuleData(
                        name = if (hasFullAccess) "Leave Approvals & History" else "My Leave Applications",
                        description = "Apply for leave and track approvals",
                        icon = Icons.Default.EventBusy,
                        route = "leave"
                    )
                )
            }

            if (permittedPeople.isNotEmpty()) {
                item {
                    CategoryHeader("👥 HR & PAYROLL")
                }
                items(permittedPeople) { module ->
                    ModuleItem(module = module, onNavigate = onNavigate)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // ====================================================
            // 2. PROJECT & FIELD SURVEY (Filtered)
            // ====================================================
            val permittedSurvey = buildList {
                add(
                    ModuleData(
                        name = "My Assigned Tasks",
                        description = "Project tasks and progress update",
                        icon = Icons.Default.Assignment,
                        route = "tasks"
                    )
                )
                if (hasFullAccess || accessUpper.contains("SURVEY")) {
                    add(
                        ModuleData(
                            name = "Survey Grid Engine",
                            description = "WGS84 to UTM, Area and Base-shift",
                            icon = Icons.Default.Calculate,
                            route = "survey"
                        )
                    )
                }
                if (hasFullAccess) {
                    add(
                        ModuleData(
                            name = "Equipment Tracker",
                            description = "Leica and survey instruments inventory",
                            icon = Icons.Default.PrecisionManufacturing,
                            route = "equipment"
                        )
                    )
                }
                if (hasFullAccess || accessUpper.contains("DSR")) {
                    add(
                        ModuleData(
                            name = "Daily Status Report (DSR)",
                            description = "Daily field site work progress logs",
                            icon = Icons.Default.Description,
                            route = "dsr"
                        )
                    )
                }
            }

            if (permittedSurvey.isNotEmpty()) {
                item {
                    CategoryHeader("🏗️ PROJECTS & FIELD OPERATIONS")
                }
                items(permittedSurvey) { module ->
                    ModuleItem(module = module, onNavigate = onNavigate)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // ====================================================
            // 3. FINANCE & BILLING (Filtered)
            // ====================================================
            val permittedFinance = buildList {
                if (hasFullAccess || accessUpper.contains("EXPENSE")) {
                    add(
                        ModuleData(
                            name = "Expense Claims",
                            description = "Submit and track field claims",
                            icon = Icons.Default.Receipt,
                            route = "expense"
                        )
                    )
                }
                if (hasFullAccess || accessUpper.contains("BILLING")) {
                    add(
                        ModuleData(
                            name = "Billing & GST Invoices",
                            description = "Create quotations, bills and tax invoices",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            route = "billing"
                        )
                    )
                }
            }

            if (permittedFinance.isNotEmpty()) {
                item {
                    CategoryHeader("💰 FINANCE & BILLING")
                }
                items(permittedFinance) { module ->
                    ModuleItem(module = module, onNavigate = onNavigate)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // ====================================================
            // 4. CRM & CLIENTS (Filtered)
            // ====================================================
            val permittedBusiness = buildList {
                if (hasFullAccess || accessUpper.contains("CRM")) {
                    add(
                        ModuleData(
                            name = "Clients CRM",
                            description = "Client directory and project contacts",
                            icon = Icons.Default.ContactPage,
                            route = "clients"
                        )
                    )
                }
                add(
                    ModuleData(
                        name = "Live Team Radar & Chat",
                        description = "Internal team communication",
                        icon = Icons.Default.Chat,
                        route = "chat"
                    )
                )
            }

            if (permittedBusiness.isNotEmpty()) {
                item {
                    CategoryHeader("🤝 CRM & COMMUNICATION")
                }
                items(permittedBusiness) { module ->
                    ModuleItem(module = module, onNavigate = onNavigate)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // ====================================================
            // 5. ADMIN CONTROLS (ADMIN ONLY)
            // ====================================================
            if (isAdmin) {
                item {
                    CategoryHeader("⚙️ ADMINISTRATION (ADMIN ONLY)")
                }
                items(adminOnlyModules) { module ->
                    ModuleItem(module = module, onNavigate = onNavigate)
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = ShahDarkGreen,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ModuleItem(
    module: ModuleData,
    onNavigate: (String) -> Unit
) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = ShahGreen.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = module.icon,
                        contentDescription = null,
                        tint = ShahGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ShahBlack
                )
                Text(
                    text = module.description,
                    fontSize = 11.sp,
                    color = ShahMediumGrey
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ShahMediumGrey,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

data class ModuleData(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val adminOnlyModules = listOf(
    ModuleData(
        name = "Admin Hub",
        description = "Company profiles, bank accounts and staff approvals",
        icon = Icons.Default.AdminPanelSettings,
        route = "admin_hub"
    ),
    ModuleData(
        name = "Geo-fence Settings",
        description = "GPS coordinates and work area radius",
        icon = Icons.Default.Settings,
        route = "settings"
    )
)