package com.shahsurveyors.myapplication.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(
        route = "dashboard",
        icon = Icons.Default.Dashboard,
        label = "HOME"
    )

    data object Attendance : BottomNavItem(
        route = "attendance",
        icon = Icons.Default.PunchClock,
        label = "ATTENDANCE"
    )

    data object Employees : BottomNavItem(
        route = "employees",
        icon = Icons.Default.Groups,
        label = "EMPLOYEES"
    )

    data object Tasks : BottomNavItem(
        route = "tasks",
        icon = Icons.Default.Assignment,
        label = "TASKS"
    )

    data object Chat : BottomNavItem(
        route = "chat",
        icon = Icons.Default.Chat,
        label = "CHAT"
    )

    data object More : BottomNavItem(
        route = "more",
        icon = Icons.Default.MoreHoriz,
        label = "MORE"
    )
}

@Composable
fun MainScaffold(
    currentRoute: String?,
    isAdmin: Boolean = false,
    onNavigate: (String) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val items = if (isAdmin) {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Attendance,
            BottomNavItem.Employees,
            BottomNavItem.Chat,
            BottomNavItem.More
        )
    } else {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Attendance,
            BottomNavItem.Tasks,
            BottomNavItem.Chat,
            BottomNavItem.More
        )
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    onNavigate(item.route)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(text = item.label)
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}

private fun shouldShowBottomBar(route: String?): Boolean {
    return route in setOf(
        BottomNavItem.Home.route,
        BottomNavItem.Attendance.route,
        BottomNavItem.Employees.route,
        BottomNavItem.Tasks.route,
        BottomNavItem.Chat.route,
        BottomNavItem.More.route
    )
}