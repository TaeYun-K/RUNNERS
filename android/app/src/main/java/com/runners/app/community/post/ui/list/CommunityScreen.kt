package com.runners.app.community.post.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runners.app.community.post.ui.components.CommunityHeader
import com.runners.app.community.post.viewmodel.CommunityViewModel
import com.runners.app.network.CommunityPostBoardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    authorNickname: String,
    totalDistanceKm: Double?,
    onCreateClick: () -> Unit,
    onPostClick: (Long) -> Unit,
    onBoardClick: (CommunityPostBoardType?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CommunityHeader(
            title = "커뮤니티",
            onCreateClick = onCreateClick,
            onSearchClick = { onBoardClick(null) },
        )

        if (uiState.latestPosts.isNotEmpty()) {
            LatestPostsSection(
                posts = uiState.latestPosts.take(10),
                onPostClick = onPostClick,
            )
        } else if (uiState.listErrorMessage != null) {
            Text(
                text = uiState.listErrorMessage ?: "최신 글을 불러오지 못했어요",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        BoardTypeNavRow(onBoardClick = onBoardClick)
    }
}

@Composable
private fun BoardTypeNavRow(
    onBoardClick: (CommunityPostBoardType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items =
        listOf(
            null,
            CommunityPostBoardType.FREE,
            CommunityPostBoardType.QNA,
            CommunityPostBoardType.INFO,
        )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { type ->
            FilterChip(
                selected = false,
                onClick = { onBoardClick(type) },
                label = { Text(type?.labelKo ?: "전체") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
