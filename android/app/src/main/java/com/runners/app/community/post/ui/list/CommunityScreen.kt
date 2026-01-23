package com.runners.app.community.post.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
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
    val items = listOf(
        null, // 전체
        CommunityPostBoardType.FREE,
        CommunityPostBoardType.QNA,
        CommunityPostBoardType.INFO,
    )

    // 2개씩 나누어 2줄로 배치 (Chunked 사용)
    val chunkedItems = items.chunked(2)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedItems.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { type ->
                    CategoryCard(
                        type = type,
                        onClick = { onBoardClick(type) },
                        modifier = Modifier.weight(1f) // 가로 공간을 균등하게 차지하도록 설정
                    )
                }
                // 만약 홀수 개수일 경우 빈 공간을 채우기 위한 로직 (현재는 4개라 딱 맞음)
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryCard(
    type: CommunityPostBoardType?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // FilterChip 대신 꽉 찬 느낌을 주는 커스텀 카드나 OutlinedButton 사용 추천
    // 여기서는 기존 스타일을 유지하되 크기를 키운 FilterChip 변형을 사용합니다.
    FilterChip(
        selected = false, // TODO: 실제 선택 상태(uiState)를 받아서 처리하면 더 좋습니다.
        onClick = onClick,
        label = {
            Text(
                text = type?.labelKo ?: "전체",
                modifier = Modifier.fillMaxWidth(), // 텍스트 가운데 정렬을 위해 채움
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = MaterialTheme.shapes.medium, // 둥근 사각형 형태로 변경
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier.padding(vertical = 4.dp) // 터치 영역 확보
    )
}
