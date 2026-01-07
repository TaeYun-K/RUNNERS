package com.runners.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "안녕하세요, ${uiState.nickname}님",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            TotalDistanceHeroCard(
                totalDistanceKm = uiState.totalDistanceKm,
                weekDistanceKm = uiState.weekDistanceKm,
                monthDistanceKm = uiState.monthDistanceKm,
                firstRunDate = uiState.firstRunDate,
            )

            Spacer(modifier = Modifier.height(18.dp))

            RecentRunsCard(
                runs = uiState.recentRuns,
            )

            Spacer(modifier = Modifier.height(18.dp))

            PopularPostsCard(
                posts = uiState.popularPosts,
                onOpenCommunity = onOpenCommunity,
                onPostClick = onPopularPostClick,
            )
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
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp)
    ) {
        Text(
            text = "총 러닝 거리",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = totalDistanceKm?.let { String.format(Locale.US, "%.1f", it) } ?: "—",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "km",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Column(
                modifier = Modifier.widthIn(max = 140.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = spacedBy(6.dp),
            ) {
                SmallMetric(label = "이번 주", valueKm = weekDistanceKm)
                SmallMetric(label = "이번 달", valueKm = monthDistanceKm)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (totalDistanceKm == null) {
                "Health Connect 권한/연결을 확인해주세요."
            } else if (firstRunDate != null) {
                "${firstRunDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN))}부터 달리는 중"
            } else {
                "오늘도 한 걸음 더."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SmallMetric(
    label: String,
    valueKm: Double?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = valueKm?.toKmText() ?: "—",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecentRunsCard(
    runs: List<RecentRunUiModel>,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp)
    ) {
        Text(
            text = "최근 러닝",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (runs.isEmpty()) {
            Text(
                text = "아직 기록이 없어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        Column(verticalArrangement = spacedBy(12.dp)) {
            runs.forEach { run ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = run.date.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val durationText = run.durationMinutes?.let { "${it}분" }
                        if (durationText != null) {
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = run.distanceKm.toKmText(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
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
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "인기 게시글",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "더보기",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onOpenCommunity),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (posts.isEmpty()) {
            Text(
                text = "아직 인기 게시글이 없어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        Column(verticalArrangement = spacedBy(12.dp)) {
            posts.take(5).forEach { post ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPostClick(post.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "♥ ${post.likeCount}  ·  💬 ${post.commentCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun Double.toKmText(): String {
    return String.format(Locale.US, "%.1f km", this)
}
