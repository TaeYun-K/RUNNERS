package com.runners.app.community.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun CommunityAuthorLine(
    nickname: String,
    totalDistanceKm: Double?,
    showTotalDistance: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = nickname.ifBlank { "RUNNERS" },
            style = MaterialTheme.typography.titleSmall,
        )

        if (showTotalDistance) {
            Spacer(Modifier.width(8.dp))
            val distanceLabel = totalDistanceKm?.let { "· ${formatKm(it)}" } ?: "· km 정보 없음"
            Text(
                text = distanceLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatKm(km: Double): String =
    String.format(Locale.getDefault(), "%.1fkm", km.coerceAtLeast(0.0))

