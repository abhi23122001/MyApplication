package com.shahsurveyors.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.admin.hasModuleAccess
import com.shahsurveyors.myapplication.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val module: String?
) {
    data object Home : BottomNavItem("dashboard", Icons.Default.Dashboard, "Home", null)
    data object Attendance : BottomNavItem("attendance", Icons.Default.PunchClock, "Attendance", "ATTENDANCE")
    data object Employees : BottomNavItem("employees", Icons.Default.Groups, "Employees", "ADMIN")
    data object Chat : BottomNavItem("chat", Icons.AutoMirrored.Filled.Chat, "Chat", "CHAT")
    data object More : BottomNavItem("more", Icons.Default.MoreHoriz, "More", null)
}

private fun routeModule(route: String?): String? = when (route) {
    "attendance" -> "ATTENDANCE"
    "employees" -> "ADMIN"
    "chat" -> "CHAT"
    "leave" -> "LEAVE"
    "advance_salary" -> "ADVANCE"
    "salary" -> "SALARY"
    "projects" -> "PROJECTS"
    "survey" -> "SURVEY"
    "equipment" -> "EQUIPMENT"
    "tasks" -> "TASKS"
    "dsr" -> "DSR"
    "expense" -> "EXPENSE"
    "billing", "quotes" -> "BILLING"
    "clients" -> "CLIENTS"
    "marketing" -> "MARKETING"
    "employee_reports", "reports_finance", "reports_work" -> "REPORTS"
    "admin_hub", "communication", "company_settings", "bank_details", "terms_conditions", "settings", "employee_permissions" -> "ADMIN"
    else -> null
}

@Composable
fun MainScaffold(currentRoute: String?, onNavigate: (String) -> Unit, content: @Composable (Modifier) -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    var access by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var accessLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(auth.currentUser?.uid) {
        accessLoaded = false
        access = ""
        role = ""
        val uid = auth.currentUser?.uid
        if (uid == null) {
            accessLoaded = true
            return@LaunchedEffect
        }
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                access = doc.getString("access") ?: ""
                role = doc.getString("role") ?: ""
                accessLoaded = true
            }
            .addOnFailureListener {
                accessLoaded = true
            }
    }

    fun allowed(route: String?): Boolean {
        if (route == null || route == "dashboard" || route == "more" || route == "splash" || route == "login" || route == "signup") return true
        val module = routeModule(route) ?: return false
        return hasModuleAccess(access, module)
    }

    val items = listOf(BottomNavItem.Home, BottomNavItem.Attendance, BottomNavItem.Employees, BottomNavItem.Chat, BottomNavItem.More)
        .filter { it.module == null || hasModuleAccess(access, it.module) }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                Surface(
                    modifier = Modifier.navigationBarsPadding(),
                    color = ShahWhite,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                ) {
                    NavigationBar(containerColor = ShahWhite, tonalElevation = 0.dp) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { if (!selected && allowed(item.route)) onNavigate(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, fontSize = 10.sp) },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ShahGreen,
                                    selectedTextColor = ShahGreen,
                                    indicatorColor = ShahLightGreen.copy(alpha = 0.75f),
                                    unselectedIconColor = ShahMediumGrey,
                                    unselectedTextColor = ShahMediumGrey
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (accessLoaded && !allowed(currentRoute)) {
            Box(Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(Modifier.padding(24.dp), colors = CardDefaults.cardColors(containerColor = ShahWhite)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Access Restricted", style = MaterialTheme.typography.titleLarge, color = ShahDarkGreen)
                        Spacer(Modifier.height(8.dp))
                        Text("This module has not been assigned to your account.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { onNavigate("dashboard") }) { Text("GO TO HOME") }
                    }
                }
            }
        } else {
            content(Modifier.padding(paddingValues))
        }
    }
}

private fun shouldShowBottomBar(route: String?): Boolean = route in setOf(
    BottomNavItem.Home.route,
    BottomNavItem.Attendance.route,
    BottomNavItem.Employees.route,
    BottomNavItem.Chat.route,
    BottomNavItem.More.route
)
