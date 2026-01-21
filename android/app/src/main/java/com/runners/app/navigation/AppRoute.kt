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
    data object CommunityCreate : AppRoute("community/create")
    data object CommunityPostDetail : AppRoute("community/posts/{postId}") {
        fun createRoute(postId: Long): String = "community/posts/$postId"
    }
    data object CommunityPostEdit : AppRoute("community/posts/{postId}/edit") {
        fun createRoute(postId: Long): String = "community/posts/$postId/edit"
    }
    data object MyPage : AppRoute("mypage")
}

fun shouldShowBottomBar(currentRoute: String?): Boolean {
    if (currentRoute == null) return true

    return when {
        currentRoute.startsWith("community/create") -> false
        currentRoute.startsWith("community/posts/") -> false // detail, edit 모두 포함
        else -> true
    }
}
