package com.runners.app.records

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.runners.app.ui.theme.Blue40
import com.runners.app.ui.theme.Blue60
import com.runners.app.ui.theme.Teal40
import com.runners.app.ui.theme.Teal60
import java.util.Locale

@Composable
fun RecordsDashboardScreen(
    runs: List<RunRecordUiModel> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val totalDistanceKm = runs.sumOf { it.distanceKm }.takeIf { it > 0.0 }
    val totalDurationMinutes = runs.sumOf { it.durationMinutes ?: 0L }.takeIf { it > 0L }
    val runCount = runs.size
    val avgPaceText = formatPaceMinutesPerKm(
        distanceKm = totalDistanceKm,
        totalMinutes = totalDurationMinutes,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 헤더
        Text(
            text = "기록 대시보드",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        RunningCalendarCard(runs = runs)

        // 통계 그리드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Default.DirectionsRun,
                label = "총 누적 거리",
                value = totalDistanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "0.0 km",
                gradientColors = listOf(Blue40, Blue60),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.Schedule,
                label = "총 시간",
                value = totalDurationMinutes?.let { formatTotalTime(it) } ?: "0시간",
                gradientColors = listOf(Teal40, Teal60),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Default.Speed,
                label = "평균 페이스",
                value = avgPaceText,
                gradientColors = listOf(Teal40, Teal60),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Default.DirectionsRun,
                label = "러닝 횟수",
                value = "${runCount}회",
                gradientColors = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF81C784),
                ),
                modifier = Modifier.weight(1f),
            )
        }

        // 안내 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "기록을 시작하려면",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "마이페이지에서 헬스 커넥트를 연동하면 러닝 기록이 자동으로 동기화됩니다. (칼로리/심박 등은 추후 지원)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
        }
    }
}

private fun formatTotalTime(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}

private fun formatPaceMinutesPerKm(distanceKm: Double?, totalMinutes: Long?): String {
    if (distanceKm == null || totalMinutes == null || distanceKm <= 0.0 || totalMinutes <= 0L) return "-'--\""
    val minutesPerKm = totalMinutes.toDouble() / distanceKm
    val wholeMinutes = minutesPerKm.toInt()
    val seconds = ((minutesPerKm - wholeMinutes) * 60).toInt().coerceIn(0, 59)
    return String.format(Locale.US, "%d'%02d\"", wholeMinutes, seconds)
}

@Composable
private fun StatCard(
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(gradientColors)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
