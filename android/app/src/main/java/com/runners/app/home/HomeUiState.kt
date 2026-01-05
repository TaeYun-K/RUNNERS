package com.runners.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

data class HomeUiState(
    val nickname: String,
    val totalDistanceKm: Double?,
    val weekDistanceKm: Double?,
    val monthDistanceKm: Double?,
    val recentRunDistanceKm: Double?,
    val recentRunDate: LocalDate?,
    val firstRunDate: LocalDate?,
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // 상단: 인사 + 닉네임
            Text(
                text = "안녕하세요,",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = uiState.nickname,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SummaryCardRow(
                totalDistanceKm = uiState.totalDistanceKm,
                weekDistanceKm = uiState.weekDistanceKm,
                monthDistanceKm = uiState.monthDistanceKm,
            )

            if (uiState.firstRunDate != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${uiState.firstRunDate}부터 러닝 중",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            RecentRunCard(
                distanceKm = uiState.recentRunDistanceKm,
                date = uiState.recentRunDate,
            )
        }
    }
}

@Composable
private fun SummaryCardRow(
    totalDistanceKm: Double?,
    weekDistanceKm: Double?,
    monthDistanceKm: Double?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp)
    ) {
        Text(
            text = "러닝 거리",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        MetricRow(label = "총 거리", valueKm = totalDistanceKm)
        Spacer(modifier = Modifier.height(8.dp))
        MetricRow(label = "이번 주", valueKm = weekDistanceKm)
        Spacer(modifier = Modifier.height(8.dp))
        MetricRow(label = "이번 달", valueKm = monthDistanceKm)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (totalDistanceKm == null) "Health Connect 권한/연결을 확인해주세요." else "꾸준히 쌓이는 중 🔥",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricRow(
    label: String,
    valueKm: Double?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = valueKm?.let { String.format("%.1f km", it) } ?: "—",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecentRunCard(
    distanceKm: Double?,
    date: LocalDate?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp)
    ) {
        Text(
            text = "최근 러닝",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when {
                distanceKm == null && date == null -> "—"
                distanceKm != null && date != null -> "${String.format("%.1f", distanceKm)} km · $date"
                distanceKm != null -> "${String.format("%.1f", distanceKm)} km"
                else -> "$date"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
