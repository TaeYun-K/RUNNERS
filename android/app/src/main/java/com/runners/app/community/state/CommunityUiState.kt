package com.runners.app.community.state

import com.runners.app.network.CommunityPostSummaryResult

data class CommunityUiState(
    val posts: List<CommunityPostSummaryResult> = emptyList(),
    val nextCursor: String? = null,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val listErrorMessage: String? = null,
    val isCreateDialogOpen: Boolean = false,
    val createTitle: String = "",
    val createContent: String = "",
    val isCreating: Boolean = false,
    val createErrorMessage: String? = null,
    val scrollToTopSignal: Long = 0L,
) {
    val isLoading: Boolean get() = isInitialLoading || isLoadingMore
}