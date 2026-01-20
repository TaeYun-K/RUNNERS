package com.runners.app.community.post.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun InfiniteScrollHandler(
    listState: LazyListState,
    buffer: Int = 5,
    enabled: Boolean = true,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(listState, enabled, buffer) {
        if (!enabled) return@LaunchedEffect

        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .map { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                totalItems > 0 && lastVisibleIndex >= totalItems - buffer
            }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }
}
