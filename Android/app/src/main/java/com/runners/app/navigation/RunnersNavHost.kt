package com.runners.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.runners.app.community.CommunityScreen
import com.runners.app.home.HomeScreen
import com.runners.app.home.HomeUiState
import com.runners.app.mypage.MyPageScreen
import com.runners.app.network.GoogleLoginResult
import com.runners.app.records.RecordsDashboardScreen

@Composable
fun RunnersNavHost(
    navController: NavHostController,
    session: GoogleLoginResult,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier,
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen(
                uiState = HomeUiState(
                    nickname = session.name ?: session.email ?: "RUNNERS",
                    monthDistanceKm = 0.0,
                )
            )
        }
        composable(AppRoute.Records.route) { RecordsDashboardScreen() }
        composable(AppRoute.Community.route) { CommunityScreen() }
        composable(AppRoute.MyPage.route) { MyPageScreen() }
    }
}

