package com.runners.app.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.runners.app.community.data.CommunityRepository
import com.runners.app.community.state.CommunityPostDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityPostDetailViewModel(
    private val postId: Long,
    private val repository: CommunityRepository = CommunityRepository(),
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

    fun onCommentDraftChange(value: String) {
        _uiState.update { it.copy(commentDraft = value, submitCommentErrorMessage = null) }
    }

    fun submitComment() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isSubmittingComment) return@launch

            val content = state.commentDraft.trim()
            if (content.isBlank()) return@launch

            _uiState.update { it.copy(isSubmittingComment = true, submitCommentErrorMessage = null) }

            runCatching {
                repository.createComment(postId = postId, content = content, parentId = null)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        commentDraft = "",
                        post = it.post?.copy(commentCount = result.commentCount),
                    )
                }
                loadComments(reset = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        submitCommentErrorMessage = error.message ?: "댓글을 작성하지 못했어요",
                    )
                }
            }

            _uiState.update { it.copy(isSubmittingComment = false) }
        }
    }

    fun startEditingComment(commentId: Long, initialContent: String) {
        val state = _uiState.value
        if (state.isUpdatingComment) return
        _uiState.update {
            it.copy(
                editingCommentId = commentId,
                editingCommentDraft = initialContent,
                updateCommentErrorMessage = null,
            )
        }
    }

    fun cancelEditingComment() {
        val state = _uiState.value
        if (state.isUpdatingComment || state.isDeletingComment) return
        _uiState.update {
            it.copy(
                editingCommentId = null,
                editingCommentDraft = "",
                updateCommentErrorMessage = null,
            )
        }
    }

    fun onEditingCommentDraftChange(value: String) {
        _uiState.update { it.copy(editingCommentDraft = value, updateCommentErrorMessage = null) }
    }

    fun submitEditingComment() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isUpdatingComment || state.isDeletingComment) return@launch

            val commentId = state.editingCommentId ?: return@launch
            val content = state.editingCommentDraft.trim()
            if (content.isBlank()) return@launch

            _uiState.update { it.copy(isUpdatingComment = true, updateCommentErrorMessage = null) }

            runCatching {
                repository.updateComment(postId = postId, commentId = commentId, content = content)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        post = it.post?.copy(commentCount = result.commentCount),
                        editingCommentId = null,
                        editingCommentDraft = "",
                        isUpdatingComment = false,
                    )
                }
                loadComments(reset = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        updateCommentErrorMessage = error.message ?: "댓글을 수정하지 못했어요",
                        isUpdatingComment = false,
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
                deleteCommentTargetId = commentId,
                deleteCommentErrorMessage = null,
            )
        }
    }

    fun cancelDeleteComment() {
        val state = _uiState.value
        if (state.isDeletingComment) return
        _uiState.update { it.copy(deleteCommentTargetId = null, deleteCommentErrorMessage = null) }
    }

    fun confirmDeleteComment() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isDeletingComment || state.isUpdatingComment) return@launch

            val targetId = state.deleteCommentTargetId ?: return@launch

            _uiState.update { it.copy(isDeletingComment = true, deleteCommentErrorMessage = null) }

            runCatching {
                repository.deleteComment(postId = postId, commentId = targetId)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        post = it.post?.copy(commentCount = result.commentCount),
                        deleteCommentTargetId = null,
                        isDeletingComment = false,
                    )
                }
                if (_uiState.value.editingCommentId == targetId) {
                    cancelEditingComment()
                }
                loadComments(reset = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isDeletingComment = false,
                        deleteCommentErrorMessage = error.message ?: "댓글을 삭제하지 못했어요",
                    )
                }
            }
        }
    }

    private suspend fun loadPost() {
        val state = _uiState.value
        if (state.isPostLoading) return

        _uiState.update { it.copy(isPostLoading = true, postErrorMessage = null) }

        runCatching {
            repository.getPost(postId)
        }.onSuccess { post ->
            _uiState.update { it.copy(post = post, isPostLoading = false) }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    postErrorMessage = error.message ?: "게시글을 불러오지 못했어요",
                    isPostLoading = false,
                )
            }
        }
    }

    private suspend fun loadComments(reset: Boolean) {
        val state = _uiState.value
        if (state.isCommentsLoading) return
        if (!reset && state.commentsNextCursor == null) return

        _uiState.update { it.copy(isCommentsLoading = true, commentsErrorMessage = null) }

        val cursor = if (reset) null else state.commentsNextCursor

        runCatching {
            repository.listComments(postId = postId, cursor = cursor, size = 20)
        }.onSuccess { result ->
            _uiState.update {
                it.copy(
                    comments = if (reset) result.comments else it.comments + result.comments,
                    commentsNextCursor = result.nextCursor,
                    isCommentsLoading = false,
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    commentsErrorMessage = error.message ?: "댓글을 불러오지 못했어요",
                    isCommentsLoading = false,
                )
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

            _uiState.update { it.copy(isUpdatingPost = true, updatePostErrorMessage = null) }

            runCatching {
                repository.updatePost(postId = postId, title = newTitle, content = newContent)
            }.onSuccess { updated ->
                // 1) 즉시 화면 데이터 갱신
                _uiState.update {
                    it.copy(
                        post = it.post?.copy(
                            title = updated.title,
                            content = updated.content,
                            // 서버가 updatedAt을 내려주면 반영 (CreateCommunityPostResult에 updatedAt 추가한 경우)
                            updatedAt = updated.updatedAt ?: it.post?.updatedAt
                        ),
                        isUpdatingPost = false,
                    )
                }

                // 2) 서버 데이터로 완전 동기화 원하면 아래 한 줄 추가 (선택)
                // loadPost()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        updatePostErrorMessage = error.message ?: "게시글을 수정하지 못했어요",
                        isUpdatingPost = false,
                    )
                }
            }
        }
    }

    fun deletePost() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isDeletingPost) return@launch

            _uiState.update { it.copy(isDeletingPost = true, deletePostErrorMessage = null) }

            runCatching {
                repository.deletePost(postId = postId)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isDeletingPost = false,
                        deleteSuccessSignal = it.deleteSuccessSignal + 1L,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isDeletingPost = false,
                        deletePostErrorMessage = error.message ?: "게시글을 삭제하지 못했어요",
                    )
                }
            }
        }
    }

    class Factory(
        private val postId: Long,
        private val repository: CommunityRepository = CommunityRepository(),
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommunityPostDetailViewModel::class.java)) {
                return CommunityPostDetailViewModel(
                    postId = postId,
                    repository = repository,
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
