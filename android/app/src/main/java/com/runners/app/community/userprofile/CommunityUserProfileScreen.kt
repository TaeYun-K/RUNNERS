package com.runners.app.community.userprofile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.runners.app.network.BackendUserApi
import com.runners.app.network.UserPublicProfileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CommunityUserProfileScreen(
    userId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<UserPublicProfileResult?>(null) }

    suspend fun refresh() {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        try {
            profile = withContext(Dispatchers.IO) { BackendUserApi.getPublicProfile(userId) }
        } catch (e: Exception) {
            errorMessage = e.message ?: "프로필을 불러오지 못했어요"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(userId) { refresh() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("프로필") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        when {
            isLoading && profile == null -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null && profile == null -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = errorMessage ?: "오류가 발생했어요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }

            else -> {
                val data = profile ?: return@Scaffold
                val displayName = data.displayName.ifBlank { "RUNNERS" }
                val intro = data.intro?.takeUnless { it.isBlank() } ?: "소개가 없어요"

                val totalDistanceKm = data.totalDistanceKm?.takeIf { it > 0.0 }
                val totalDurationMinutes = data.totalDurationMinutes?.takeIf { it > 0L }
                val runCount = (data.runCount ?: 0).coerceAtLeast(0)
                val avgPaceText = formatPaceMinutesPerKm(
                    distanceKm = totalDistanceKm,
                    totalMinutes = totalDurationMinutes,
                )

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val pictureUrl = data.picture
                    if (!pictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = pictureUrl,
                            contentDescription = "프로필 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (displayName.firstOrNull() ?: 'R').toString().uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = intro,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StatCard(
                            icon = Icons.Default.DirectionsRun,
                            label = "총 누적 거리",
                            value = totalDistanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "0.0 km",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon = Icons.Default.Schedule,
                            label = "총 시간",
                            value = totalDurationMinutes?.let { formatTotalTime(it) } ?: "0시간",
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
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon = Icons.Default.DirectionsRun,
                            label = "러닝 횟수",
                            value = "${runCount}회",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = shape,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
