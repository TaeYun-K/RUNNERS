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

