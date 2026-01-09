package com.runners.app.community.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runners.app.community.viewmodel.CommunityViewModel
import com.runners.app.settings.AppSettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    authorNickname: String,
    totalDistanceKm: Double?,
    onCreateClick: () -> Unit,
    onPostClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val showTotalDistanceInCommunity =
        AppSettingsStore.showTotalDistanceInCommunityFlow(context)
            .collectAsStateWithLifecycle(initialValue = true)
            .value

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
            onCreateClick = onCreateClick,
        )

        PullToRefreshBox(
            isRefreshing = uiState.isInitialLoading,
            onRefresh = {
                if (!uiState.isLoadingMore && !uiState.isCreating) {
                    viewModel.refresh()
                }
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
        ) {
            CommunityPostList(
                posts = uiState.posts,
                listState = listState,
                isInitialLoading = uiState.isInitialLoading,
                isLoadingMore = uiState.isLoadingMore,
                errorMessage = uiState.listErrorMessage,
                nextCursor = uiState.nextCursor,
                showTotalDistance = showTotalDistanceInCommunity,
                onPostClick = onPostClick,
                onRetryInitial = viewModel::refresh,
                onRetryMore = viewModel::loadMore,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
