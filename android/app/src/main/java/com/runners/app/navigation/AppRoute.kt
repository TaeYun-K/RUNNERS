package com.runners.app.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Records : AppRoute("records")
    data object Community : AppRoute("community")
    data object CommunityCreate : AppRoute("community/create")
    data object MyPage : AppRoute("mypage")
}

