package com.runners.app.navigation

import java.time.LocalDate

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Records : AppRoute("records") {
        const val dateArg = "date"
        const val routeWithDate = "records?date={date}"

        fun createRoute(date: LocalDate): String = "records?date=${date}"
    }
    data object Community : AppRoute("community")
    data object Notifications : AppRoute("notifications")
    data object CommunityBoard : AppRoute("community/boards/{boardType}") {
        fun createRoute(boardType: String): String = "community/boards/$boardType"
    }
    data object CommunityCreate : AppRoute("community/create")
    data object CommunityPostDetail : AppRoute("community/posts/{postId}") {
        fun createRoute(postId: Long): String = "community/posts/$postId"
    }
    data object CommunityPostEdit : AppRoute("community/posts/{postId}/edit") {
        fun createRoute(postId: Long): String = "community/posts/$postId/edit"
    }
    data object CommunityUserProfile : AppRoute("community/users/{userId}") {
        fun createRoute(userId: Long): String = "community/users/$userId"
    }
    data object MyPage : AppRoute("mypage")
}

fun shouldShowBottomBar(currentRoute: String?): Boolean {
    if (currentRoute == null) return true

    return when {
        currentRoute.startsWith(AppRoute.Notifications.route) -> false
        currentRoute.startsWith("community/create") -> false
        currentRoute.startsWith("community/boards/") -> false
        currentRoute.startsWith("community/posts/") -> false // detail, edit 모두 포함
        currentRoute.startsWith("community/users/") -> false
        else -> true
    }
}
