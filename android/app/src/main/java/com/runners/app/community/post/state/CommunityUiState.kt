package com.runners.app.community.post.state

import com.runners.app.network.CommunityPostSummaryResult

data class CommunityUiState(
    val searchInput: String = "",
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val posts: List<CommunityPostSummaryResult> = emptyList(),
    val nextCursor: String? = null,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val listErrorMessage: String? = null,
    val createTitle: String = "",
    val createContent: String = "",
    val createImageUris: List<String> = emptyList(),
    val isCreating: Boolean = false,
    val createErrorMessage: String? = null,
    val createSuccessSignal: Long = 0L,
    val scrollToTopSignal: Long = 0L,
) {
    val isLoading: Boolean get() = isInitialLoading || isLoadingMore
    val isSearchMode: Boolean get() = searchQuery.isNotBlank()
}
