package com.runners.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions

private val navItems = listOf(
    AppNavItem(AppRoute.Home, "홈", Icons.Filled.Home),
    AppNavItem(AppRoute.Records, "기록 대시보드", Icons.Filled.Dashboard),
    AppNavItem(AppRoute.Community, "커뮤니티", Icons.Filled.Group),
    AppNavItem(AppRoute.MyPage, "마이페이지", Icons.Filled.Person),
)

@Composable
fun RunnersBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        navItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true
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
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

