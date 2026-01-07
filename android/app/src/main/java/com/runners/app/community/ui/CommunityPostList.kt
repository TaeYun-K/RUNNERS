package com.runners.app.community.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.runners.app.network.CommunityPostSummaryResult

@Composable
fun CommunityPostList(
    posts: List<CommunityPostSummaryResult>,
    listState: LazyListState,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    errorMessage: String?,
    nextCursor: String?,
    onRetryInitial: () -> Unit,
    onRetryMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        posts.isEmpty() && isInitialLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null && posts.isEmpty() -> {
            Card(modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetryInitial) {
                        Text("다시 시도")
                    }
                }
            }
        }

        else -> {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = modifier.fillMaxSize(),
            ) {
                items(posts, key = { it.postId }) { post ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(post.title, style = MaterialTheme.typography.titleMedium)
                            if (!post.contentPreview.isNullOrBlank()) {
                                Text(
                                    text = post.contentPreview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
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
                            val dateLabel =
                                post.createdAt.takeIf { it.length >= 10 }?.substring(0, 10) ?: post.createdAt
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
                        isLoadingMore -> {
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
                                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                                    Button(onClick = onRetryMore, enabled = nextCursor != null) {
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
                                Text("더이상 게시글이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
