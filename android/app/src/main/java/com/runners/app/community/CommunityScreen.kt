package com.runners.app.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.runners.app.network.BackendCommunityApi
import com.runners.app.network.CommunityPostSummaryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CommunityScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var posts by remember { mutableStateOf<List<CommunityPostSummaryResult>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun load(initial: Boolean) {
        if (isLoading) return
        if (!initial && nextCursor == null) return

        isLoading = true
        errorMessage = null
        try {
            val cursor = if (initial) null else nextCursor
            val result = withContext(Dispatchers.IO) { BackendCommunityApi.listPosts(cursor = cursor, size = 20) }
            posts = if (initial) result.posts else posts + result.posts
            nextCursor = result.nextCursor
        } catch (e: Exception) {
            errorMessage = e.message ?: "게시글을 불러오지 못했어요"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        load(initial = true)
    }

    LaunchedEffect(listState, posts.size, nextCursor) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .map { lastVisibleIndex -> posts.isNotEmpty() && lastVisibleIndex >= posts.size - 5 }
            .distinctUntilChanged()
            .filter { it }
            .collectLatest {
                if (!isLoading && nextCursor != null) {
                    load(initial = false)
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("커뮤니티", style = MaterialTheme.typography.headlineSmall)

        when {
            posts.isEmpty() && isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null && posts.isEmpty() -> {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { scope.launch { load(initial = true) } }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(posts, key = { it.postId }) { post ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(post.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = post.authorName ?: "익명",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                HorizontalDivider()
                                Text(
                                    text = "조회 ${post.viewCount} · 추천 ${post.recommendCount} · 댓글 ${post.commentCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                val dateLabel = post.createdAt.takeIf { it.length >= 10 }?.substring(0, 10) ?: post.createdAt
                                if (dateLabel.isNotBlank()) {
                                    Text(
                                        text = dateLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            errorMessage != null -> {
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                                        Button(
                                            onClick = { scope.launch { load(initial = false) } },
                                            enabled = nextCursor != null
                                        ) {
                                            Text("다시 시도")
                                        }
                                    }
                                }
                            }
                            nextCursor == null -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("마지막 게시글이에요", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

