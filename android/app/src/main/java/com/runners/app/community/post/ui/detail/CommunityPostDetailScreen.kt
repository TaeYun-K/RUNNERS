package com.runners.app.community.post.ui.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.runners.app.network.CommunityPostDetailResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDetailScreen(
    postId: Long,
    onBack: (CommunityPostDetailResult?) -> Unit,
    onEdit: () -> Unit,
    onDeleted: (Long) -> Unit,
    currentUserId: Long,
    modifier: Modifier = Modifier,
) {
    CommunityPostDetailRoute(
        postId = postId,
        onBack = onBack,
        onEdit = onEdit,
        onDeleted = onDeleted,
        currentUserId = currentUserId,
        modifier = modifier,
    )
}

