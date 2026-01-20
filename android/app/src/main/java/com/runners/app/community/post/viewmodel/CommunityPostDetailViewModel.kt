package com.runners.app.community.post.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.runners.app.community.comment.data.CommunityCommentRepository
import com.runners.app.community.post.data.CommunityPostRepository
import com.runners.app.community.post.state.CommunityPostDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityPostDetailViewModel(
    private val postId: Long,
    private val postRepository: CommunityPostRepository = CommunityPostRepository(),
    private val commentRepository: CommunityCommentRepository = CommunityCommentRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityPostDetailUiState())
    val uiState: StateFlow<CommunityPostDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadPost()
            loadComments(reset = true)
        }
    }

    fun loadMoreComments() {
        viewModelScope.launch {
            loadComments(reset = false)
        }
    }

    fun togglePostRecommend() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isTogglingPostRecommend || state.isPostLoading) return@launch

            _uiState.update {
                it.copy(
                    postState = it.postState.copy(
                        isTogglingPostRecommend = true,
                        togglePostRecommendErrorMessage = null,
                    ),
                )
            }

            val currentlyRecommended = state.isPostRecommended
            runCatching {
                if (currentlyRecommended) {
                    postRepository.unrecommendPost(postId)
                } else {
                    postRepository.recommendPost(postId)
                }
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            post = it.postState.post?.copy(recommendCount = result.recommendCount),
                            isPostRecommended = result.recommended,
                            isTogglingPostRecommend = false,
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            isTogglingPostRecommend = false,
                            togglePostRecommendErrorMessage = error.message ?: "추천을 처리하지 못했어요",
                        ),
                    )
                }
            }
        }
    }

    fun toggleCommentRecommend(commentId: Long) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isSubmittingComment || state.isUpdatingComment || state.isDeletingComment) return@launch
            if (commentId <= 0) return@launch
            if (state.commentState.recommendingCommentIds.contains(commentId)) return@launch

            val currentlyRecommended = state.commentState.recommendedCommentIds.contains(commentId)

            _uiState.update {
                it.copy(
                    commentState = it.commentState.copy(
                        recommendingCommentIds = it.commentState.recommendingCommentIds + commentId,
                        recommendCommentErrorMessage = null,
                    ),
                )
            }

            runCatching {
                if (currentlyRecommended) {
                    commentRepository.unrecommendComment(postId = postId, commentId = commentId)
                } else {
                    commentRepository.recommendComment(postId = postId, commentId = commentId)
                }
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            comments =
                                it.commentState.comments.map { comment ->
                                    if (comment.commentId == commentId) {
                                        comment.copy(recommendCount = result.recommendCount)
                                    } else {
                                        comment
                                    }
                                },
                            recommendedCommentIds =
                                if (result.recommended) it.commentState.recommendedCommentIds + commentId else it.commentState.recommendedCommentIds - commentId,
                            checkedRecommendCommentIds = it.commentState.checkedRecommendCommentIds + commentId,
                            recommendingCommentIds = it.commentState.recommendingCommentIds - commentId,
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            checkedRecommendCommentIds = it.commentState.checkedRecommendCommentIds + commentId,
                            recommendingCommentIds = it.commentState.recommendingCommentIds - commentId,
                            recommendCommentErrorMessage = error.message ?: "댓글 추천을 처리하지 못했어요",
                        ),
                    )
                }
            }
        }
    }

    fun onCommentDraftChange(value: String) {
        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    commentDraft = value,
                    submitCommentErrorMessage = null,
                ),
            )
        }
    }

    fun startReply(commentId: Long, authorName: String?) {
        val state = _uiState.value
        if (state.isSubmittingComment || state.isDeletingComment || state.isUpdatingComment) return

        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    replyTargetCommentId = commentId,
                    replyTargetAuthorName = authorName,
                    submitCommentErrorMessage = null,
                    editingCommentId = null,
                    editingCommentDraft = "",
                    updateCommentErrorMessage = null,
                ),
            )
        }
    }

    fun cancelReply() {
        val state = _uiState.value
        if (state.isSubmittingComment) return
        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    replyTargetCommentId = null,
                    replyTargetAuthorName = null,
                    submitCommentErrorMessage = null,
                ),
            )
        }
    }

    fun submitComment() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isSubmittingComment) return@launch

            val content = state.commentDraft.trim()
            if (content.isBlank()) return@launch

            _uiState.update {
                it.copy(
                    commentState = it.commentState.copy(
                        isSubmittingComment = true,
                        submitCommentErrorMessage = null,
                    ),
                )
            }

            runCatching {
                commentRepository.createComment(
                    postId = postId,
                    content = content,
                    parentId = state.replyTargetCommentId,
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            post = it.postState.post?.copy(commentCount = result.commentCount),
                        ),
                        commentState = it.commentState.copy(
                            commentDraft = "",
                            replyTargetCommentId = null,
                            replyTargetAuthorName = null,
                        ),
                    )
                }
                loadComments(reset = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            submitCommentErrorMessage = error.message ?: "댓글을 작성하지 못했어요",
                        ),
                    )
                }
            }

            _uiState.update {
                it.copy(
                    commentState = it.commentState.copy(isSubmittingComment = false),
                )
            }
        }
    }

    fun startEditingComment(commentId: Long, initialContent: String) {
        val state = _uiState.value
        if (state.isUpdatingComment) return
        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    editingCommentId = commentId,
                    editingCommentDraft = initialContent,
                    updateCommentErrorMessage = null,
                    replyTargetCommentId = null,
                    replyTargetAuthorName = null,
                ),
            )
        }
    }

    fun cancelEditingComment() {
        val state = _uiState.value
        if (state.isUpdatingComment || state.isDeletingComment) return
        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    editingCommentId = null,
                    editingCommentDraft = "",
                    updateCommentErrorMessage = null,
                ),
            )
        }
    }

    fun onEditingCommentDraftChange(value: String) {
        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    editingCommentDraft = value,
                    updateCommentErrorMessage = null,
                ),
            )
        }
    }

    fun submitEditingComment() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isUpdatingComment || state.isDeletingComment) return@launch

            val commentId = state.editingCommentId ?: return@launch
            val content = state.editingCommentDraft.trim()
            if (content.isBlank()) return@launch

            _uiState.update {
                it.copy(
                    commentState = it.commentState.copy(
                        isUpdatingComment = true,
                        updateCommentErrorMessage = null,
                    ),
                )
            }

            runCatching {
                commentRepository.updateComment(postId = postId, commentId = commentId, content = content)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            post = it.postState.post?.copy(commentCount = result.commentCount),
                        ),
                        commentState = it.commentState.copy(
                            editingCommentId = null,
                            editingCommentDraft = "",
                            isUpdatingComment = false,
                        ),
                    )
                }
                loadComments(reset = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            updateCommentErrorMessage = error.message ?: "댓글을 수정하지 못했어요",
                            isUpdatingComment = false,
                        ),
                    )
                }
            }
        }
    }

    fun requestDeleteComment(commentId: Long) {
        val state = _uiState.value
        if (state.isDeletingComment || state.isUpdatingComment) return
        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    deleteCommentTargetId = commentId,
                    deleteCommentErrorMessage = null,
                ),
            )
        }
    }

    fun cancelDeleteComment() {
        val state = _uiState.value
        if (state.isDeletingComment) return
        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    deleteCommentTargetId = null,
                    deleteCommentErrorMessage = null,
                ),
            )
        }
    }

    fun confirmDeleteComment() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isDeletingComment || state.isUpdatingComment) return@launch

            val targetId = state.deleteCommentTargetId ?: return@launch

            _uiState.update {
                it.copy(
                    commentState = it.commentState.copy(
                        isDeletingComment = true,
                        deleteCommentErrorMessage = null,
                    ),
                )
            }

            runCatching {
                commentRepository.deleteComment(postId = postId, commentId = targetId)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            post = it.postState.post?.copy(commentCount = result.commentCount),
                        ),
                        commentState = it.commentState.copy(
                            deleteCommentTargetId = null,
                            isDeletingComment = false,
                        ),
                    )
                }
                if (_uiState.value.editingCommentId == targetId) {
                    cancelEditingComment()
                }
                loadComments(reset = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            isDeletingComment = false,
                            deleteCommentErrorMessage = error.message ?: "댓글을 삭제하지 못했어요",
                        ),
                    )
                }
            }
        }
    }

    private suspend fun loadPost() {
        val state = _uiState.value
        if (state.isPostLoading) return

        _uiState.update {
            it.copy(
                postState = it.postState.copy(
                    isPostLoading = true,
                    postErrorMessage = null,
                ),
            )
        }

        runCatching {
            postRepository.getPost(postId)
        }.onSuccess { post ->
            _uiState.update {
                it.copy(
                    postState = it.postState.copy(
                        post = post,
                        isPostLoading = false,
                    ),
                )
            }

            runCatching {
                postRepository.getPostRecommendStatus(postId)
            }.onSuccess { status ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            isPostRecommended = status.recommended,
                            post = it.postState.post?.copy(recommendCount = status.recommendCount),
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            togglePostRecommendErrorMessage = error.message ?: "추천 상태를 불러오지 못했어요",
                        ),
                    )
                }
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    postState = it.postState.copy(
                        postErrorMessage = error.message ?: "게시글을 불러오지 못했어요",
                        isPostLoading = false,
                    ),
                )
            }
        }
    }

    private suspend fun loadComments(reset: Boolean) {
        val state = _uiState.value
        if (state.isCommentsLoading) return
        if (!reset && state.commentsNextCursor == null) return

        _uiState.update {
            it.copy(
                commentState = it.commentState.copy(
                    isCommentsLoading = true,
                    commentsErrorMessage = null,
                    recommendCommentErrorMessage = null,
                    recommendedCommentIds = if (reset) emptySet() else it.commentState.recommendedCommentIds,
                    checkedRecommendCommentIds = if (reset) emptySet() else it.commentState.checkedRecommendCommentIds,
                ),
            )
        }

        val cursor = if (reset) null else state.commentsNextCursor

        runCatching {
            commentRepository.listComments(postId = postId, cursor = cursor, size = 20)
        }.onSuccess { result ->
            _uiState.update {
                it.copy(
                    commentState = it.commentState.copy(
                        comments = if (reset) result.comments else it.commentState.comments + result.comments,
                        commentsNextCursor = result.nextCursor,
                        isCommentsLoading = false,
                    ),
                )
            }

            val newComments = result.comments
            viewModelScope.launch {
                fetchRecommendStatusesForComments(newComments)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    commentState = it.commentState.copy(
                        commentsErrorMessage = error.message ?: "댓글을 불러오지 못했어요",
                        isCommentsLoading = false,
                    ),
                )
            }
        }
    }

    private suspend fun fetchRecommendStatusesForComments(comments: List<com.runners.app.network.CommunityCommentResult>) {
        if (comments.isEmpty()) return

        for (comment in comments) {
            val commentId = comment.commentId
            val state = _uiState.value

            if (state.commentState.checkedRecommendCommentIds.contains(commentId)) continue
            if (comment.content == "삭제된 댓글입니다") {
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            checkedRecommendCommentIds = it.commentState.checkedRecommendCommentIds + commentId,
                        ),
                    )
                }
                continue
            }

            runCatching {
                commentRepository.getCommentRecommendStatus(postId = postId, commentId = commentId)
            }.onSuccess { status ->
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            checkedRecommendCommentIds = it.commentState.checkedRecommendCommentIds + commentId,
                            recommendedCommentIds =
                                if (status.recommended) it.commentState.recommendedCommentIds + commentId else it.commentState.recommendedCommentIds - commentId,
                            comments =
                                it.commentState.comments.map { existing ->
                                    if (existing.commentId == commentId) {
                                        existing.copy(recommendCount = status.recommendCount)
                                    } else {
                                        existing
                                    }
                                },
                        ),
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        commentState = it.commentState.copy(
                            checkedRecommendCommentIds = it.commentState.checkedRecommendCommentIds + commentId,
                        ),
                    )
                }
            }
        }
    }

    fun updatePost(title: String, content: String) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isUpdatingPost) return@launch

            val newTitle = title.trim()
            val newContent = content.trim()
            if (newTitle.isBlank() || newContent.isBlank()) return@launch

            _uiState.update {
                it.copy(
                    postState = it.postState.copy(
                        isUpdatingPost = true,
                        updatePostErrorMessage = null,
                    ),
                )
            }

            runCatching {
                postRepository.updatePost(postId = postId, title = newTitle, content = newContent)
            }.onSuccess { updated ->
                // 1) 즉시 화면 데이터 갱신
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            post = it.postState.post?.copy(
                                title = updated.title,
                                content = updated.content,
                                updatedAt = updated.updatedAt ?: it.postState.post?.updatedAt,
                            ),
                            isUpdatingPost = false,
                        ),
                    )
                }

                // 2) 서버 데이터로 완전 동기화 원하면 아래 한 줄 추가 (선택)
                // loadPost()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            updatePostErrorMessage = error.message ?: "게시글을 수정하지 못했어요",
                            isUpdatingPost = false,
                        ),
                    )
                }
            }
        }
    }

    fun deletePost() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isDeletingPost) return@launch

            _uiState.update {
                it.copy(
                    postState = it.postState.copy(
                        isDeletingPost = true,
                        deletePostErrorMessage = null,
                    ),
                )
            }

            runCatching {
                postRepository.deletePost(postId = postId)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            isDeletingPost = false,
                            deleteSuccessSignal = it.postState.deleteSuccessSignal + 1L,
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        postState = it.postState.copy(
                            isDeletingPost = false,
                            deletePostErrorMessage = error.message ?: "게시글을 삭제하지 못했어요",
                        ),
                    )
                }
            }
        }
    }

    class Factory(
        private val postId: Long,
        private val postRepository: CommunityPostRepository = CommunityPostRepository(),
        private val commentRepository: CommunityCommentRepository = CommunityCommentRepository(),
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommunityPostDetailViewModel::class.java)) {
                return CommunityPostDetailViewModel(
                    postId = postId,
                    postRepository = postRepository,
                    commentRepository = commentRepository,
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
