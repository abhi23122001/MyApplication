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
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahLightGrey
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesScreen(
    onNavigate: (String) -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "More Modules",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            ShahDarkGreen
                    )
            )
        }

    ) { paddingValues ->

        LazyColumn(

            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(ShahGrey),

            contentPadding =
                PaddingValues(16.dp)
        ) {

            // ====================================================
            // PEOPLE & HR
            // ====================================================

            item {
                CategoryHeader("👥 PEOPLE & HR")
            }

            items(peopleModules) { module ->

                ModuleItem(
                    module = module,
                    onNavigate = onNavigate
                )
            }


            item {
                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }


            // ====================================================
            // PROJECT & SURVEY
            // ====================================================

            item {
                CategoryHeader("🏗️ PROJECT & SURVEY")
            }

            items(projectModules) { module ->

                ModuleItem(
                    module = module,
                    onNavigate = onNavigate
                )
            }


            item {
                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }


            // ====================================================
            // FINANCE
            // ====================================================

            item {
                CategoryHeader("💰 FINANCE")
            }

            items(financeModules) { module ->

                ModuleItem(
                    module = module,
                    onNavigate = onNavigate
                )
            }


            item {
                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }


            // ====================================================
            // BUSINESS
            // ====================================================

            item {
                CategoryHeader("🤝 BUSINESS")
            }

            items(businessModules) { module ->

                ModuleItem(
                    module = module,
                    onNavigate = onNavigate
                )
            }


            item {
                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }


            // ====================================================
            // REPORTS
            // ====================================================

            item {
                CategoryHeader("📊 REPORTS")
            }

            items(reportModules) { module ->

                ModuleItem(
                    module = module,
                    onNavigate = onNavigate
                )
            }


            item {
                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }


            // ====================================================
            // ADMINISTRATION
            // ====================================================

            item {
                CategoryHeader("⚙️ ADMINISTRATION")
            }

            items(adminModules) { module ->

                ModuleItem(
                    module = module,
                    onNavigate = onNavigate
                )
            }


            item {
                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )
            }
        }
    }
}


// ============================================================
// CATEGORY HEADER
// ============================================================

@Composable
fun CategoryHeader(
    title: String
) {

    Text(

        text =
            title,

        style =
            MaterialTheme
                .typography
                .labelLarge,

        color =
            ShahGreen,

        fontWeight =
            FontWeight.Bold,

        modifier =
            Modifier.padding(
                bottom = 8.dp
            )
    )
}


// ============================================================
// MODULE ITEM
// ============================================================

@Composable
fun ModuleItem(
    module: ModuleData,
    onNavigate: (String) -> Unit
) {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    onNavigate(
                        module.route
                    )
                },

        shape =
            RoundedCornerShape(12.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    ShahWhite
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Row(

            modifier =
                Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(

                color =
                    ShahGreen.copy(
                        alpha = 0.1f
                    ),

                shape =
                    RoundedCornerShape(8.dp)
            ) {

                Icon(

                    imageVector =
                        module.icon,

                    contentDescription =
                        module.name,

                    tint =
                        ShahGreen,

                    modifier =
                        Modifier
                            .padding(8.dp)
                            .size(24.dp)
                )
            }


            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        module.name,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        ShahBlack
                )


                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )


                Text(

                    text =
                        module.description,

                    fontSize =
                        11.sp,

                    color =
                        ShahMediumGrey
                )
            }


            Icon(

                imageVector =
                    Icons.Default.ChevronRight,

                contentDescription =
                    "Open",

                tint =
                    ShahLightGrey
            )
        }
    }
}


// ============================================================
// MODULE MODEL
// ============================================================

data class ModuleData(

    val name: String,

    val description: String,

    val icon: ImageVector,

    val route: String
)


// ============================================================
// PEOPLE & HR
// ============================================================

val peopleModules =
    listOf(

        ModuleData(
            name = "Employees",
            description =
                "Manage team and staff details",
            icon =
                Icons.Default.Groups,
            route =
                "employees"
        ),

        ModuleData(
            name = "Attendance",
            description =
                "Track daily presence & logs",
            icon =
                Icons.Default.PunchClock,
            route =
                "attendance"
        ),

        ModuleData(
            name = "Leave",
            description =
                "Approve and track leave requests",
            icon =
                Icons.Default.EventBusy,
            route =
                "leave"
        ),

        ModuleData(
            name = "Salary & Payroll",
            description =
                "Generate salary slips",
            icon =
                Icons.Default.Payments,
            route =
                "salary"
        )
    )


// ============================================================
// PROJECT & SURVEY
// ============================================================

val projectModules =
    listOf(

        ModuleData(
            name = "Projects",
            description =
                "Monitor active project sites",
            icon =
                Icons.Default.Architecture,
            route =
                "projects"
        ),

        ModuleData(
            name = "Survey Tools",
            description =
                "Calculators and data entry",
            icon =
                Icons.Default.Calculate,
            route =
                "survey"
        ),

        ModuleData(
            name = "Equipment",
            description =
                "Leica and survey inventory",
            icon =
                Icons.Default.PrecisionManufacturing,
            route =
                "equipment"
        ),

        ModuleData(
            name = "DSR Reports",
            description =
                "Daily status report logs",
            icon =
                Icons.Default.Description,
            route =
                "dsr"
        )
    )


// ============================================================
// FINANCE
// ============================================================

val financeModules =
    listOf(

        ModuleData(
            name = "Expenses",
            description =
                "Manage and approve claims",
            icon =
                Icons.Default.Receipt,
            route =
                "expense"
        ),

        ModuleData(
            name = "Billing & Invoices",
            description =
                "Create and track invoices",
            icon =
                Icons.AutoMirrored.Filled.ReceiptLong,
            route =
                "billing"
        ),

        ModuleData(
            name = "Quotations",
            description =
                "Generate official quotes",
            icon =
                Icons.Default.RequestQuote,
            route =
                "quotes"
        )
    )


// ============================================================
// BUSINESS
// ============================================================

val businessModules =
    listOf(

        ModuleData(
            name = "Clients",
            description =
                "CRM and client management",
            icon =
                Icons.Default.ContactPage,
            route =
                "clients"
        ),

        ModuleData(
            name = "Marketing",
            description =
                "Leads and conversion",
            icon =
                Icons.Default.Campaign,
            route =
                "marketing"
        )
    )


// ============================================================
// REPORTS
// ============================================================

val reportModules =
    listOf(

        ModuleData(
            name = "Financial Reports",
            description =
                "Profit & Loss summaries",
            icon =
                Icons.Default.Assessment,
            route =
                "reports_finance"
        ),

        ModuleData(
            name = "Work Progress",
            description =
                "Field and site reports",
            icon =
                Icons.Default.Timeline,
            route =
                "reports_work"
        )
    )


// ============================================================
// ADMINISTRATION
// ============================================================

val adminModules =
    listOf(

        ModuleData(
            name = "Admin Hub",
            description =
                "Company and user controls",
            icon =
                Icons.Default.AdminPanelSettings,
            route =
                "admin_hub"
        ),

        ModuleData(
            name = "Settings",
            description =
                "App preferences & Geo-fence",
            icon =
                Icons.Default.Settings,
            route =
                "settings"
        )
    )