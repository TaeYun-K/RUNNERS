package com.runners.app.community.post.ui.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runners.app.community.post.ui.components.CommunityHeader
import com.runners.app.community.post.ui.components.InfiniteScrollHandler
import com.runners.app.community.post.viewmodel.CommunityViewModel
import com.runners.app.settings.AppSettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    authorNickname: String,
    totalDistanceKm: Double?,
    onCreateClick: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
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

    BackHandler(enabled = uiState.isSearchMode || uiState.isSearchOpen) {
        if (uiState.isSearchMode) {
            viewModel.clearSearchAndRefresh()
        } else {
            viewModel.closeSearch()
        }
    }

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
            onSearchClick = viewModel::toggleSearchOpen,
        )

        if (uiState.isSearchOpen) {
            OutlinedTextField(
                value = uiState.searchInput,
                onValueChange = viewModel::onSearchInputChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isCreating,
                label = { Text("검색") },
                placeholder = { Text("제목/댓글에서 검색") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchInput.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchInputChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.clearSearchAndRefresh()
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.submitSearch() },
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }

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
                onAuthorClick = onAuthorClick,
                onRetryInitial = viewModel::refresh,
                onRetryMore = viewModel::loadMore,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
