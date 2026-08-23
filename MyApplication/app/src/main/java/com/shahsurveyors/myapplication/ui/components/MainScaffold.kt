package com.shahsurveyors.myapplication.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("dashboard", Icons.Default.Dashboard, "HOME")
    object Attendance : BottomNavItem("attendance", Icons.Default.PunchClock, "ATTENDANCE")
    object Employees : BottomNavItem("employees", Icons.Default.Groups, "EMPLOYEES")
    object Chat : BottomNavItem("chat", Icons.Default.Chat, "CHAT")
    object More : BottomNavItem("more", Icons.Default.MoreHoriz, "MORE")
}

@Composable
fun MainScaffold(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Attendance,
        BottomNavItem.Employees,
        BottomNavItem.Chat,
        BottomNavItem.More
    )

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        content(Modifier.padding(padding))
    }
}

private fun shouldShowBottomBar(route: String?): Boolean {
    return route in listOf("dashboard", "attendance", "employees", "chat", "more")
}
