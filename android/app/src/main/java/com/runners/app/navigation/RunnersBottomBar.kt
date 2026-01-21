package com.runners.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions

private data class NavItemData(
    val route: AppRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    NavItemData(AppRoute.Home, "홈", Icons.Filled.Home, Icons.Outlined.Home),
    NavItemData(AppRoute.Records, "기록", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    NavItemData(AppRoute.Community, "커뮤니티", Icons.Filled.Group, Icons.Outlined.Group),
    NavItemData(AppRoute.MyPage, "MY", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun RunnersBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        navItems.forEach { item ->
            val selected =
                currentDestination?.hierarchy?.any { destination ->
                    val route = destination.route ?: return@any false
                    route == item.route.route ||
                        route.startsWith("${item.route.route}/") ||
                        route.startsWith("${item.route.route}?")
                } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(
                        route = item.route.route,
                        navOptions = navOptions {
                            popUpTo(AppRoute.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
