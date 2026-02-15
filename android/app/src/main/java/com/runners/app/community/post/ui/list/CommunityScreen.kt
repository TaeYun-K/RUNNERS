package com.runners.app.community.post.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runners.app.community.post.viewmodel.CommunityViewModel
import com.runners.app.network.CommunityPostBoardType

@Composable
fun CommunityScreen(
  authorNickname: String,
  totalDistanceKm: Double?,
  onPostClick: (Long) -> Unit,
  onBoardClick: (CommunityPostBoardType?) -> Unit,
  onMyPostsClick: () -> Unit = {},
  onMyCommentsClick: () -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: CommunityViewModel = viewModel(),
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    item(key = "my_activity_header") {
      SectionHeader(title = "내 활동")
    }

    item(key = "my_posts_item") {
      NavigationListItem(
        title = "내가 쓴 글",
        icon = Icons.Default.EditNote,
        trailingText = "${uiState.myPostsCountText ?: "-"}건",
        onClick = onMyPostsClick,
      )
    }

    item(key = "my_comments_item") {
      NavigationListItem(
        title = "댓글 단 글",
        icon = Icons.Default.ModeComment,
        trailingText = "${uiState.myCommentedCountText ?: "-"}건",
        onClick = onMyCommentsClick,
      )
    }

    item(key = "section_divider") {
      HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
      )
    }

    item(key = "board_header") {
      SectionHeader(title = "게시판")
    }

    item(key = "board_list") {
      BoardNavigationSection(onBoardClick = onBoardClick)
    }

    item(key = "latest_spacer") {
      Spacer(modifier = Modifier.height(8.dp))
    }

    item(key = "latest_header") {
      SectionHeader(title = "지금 올라온 글")
    }

    item(key = "latest_content") {
      if (uiState.latestPosts.isNotEmpty()) {
        LatestPostsSection(
          posts = uiState.latestPosts.take(10),
          onPostClick = onPostClick,
        )
      } else if (uiState.listErrorMessage != null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
          contentAlignment = Alignment.Center,
        ) {
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

private data class BoardNavigationItem(
  val type: CommunityPostBoardType?,
  val label: String,
  val icon: ImageVector,
)

@Composable
private fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = modifier.padding(bottom = 2.dp),
  )
}

@Composable
private fun BoardNavigationSection(
  onBoardClick: (CommunityPostBoardType?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val boardItems = listOf(
    BoardNavigationItem(
      type = null,
      label = "전체보기",
      icon = Icons.Default.Apps,
    ),
    BoardNavigationItem(
      type = CommunityPostBoardType.FREE,
      label = "자유게시판",
      icon = Icons.Default.ChatBubbleOutline,
    ),
    BoardNavigationItem(
      type = CommunityPostBoardType.QNA,
      label = "질문답변",
      icon = Icons.Default.HelpOutline,
    ),
    BoardNavigationItem(
      type = CommunityPostBoardType.INFO,
      label = "정보공유",
      icon = Icons.Default.Info,
    ),
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    boardItems.forEach { board ->
      NavigationListItem(
        title = board.label,
        icon = board.icon,
        onClick = { onBoardClick(board.type) },
      )
    }
  }
}

@Composable
private fun NavigationListItem(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  supportingText: String? = null,
  trailingText: String? = null,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp),
    )

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      if (!supportingText.isNullOrBlank()) {
        Text(
          text = supportingText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    if (!trailingText.isNullOrBlank()) {
      Text(
        text = trailingText,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
      )
    } else {
      Icon(
        imageVector = Icons.Default.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}
