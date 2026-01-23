package com.runners.app.community.post.ui.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runners.app.community.post.ui.components.InfiniteScrollHandler
import com.runners.app.community.post.viewmodel.CommunityViewModel
import com.runners.app.network.CommunityPostBoardType
import com.runners.app.settings.AppSettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityBoardScreen(
    boardType: CommunityPostBoardType?,
    onBack: () -> Unit,
    onCreateClick: () -> Unit,
    onPostClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CommunityBoardHeader(
            title = boardType?.let { "${it.labelKo} 게시판" } ?: "전체 게시판",
            onBack = onBack,
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
                        IconButton(onClick = { viewModel.clearSearchAndRefresh() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.submitSearch() }),
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
                selectedBoardType = uiState.selectedBoardType,
                showLatestSection = false,
                showBoardTypeChips = false,
                onPostClick = onPostClick,
                onRetryInitial = viewModel::refresh,
                onRetryMore = viewModel::loadMore,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun CommunityBoardHeader(
    title: String,
    onBack: () -> Unit,
    onCreateClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMenuExpanded = remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSearchClick) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "검색")
            }

            IconButton(onClick = { isMenuExpanded.value = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "더보기")
            }

            DropdownMenu(
                expanded = isMenuExpanded.value,
                onDismissRequest = { isMenuExpanded.value = false },
            ) {
                DropdownMenuItem(
                    text = { Text("글쓰기") },
                    onClick = {
                        isMenuExpanded.value = false
                        onCreateClick()
                    },
                )
            }
        }
    }
}

