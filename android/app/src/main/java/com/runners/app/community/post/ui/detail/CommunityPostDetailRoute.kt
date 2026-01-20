package com.runners.app.community.post.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runners.app.community.post.viewmodel.CommunityPostDetailViewModel
import com.runners.app.network.CommunityPostDetailResult
import com.runners.app.settings.AppSettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityPostDetailRoute(
    postId: Long,
    onBack: (CommunityPostDetailResult?) -> Unit,
    onEdit: () -> Unit,
    onDeleted: (Long) -> Unit,
    currentUserId: Long,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: CommunityPostDetailViewModel =
        viewModel(
            key = "CommunityPostDetailViewModel:$postId",
            factory = CommunityPostDetailViewModel.Factory(postId = postId),
        )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val initialDeleteSignal = remember(postId) { uiState.deleteSuccessSignal }
    val showTotalDistanceInCommunity =
        AppSettingsStore.showTotalDistanceInCommunityFlow(context)
            .collectAsStateWithLifecycle(initialValue = true)
            .value

    LaunchedEffect(uiState.deleteSuccessSignal) {
        if (uiState.deleteSuccessSignal > initialDeleteSignal) {
            onDeleted(postId)
        }
    }

    BackHandler(enabled = true) {
        onBack(uiState.post)
    }

    CommunityPostDetailContent(
        uiState = uiState,
        currentUserId = currentUserId,
        showTotalDistance = showTotalDistanceInCommunity,
        onBack = { onBack(uiState.post) },
        onEdit = onEdit,
        onRefresh = viewModel::refresh,
        onCommentDraftChange = viewModel::onCommentDraftChange,
        onStartReply = viewModel::startReply,
        onCancelReply = viewModel::cancelReply,
        onSubmitComment = viewModel::submitComment,
        onStartEditingComment = viewModel::startEditingComment,
        onCancelEditingComment = viewModel::cancelEditingComment,
        onEditingCommentDraftChange = viewModel::onEditingCommentDraftChange,
        onSubmitEditingComment = viewModel::submitEditingComment,
        onRequestDeleteComment = viewModel::requestDeleteComment,
        onCancelDeleteComment = viewModel::cancelDeleteComment,
        onConfirmDeleteComment = viewModel::confirmDeleteComment,
        onLoadMoreComments = viewModel::loadMoreComments,
        onDeletePost = viewModel::deletePost,
        modifier = modifier,
    )
}
