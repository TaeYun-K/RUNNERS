package com.runners.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.runners.app.navigation.AppRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnersTopAppBar(
    currentRoute: String?,
    unreadCount: Long,
    onNotificationsClick: () -> Unit,
    onCommunityCreateClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val route = currentRoute.orEmpty()
    val title =
        when {
            route.startsWith(AppRoute.Home.route) -> "홈"
            route.startsWith(AppRoute.Records.route) -> "기록"
            route.startsWith(AppRoute.Community.route) -> "커뮤니티"
            route.startsWith(AppRoute.MyPage.route) -> "마이페이지"
            else -> "RUNNERS"
        }

    val showCommunityCreate = route == AppRoute.Community.route && onCommunityCreateClick != null

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = {
            if (showCommunityCreate) {
                IconButton(onClick = onCommunityCreateClick!!) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = "글쓰기",
                    )
                }
            }

            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0L) {
                            val text = if (unreadCount > 99L) "99+" else unreadCount.toString()
                            Badge { Text(text) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "알림",
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

