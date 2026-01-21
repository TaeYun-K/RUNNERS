package com.runners.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.runners.app.ui.theme.Blue40
import com.runners.app.ui.theme.Blue60
import com.runners.app.ui.theme.Teal40
import com.runners.app.ui.theme.Teal60
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val nickname: String,
    val totalDistanceKm: Double?,
    val weekDistanceKm: Double?,
    val monthDistanceKm: Double?,
    val firstRunDate: LocalDate?,
    val recentRuns: List<RecentRunUiModel>,
    val popularPosts: List<PopularPostUiModel>,
)

data class RecentRunUiModel(
    val date: LocalDate,
    val distanceKm: Double,
    val durationMinutes: Long?,
)

data class PopularPostUiModel(
    val id: String,
    val title: String,
    val likeCount: Int,
    val commentCount: Int,
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenCommunity: () -> Unit = {},
    onPopularPostClick: (postId: String) -> Unit = {},
    onRecentRunClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = spacedBy(16.dp),
        ) {
            // 환영 헤더
            Column(verticalArrangement = spacedBy(4.dp)) {
                Text(
                    text = "안녕하세요,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${uiState.nickname}님",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            TotalDistanceHeroCard(
                totalDistanceKm = uiState.totalDistanceKm,
                weekDistanceKm = uiState.weekDistanceKm,
                monthDistanceKm = uiState.monthDistanceKm,
                firstRunDate = uiState.firstRunDate,
            )

            // 주간/월간 통계 그리드
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = spacedBy(12.dp),
            ) {
                StatMiniCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "이번 주",
                    value = uiState.weekDistanceKm?.toKmText() ?: "0.0 km",
                    gradientColors = listOf(Blue40, Blue60),
                    modifier = Modifier.weight(1f),
                )
                StatMiniCard(
                    icon = Icons.Filled.TrendingUp,
                    label = "이번 달",
                    value = uiState.monthDistanceKm?.toKmText() ?: "0.0 km",
                    gradientColors = listOf(Teal40, Teal60),
                    modifier = Modifier.weight(1f),
                )
            }

            RecentRunsCard(
                runs = uiState.recentRuns,
                onRunClick = onRecentRunClick,
            )

            PopularPostsCard(
                posts = uiState.popularPosts,
                onOpenCommunity = onOpenCommunity,
                onPostClick = onPopularPostClick,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TotalDistanceHeroCard(
    totalDistanceKm: Double?,
    weekDistanceKm: Double?,
    monthDistanceKm: Double?,
    firstRunDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Blue40, Blue60)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column {
                    Text(
                        text = "총 누적 거리",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = totalDistanceKm?.let { String.format(Locale.US, "%.1f", it) } ?: "0.0",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "km",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (totalDistanceKm == null) {
                    "Health Connect 권한을 확인해주세요"
                } else if (firstRunDate != null) {
                    "${firstRunDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN))}부터 달리는 중"
                } else {
                    "오늘도 한 걸음 더 나아가요"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun StatMiniCard(
    icon: ImageVector,
    label: String,
    value: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.linearGradient(gradientColors)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(verticalArrangement = spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun RecentRunsCard(
    runs: List<RecentRunUiModel>,
    onRunClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "최근 러닝",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (runs.isEmpty()) {
                Text(
                    text = "아직 기록이 없어요. 첫 러닝을 시작해보세요!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = spacedBy(12.dp)) {
                    runs.forEach { run ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onRunClick(run.date) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                                Column {
                                    Text(
                                        text = run.date.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    run.durationMinutes?.let { minutes ->
                                        Text(
                                            text = "${minutes}분",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            Text(
                                text = run.distanceKm.toKmText(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PopularPostsCard(
    posts: List<PopularPostUiModel>,
    onOpenCommunity: () -> Unit,
    onPostClick: (postId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenCommunity),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "인기 게시글",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "더보기",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (posts.isEmpty()) {
                Text(
                    text = "아직 인기 게시글이 없어요. 커뮤니티에서 첫 글을 작성해보세요!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = spacedBy(12.dp)) {
                    posts.take(5).forEach { post ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPostClick(post.id) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${post.likeCount} · ${post.commentCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Double.toKmText(): String {
    return String.format(Locale.US, "%.1f km", this)
}
