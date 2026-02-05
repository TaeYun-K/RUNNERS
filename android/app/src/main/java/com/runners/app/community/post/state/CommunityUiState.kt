package com.runners.app.community.post.state

import com.runners.app.network.CommunityPostBoardType
import com.runners.app.network.CommunityPostSummaryResult

enum class CommunityListMode {
    BOARD,
    MY_POSTS,
    MY_COMMENTED,
}

data class CommunityUiState(
    val searchInput: String = "",
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val selectedBoardType: CommunityPostBoardType? = null,
    val latestPosts: List<CommunityPostSummaryResult> = emptyList(),
    val posts: List<CommunityPostSummaryResult> = emptyList(),
    val nextCursor: String? = null,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val listErrorMessage: String? = null,
    val createBoardType: CommunityPostBoardType = CommunityPostBoardType.FREE,
    val createTitle: String = "",
    val createContent: String = "",
    val createImageUris: List<String> = emptyList(),
    val isCreating: Boolean = false,
    val createErrorMessage: String? = null,
    val createSuccessSignal: Long = 0L,
    val lastCreatedPostId: Long? = null,
    val scrollToTopSignal: Long = 0L,
    val listMode: CommunityListMode = CommunityListMode.BOARD,
    val myPostsCountText: String? = null,
    val myCommentedCountText: String? = null,
) {
    val isLoading: Boolean get() = isInitialLoading || isLoadingMore
    val isSearchMode: Boolean get() = searchQuery.isNotBlank()
}
