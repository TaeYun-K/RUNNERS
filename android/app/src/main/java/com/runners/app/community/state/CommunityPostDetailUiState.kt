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
    val editingCommentId: Long? = null,
    val editingCommentDraft: String = "",
    val isUpdatingComment: Boolean = false,
    val updateCommentErrorMessage: String? = null,
    val isUpdatingPost: Boolean = false,
    val updatePostErrorMessage: String? = null,
    val isDeletingPost: Boolean = false,
    val deletePostErrorMessage: String? = null,
    val deleteSuccessSignal: Long = 0L,
) {
    val isRefreshing: Boolean get() = isPostLoading || isCommentsLoading
    val canSubmitComment: Boolean get() = commentDraft.trim().isNotBlank() && !isSubmittingComment
}
