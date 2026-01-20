package com.runners.app.community.comment.state

import com.runners.app.network.CommunityCommentResult

data class CommunityCommentUiState(
    val comments: List<CommunityCommentResult> = emptyList(),
    val commentsNextCursor: String? = null,
    val isCommentsLoading: Boolean = false,
    val commentsErrorMessage: String? = null,
    val commentDraft: String = "",
    val replyTargetCommentId: Long? = null,
    val replyTargetAuthorName: String? = null,
    val isSubmittingComment: Boolean = false,
    val submitCommentErrorMessage: String? = null,
    val editingCommentId: Long? = null,
    val editingCommentDraft: String = "",
    val isUpdatingComment: Boolean = false,
    val updateCommentErrorMessage: String? = null,
    val deleteCommentTargetId: Long? = null,
    val isDeletingComment: Boolean = false,
    val deleteCommentErrorMessage: String? = null,
)
