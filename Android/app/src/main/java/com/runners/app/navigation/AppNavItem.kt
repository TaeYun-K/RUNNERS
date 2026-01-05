package com.runners.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class AppNavItem(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
)

