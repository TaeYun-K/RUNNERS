package com.runners.app.community.post.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.runners.app.ads.CommunityTopBannerAd
import com.runners.app.network.CommunityPostSummaryResult
import coil.compose.AsyncImage
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale

@Composable
fun CommunityPostList(
    posts: List<CommunityPostSummaryResult>,
    listState: LazyListState,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    errorMessage: String?,
    nextCursor: String?,
    showTotalDistance: Boolean,
    onPostClick: (Long) -> Unit,
    onRetryInitial: () -> Unit,
    onRetryMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        posts.isEmpty() && isInitialLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                )
            }
        }

        errorMessage != null && posts.isEmpty() -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = onRetryInitial) {
                        Text("다시 시도")
                    }
                }
            }
        }

        else -> {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.fillMaxSize(),
            ) {
                item(key = "community_top_banner_ad") {
                    CommunityTopBannerAd()
                }

                items(posts, key = { it.postId }) { post ->
                    PostCard(
                        post = post,
                        showTotalDistance = showTotalDistance,
                        onClick = { onPostClick(post.postId) },
                    )
                }

                item {
                    when {
                        isLoadingMore -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        errorMessage != null -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    Button(onClick = onRetryMore, enabled = nextCursor != null) {
                                        Text("다시 시도")
                                    }
                                }
                            }
                        }

                        nextCursor == null && posts.isNotEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "모든 게시글을 불러왔어요",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: CommunityPostSummaryResult,
    showTotalDistance: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (!post.contentPreview.isNullOrBlank()) {
                        Text(
                            text = post.contentPreview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                val thumb = post.thumbnailUrl
                if (!thumb.isNullOrBlank()) {
                    AsyncImage(
                        model = thumb,
                        contentDescription = "게시글 미리보기 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }

            // 작성자 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val authorPicture = post.authorPicture
                if (!authorPicture.isNullOrBlank()) {
                    AsyncImage(
                        model = authorPicture,
                        contentDescription = "작성자 프로필",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (post.authorName?.firstOrNull() ?: '?').toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = post.authorName ?: "익명",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (showTotalDistance) {
                    val km = post.authorTotalDistanceKm
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (km != null) formatKm(km) else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(Modifier.weight(1f))

                val dateLabel = formatCreatedAtForList(post.createdAt)
                if (dateLabel.isNotBlank()) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 통계
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatItem(
                    icon = Icons.Outlined.ThumbUp,
                    count = post.recommendCount,
                )
                StatItem(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    count = post.commentCount,
                )
                StatItem(
                    icon = Icons.Outlined.RemoveRedEye,
                    count = post.viewCount,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatKm(km: Double): String =
    String.format(Locale.getDefault(), "%.1fkm", km.coerceAtLeast(0.0))

private fun formatCreatedAtForList(createdAt: String): String {
    fun parseInstantOrNull(raw: String): Instant? {
        val text = raw.trim().replace(' ', 'T')
        return runCatching { OffsetDateTime.parse(text).toInstant() }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant()
            }.getOrNull()
    }

    val fallback = createdAt.takeIf { it.length >= 10 }?.substring(0, 10) ?: createdAt
    val createdInstant = parseInstantOrNull(createdAt) ?: return fallback

    val minutes = Duration.between(createdInstant, Instant.now()).toMinutes()
    if (minutes < 0) return fallback
    if (minutes < 60) return if (minutes == 0L) "방금 전" else "${minutes}분 전"
    return fallback
}
