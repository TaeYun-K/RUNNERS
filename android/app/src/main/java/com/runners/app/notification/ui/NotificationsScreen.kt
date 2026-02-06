package com.runners.app.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.runners.app.community.post.ui.components.InfiniteScrollHandler
import com.runners.app.network.BackendNotificationsApi
import com.runners.app.network.NotificationResult
import com.runners.app.network.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenPost: (Long) -> Unit,
    onNotificationsChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var notifications by remember { mutableStateOf<List<NotificationResult>>(emptyList()) }
    var hasNext by remember { mutableStateOf(false) }
    var nextCursor by remember { mutableStateOf<String?>(null) }

    var isInitialLoading by remember { mutableStateOf(false) }
    var isMoreLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isReadAllConfirmOpen by remember { mutableStateOf(false) }

    suspend fun loadInitial() {
        if (isInitialLoading) return
        isInitialLoading = true
        errorMessage = null
        try {
            val page = withContext(Dispatchers.IO) { BackendNotificationsApi.listNotifications(cursor = null, size = 20) }
            notifications = page.notifications
            hasNext = page.hasNext
            nextCursor = page.nextCursor
        } catch (e: Exception) {
            errorMessage = e.message ?: "알림을 불러오지 못했어요"
        } finally {
            isInitialLoading = false
        }
    }

    suspend fun loadMore() {
        if (isInitialLoading || isMoreLoading || !hasNext) return
        val cursor = nextCursor ?: return
        isMoreLoading = true
        try {
            val page = withContext(Dispatchers.IO) { BackendNotificationsApi.listNotifications(cursor = cursor, size = 20) }
            notifications = notifications + page.notifications
            hasNext = page.hasNext
            nextCursor = page.nextCursor
        } catch (e: Exception) {
            errorMessage = e.message ?: "알림을 더 불러오지 못했어요"
        } finally {
            isMoreLoading = false
        }
    }

    fun markReadLocal(notificationId: Long) {
        notifications =
            notifications.map { item ->
                if (item.id == notificationId) item.copy(isRead = true, readAt = item.readAt ?: item.createdAt)
                else item
            }
    }

    LaunchedEffect(Unit) { loadInitial() }

    InfiniteScrollHandler(
        listState = listState,
        enabled = hasNext && !isInitialLoading && !isMoreLoading,
        onLoadMore = { scope.launch { loadMore() } },
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("알림") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { isReadAllConfirmOpen = true },
                        enabled = notifications.any { !it.isRead } && !isInitialLoading,
                    ) { Text("전체 읽음") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        when {
            isInitialLoading && notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null && notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text(
                            text = errorMessage ?: "오류가 발생했어요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(onClick = { scope.launch { loadInitial() } }) {
                            Text("다시 시도")
                        }
                    }
                }
            }

            notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "새 알림이 없어요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                ) {
                    items(items = notifications, key = { it.id }) { item ->
                        NotificationRow(
                            item = item,
                            onClick = {
                                scope.launch {
                                    if (!item.isRead) {
                                        runCatching {
                                            withContext(Dispatchers.IO) { BackendNotificationsApi.markAsRead(item.id) }
                                            markReadLocal(item.id)
                                            onNotificationsChanged()
                                        }
                                    }

                                    item.relatedPostId?.let(onOpenPost)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (isMoreLoading) {
                        item(key = "loading-more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                    } else {
                        item(key = "bottom-spacer") { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    if (isReadAllConfirmOpen) {
        AlertDialog(
            onDismissRequest = { isReadAllConfirmOpen = false },
            title = { Text("전체 읽음") },
            text = { Text("모든 알림을 읽음 처리할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isReadAllConfirmOpen = false
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { BackendNotificationsApi.markAllAsRead() }
                                notifications = notifications.map { it.copy(isRead = true, readAt = it.readAt ?: it.createdAt) }
                                onNotificationsChanged()
                            }.onFailure { e ->
                                errorMessage = e.message ?: "전체 읽음 처리에 실패했어요"
                            }
                        }
                    },
                    enabled = notifications.any { !it.isRead } && !isInitialLoading,
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { isReadAllConfirmOpen = false }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun NotificationRow(
    item: NotificationResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container =
        if (item.isRead) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceContainerLow

    Surface(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        color = container,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NotificationAvatar(
                pictureUrl = item.actorPicture,
                fallbackText = item.actorName?.firstOrNull()?.toString() ?: "R",
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = notificationTitle(item),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatTimeText(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NotificationAvatar(
    pictureUrl: String?,
    fallbackText: String,
    modifier: Modifier = Modifier,
) {
    if (!pictureUrl.isNullOrBlank()) {
        AsyncImage(
            model = pictureUrl,
            contentDescription = null,
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape),
        )
    } else {
        Box(
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fallbackText.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private fun notificationTitle(item: NotificationResult): String {
    val actor = item.actorName?.takeUnless { it.isBlank() } ?: "누군가"
    return when (item.type) {
        NotificationType.COMMENT_ON_MY_POST -> "${actor}님이 내 글에 댓글을 남겼어요"
        NotificationType.COMMENT_ON_MY_COMMENTED_POST -> "${actor}님이 내가 댓글단 글에 댓글을 남겼어요"
        NotificationType.REPLY_TO_MY_COMMENT -> "${actor}님이 내 댓글에 답글을 남겼어요"
        NotificationType.UNKNOWN -> "새 알림이 도착했어요"
    }
}

private fun formatTimeText(raw: String): String {
    if (raw.isBlank()) return ""
    val normalized = raw.trim().replace('T', ' ')
    return if (normalized.length >= 16) normalized.substring(0, 16) else normalized
}

