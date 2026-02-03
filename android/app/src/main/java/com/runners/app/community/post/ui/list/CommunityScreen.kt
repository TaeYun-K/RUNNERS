package com.runners.app.community.post.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MapsUgc
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    onMyPostsClick: () -> Unit = {},
    onMyCommentsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // 배경색 명시
    ) {
        // 1. 헤더 (상단 고정)
        CommunityHeader(
            title = "커뮤니티",
            onCreateClick = onCreateClick,
            onSearchClick = { onBoardClick(null) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            // 2. 내 활동 섹션 (내가 쓴 글/댓글)
            MyActivitySection(
                onMyPostsClick = onMyPostsClick,
                onMyCommentsClick = onMyCommentsClick
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

            // 3. 게시판 네비게이션 (2x2 그리드)
            BoardTypeNavGrid(onBoardClick = onBoardClick)

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

            // 4. 최신 게시글 섹션 (구분선이나 배경을 넣어 섹션 분리감 강조)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "지금 올라온 글",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                if (uiState.latestPosts.isNotEmpty()) {
                    LatestPostsSection(
                        posts = uiState.latestPosts.take(10),
                        onPostClick = onPostClick,
                    )
                } else if (uiState.listErrorMessage != null) {
                    // 에러 메시지 UI 개선
                    Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            text = uiState.listErrorMessage ?: "최신 글을 불러오지 못했어요",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Box(x0: Modifier, contentAlignment: Alignment, content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}

@Composable
private fun BoardTypeNavGrid(
    onBoardClick: (CommunityPostBoardType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        null to ("전체보기" to androidx.compose.material.icons.Icons.Default.Apps),
        CommunityPostBoardType.FREE to ("자유게시판" to androidx.compose.material.icons.Icons.Default.ChatBubbleOutline),
        CommunityPostBoardType.QNA to ("질문답변" to androidx.compose.material.icons.Icons.Default.HelpOutline),
        CommunityPostBoardType.INFO to ("정보공유" to androidx.compose.material.icons.Icons.Default.Info),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { (type, info) ->
                    val (label, icon) = info
                    CategoryNavigationCard(
                        label = label,
                        icon = icon,
                        onClick = { onBoardClick(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryNavigationCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp), // 높이를 고정하여 안정감 부여
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow, // 은은한 배경색
        tonalElevation = 2.dp,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActivitySmallCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    pointColor: androidx.compose.ui.graphics.Color, // 포인트 컬러 전달
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface, // 깨끗한 화이트/표면색
        tonalElevation = 1.dp,
        shadowElevation = 0.dp, // 그림자 대신 보더를 살짝 주어 깔끔하게
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 아이콘 영역: 배경색을 빼고 아이콘 자체 컬러 강조
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = pointColor
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = count,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " 건",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyActivitySection(
    onMyPostsClick: () -> Unit,
    onMyCommentsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActivitySmallCard(
            title = "내가 쓴 글",
            count = "12",
            icon = androidx.compose.material.icons.Icons.Default.EditNote,
            pointColor = MaterialTheme.colorScheme.primary, // 파란색 계열 포인트
            onClick = onMyPostsClick,
            modifier = Modifier.weight(1f)
        )
        ActivitySmallCard(
            title = "댓글 단 글",
            count = "5",
            icon = androidx.compose.material.icons.Icons.Default.ModeComment,
            pointColor = MaterialTheme.colorScheme.tertiary, // 녹색/보라색 계열 포인트로 차별화
            onClick = onMyCommentsClick,
            modifier = Modifier.weight(1f)
        )
    }
}