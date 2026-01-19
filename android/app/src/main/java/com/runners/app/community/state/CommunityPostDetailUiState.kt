package com.runners.app.community.state

import com.runners.app.network.CommunityCommentResult
import com.runners.app.network.CommunityPostDetailResult

data class CommunityPostDetailUiState(
    val post: CommunityPostDetailResult? = null,
    val isPostLoading: Boolean = false,
    val postErrorMessage: String? = null,
    val comments: List<CommunityCommentResult> = emptyList(),
    val commentsNextCursor: String? = null,
    val isCommentsLoading: Boolean = false,
    val commentsErrorMessage: String? = null,
    val commentDraft: String = "",
    val isSubmittingComment: Boolean = false,
    val submitCommentErrorMessage: String? = null,
) {
    val isRefreshing: Boolean get() = isPostLoading || isCommentsLoading
    val canSubmitComment: Boolean get() = commentDraft.trim().isNotBlank() && !isSubmittingComment
}

