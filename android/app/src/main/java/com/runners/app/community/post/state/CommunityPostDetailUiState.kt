package com.runners.app.community.post.state

import com.runners.app.community.comment.state.CommunityCommentUiState
import com.runners.app.network.CommunityPostDetailResult

data class CommunityPostDetailPostUiState(
    val post: CommunityPostDetailResult? = null,
    val isPostLoading: Boolean = false,
    val postErrorMessage: String? = null,
    val isUpdatingPost: Boolean = false,
    val updatePostErrorMessage: String? = null,
    val isDeletingPost: Boolean = false,
    val deletePostErrorMessage: String? = null,
    val deleteSuccessSignal: Long = 0L,
)

data class CommunityPostDetailUiState(
    val postState: CommunityPostDetailPostUiState = CommunityPostDetailPostUiState(),
    val commentState: CommunityCommentUiState = CommunityCommentUiState(),
) {
    // Backward-compatible getters (UI expects flat fields).
    val post: CommunityPostDetailResult? get() = postState.post
    val isPostLoading: Boolean get() = postState.isPostLoading
    val postErrorMessage: String? get() = postState.postErrorMessage
    val isUpdatingPost: Boolean get() = postState.isUpdatingPost
    val updatePostErrorMessage: String? get() = postState.updatePostErrorMessage
    val isDeletingPost: Boolean get() = postState.isDeletingPost
    val deletePostErrorMessage: String? get() = postState.deletePostErrorMessage
    val deleteSuccessSignal: Long get() = postState.deleteSuccessSignal

    val comments get() = commentState.comments
    val commentsNextCursor get() = commentState.commentsNextCursor
    val isCommentsLoading get() = commentState.isCommentsLoading
    val commentsErrorMessage get() = commentState.commentsErrorMessage
    val commentDraft get() = commentState.commentDraft
    val replyTargetCommentId get() = commentState.replyTargetCommentId
    val replyTargetAuthorName get() = commentState.replyTargetAuthorName
    val isSubmittingComment get() = commentState.isSubmittingComment
    val submitCommentErrorMessage get() = commentState.submitCommentErrorMessage
    val editingCommentId get() = commentState.editingCommentId
    val editingCommentDraft get() = commentState.editingCommentDraft
    val isUpdatingComment get() = commentState.isUpdatingComment
    val updateCommentErrorMessage get() = commentState.updateCommentErrorMessage
    val deleteCommentTargetId get() = commentState.deleteCommentTargetId
    val isDeletingComment get() = commentState.isDeletingComment
    val deleteCommentErrorMessage get() = commentState.deleteCommentErrorMessage

    val isRefreshing: Boolean get() = isPostLoading || isCommentsLoading
    val canSubmitComment: Boolean get() = commentDraft.trim().isNotBlank() && !isSubmittingComment
}
