package com.runners.app.community.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runners.app.community.viewmodel.CommunityViewModel

@Composable
fun CommunityScreen(
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val listState = rememberLazyListState()

    InfiniteScrollHandler(
        listState = listState,
        buffer = 5,
        enabled = uiState.posts.isNotEmpty() &&
                uiState.nextCursor != null &&
                !uiState.isInitialLoading &&
                !uiState.isLoadingMore,
        onLoadMore = viewModel::loadMore,
    )

    LaunchedEffect(uiState.scrollToTopSignal) {
        if (uiState.scrollToTopSignal > 0L) {
            runCatching { listState.animateScrollToItem(0) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CommunityHeader(
            title = "커뮤니티",
            onCreateClick = viewModel::openCreateDialog,
        )

        CommunityCreatePostDialog(
            isOpen = uiState.isCreateDialogOpen,
            title = uiState.createTitle,
            onTitleChange = viewModel::onCreateTitleChange,
            content = uiState.createContent,
            onContentChange = viewModel::onCreateContentChange,
            isCreating = uiState.isCreating,
            errorMessage = uiState.createErrorMessage,
            onSubmit = viewModel::submitCreatePost,
            onDismiss = viewModel::closeCreateDialog,
        )

        CommunityPostList(
            posts = uiState.posts,
            listState = listState,
            isInitialLoading = uiState.isInitialLoading,
            isLoadingMore = uiState.isLoadingMore,
            errorMessage = uiState.listErrorMessage,
            nextCursor = uiState.nextCursor,
            onRetryInitial = viewModel::refresh,
            onRetryMore = viewModel::loadMore,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
