package com.shahsurveyors.myapplication.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahLightGreen
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem("dashboard", Icons.Default.Dashboard, "Home")
    data object Attendance : BottomNavItem("attendance", Icons.Default.PunchClock, "Attendance")
    data object Employees : BottomNavItem("employees", Icons.Default.Groups, "Employees")
    data object Chat : BottomNavItem("chat", Icons.Default.Chat, "Chat")
    data object More : BottomNavItem("more", Icons.Default.MoreHoriz, "More")
}

@Composable
fun MainScaffold(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Attendance, BottomNavItem.Employees, BottomNavItem.Chat, BottomNavItem.More)

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                Surface(
                    modifier = Modifier.navigationBarsPadding(),
                    color = ShahWhite,
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                ) {
                    NavigationBar(containerColor = ShahWhite, tonalElevation = 0.dp) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { if (!selected) onNavigate(item.route) },
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
    ) { paddingValues -> content(Modifier.padding(paddingValues)) }
}

private fun shouldShowBottomBar(route: String?): Boolean = route in setOf(
    BottomNavItem.Home.route,
    BottomNavItem.Attendance.route,
    BottomNavItem.Employees.route,
    BottomNavItem.Chat.route,
    BottomNavItem.More.route
)
