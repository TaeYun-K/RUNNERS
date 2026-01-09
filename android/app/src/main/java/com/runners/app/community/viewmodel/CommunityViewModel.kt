package com.runners.app.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runners.app.community.data.CommunityRepository
import com.runners.app.community.state.CommunityUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val repository: CommunityRepository = CommunityRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_uiState.value.isInitialLoading) return@launch

            _uiState.update {
                it.copy(
                    isInitialLoading = true,
                    listErrorMessage = null,
                )
            }

            runCatching {
                repository.listPosts(cursor = null, size = 20)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        posts = result.posts,
                        nextCursor = result.nextCursor,
                        isInitialLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        listErrorMessage = error.message ?: "게시글을 불러오지 못했어요",
                    )
                }
            }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val state = _uiState.value
            val cursor = state.nextCursor ?: return@launch
            if (state.isLoadingMore || state.isInitialLoading) return@launch

            _uiState.update { it.copy(isLoadingMore = true, listErrorMessage = null) }

            runCatching {
                repository.listPosts(cursor = cursor, size = 20)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        posts = it.posts + result.posts,
                        nextCursor = result.nextCursor,
                        isLoadingMore = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        listErrorMessage = error.message ?: "게시글을 불러오지 못했어요",
                    )
                }
            }
        }
    }

    fun resetCreateDraft() {
        if (_uiState.value.isCreating) return
        _uiState.update { it.copy(createTitle = "", createContent = "", createErrorMessage = null) }
    }

    fun onCreateTitleChange(value: String) {
        _uiState.update { it.copy(createTitle = value, createErrorMessage = null) }
    }

    fun onCreateContentChange(value: String) {
        _uiState.update { it.copy(createContent = value, createErrorMessage = null) }
    }

    fun submitCreatePost() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isCreating) return@launch

            val trimmedTitle = state.createTitle.trim()
            val trimmedContent = state.createContent.trim()
            if (trimmedTitle.isBlank() || trimmedContent.isBlank()) return@launch

            _uiState.update { it.copy(isCreating = true, createErrorMessage = null) }

            runCatching {
                repository.createPost(title = trimmedTitle, content = trimmedContent)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createTitle = "",
                        createContent = "",
                        createErrorMessage = null,
                        createSuccessSignal = it.createSuccessSignal + 1L,
                        scrollToTopSignal = it.scrollToTopSignal + 1L,
                    )
                }
                refreshAfterCreate()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createErrorMessage = error.message ?: "게시글을 작성하지 못했어요",
                    )
                }
            }
        }
    }

    private fun refreshAfterCreate() {
        refresh()
    }
}
