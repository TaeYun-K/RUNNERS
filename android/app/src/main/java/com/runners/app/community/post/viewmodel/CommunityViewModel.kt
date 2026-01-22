package com.runners.app.community.post.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runners.app.community.post.data.CommunityPostRepository
import com.runners.app.community.post.state.CommunityPostStatsUpdate
import com.runners.app.community.post.state.CommunityUiState
import com.runners.app.network.PresignCommunityImageUploadFileRequest
import com.runners.app.network.PresignedUploadClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommunityViewModel(
    private val repository: CommunityPostRepository = CommunityPostRepository(),
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
                val searchQuery = _uiState.value.searchQuery
                if (searchQuery.isBlank()) {
                    repository.listPosts(cursor = null, size = 20)
                } else {
                    repository.searchPosts(query = searchQuery, cursor = null, size = 20)
                }
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

    fun onSearchInputChange(value: String) {
        _uiState.update {
            it.copy(
                searchInput = value,
                listErrorMessage = null,
            )
        }
    }

    fun toggleSearchOpen() {
        _uiState.update { state ->
            state.copy(
                isSearchOpen = !state.isSearchOpen,
                listErrorMessage = null,
            )
        }
    }

    fun closeSearch() {
        _uiState.update { state ->
            state.copy(
                isSearchOpen = false,
                listErrorMessage = null,
            )
        }
    }

    fun submitSearch() {
        _uiState.update { state ->
            state.copy(
                searchQuery = state.searchInput.trim(),
                isSearchOpen = true,
                listErrorMessage = null,
                scrollToTopSignal = state.scrollToTopSignal + 1L,
            )
        }
        refresh()
    }

    fun clearSearchAndRefresh() {
        _uiState.update { state ->
            state.copy(
                searchInput = "",
                searchQuery = "",
                isSearchOpen = false,
                listErrorMessage = null,
                scrollToTopSignal = state.scrollToTopSignal + 1L,
            )
        }
        refresh()
    }

    fun loadMore() {
        viewModelScope.launch {
            val state = _uiState.value
            val cursor = state.nextCursor ?: return@launch
            if (state.isLoadingMore || state.isInitialLoading) return@launch

            _uiState.update { it.copy(isLoadingMore = true, listErrorMessage = null) }

            runCatching {
                val searchQuery = state.searchQuery
                if (searchQuery.isBlank()) {
                    repository.listPosts(cursor = cursor, size = 20)
                } else {
                    repository.searchPosts(query = searchQuery, cursor = cursor, size = 20)
                }
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
        _uiState.update { it.copy(createTitle = "", createContent = "", createImageUris = emptyList(), createErrorMessage = null) }
    }

    fun onCreateTitleChange(value: String) {
        _uiState.update { it.copy(createTitle = value, createErrorMessage = null) }
    }

    fun onCreateContentChange(value: String) {
        _uiState.update { it.copy(createContent = value, createErrorMessage = null) }
    }

    fun addCreateImages(uris: List<Uri>, maxImages: Int = 10) {
        if (uris.isEmpty()) return
        _uiState.update { state ->
            val existing = state.createImageUris.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }
            val merged = (existing + uris).distinct().take(maxImages)
            state.copy(
                createImageUris = merged.map { it.toString() },
                createErrorMessage = null,
            )
        }
    }

    fun removeCreateImage(uri: String) {
        _uiState.update { state ->
            state.copy(
                createImageUris = state.createImageUris.filterNot { it == uri },
                createErrorMessage = null,
            )
        }
    }

    fun submitCreatePost(context: Context) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isCreating) return@launch

            val trimmedTitle = state.createTitle.trim()
            val trimmedContent = state.createContent.trim()
            if (trimmedTitle.isBlank() || trimmedContent.isBlank()) return@launch

            _uiState.update { it.copy(isCreating = true, createErrorMessage = null) }

            runCatching {
                val imageKeys = uploadSelectedImagesIfNeeded(context, state.createImageUris)
                repository.createPost(title = trimmedTitle, content = trimmedContent, imageKeys = imageKeys)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createTitle = "",
                        createContent = "",
                        createImageUris = emptyList(),
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
                        createErrorMessage = error.message ?: error.javaClass.simpleName ?: "게시글을 작성하지 못했어요",
                    )
                }
            }
        }
    }

    private suspend fun uploadSelectedImagesIfNeeded(context: Context, createImageUris: List<String>): List<String>? {
        return withContext(Dispatchers.IO) {
            if (createImageUris.isEmpty()) return@withContext null

            val uris = createImageUris.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }
            if (uris.isEmpty()) return@withContext null

            val payloads = ArrayList<ByteArray>(uris.size)
            val files = ArrayList<PresignCommunityImageUploadFileRequest>(uris.size)

            for (uri in uris) {
                val bytes =
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("사진을 읽을 수 없어요")
                val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                payloads.add(bytes)
                files.add(
                    PresignCommunityImageUploadFileRequest(
                        fileName = null,
                        contentType = contentType,
                        contentLength = bytes.size.toLong(),
                    )
                )
            }

            val presigned = repository.presignCommunityPostImageUploads(files)
            if (presigned.items.size != payloads.size) {
                throw IllegalStateException("업로드 정보를 받지 못했어요")
            }

            presigned.items.forEachIndexed { index, item ->
                PresignedUploadClient.put(
                    uploadUrl = item.uploadUrl,
                    contentType = item.contentType.ifBlank { "application/octet-stream" },
                    bytes = payloads[index],
                )
            }

            presigned.items.map { it.key }
        }
    }

    private fun refreshAfterCreate() {
        refresh()
    }

    fun applyPostStatsUpdate(update: CommunityPostStatsUpdate) {
        _uiState.update { state ->
            val updatedPosts =
                state.posts.map { post ->
                    if (post.postId != update.postId) return@map post
                    post.copy(
                        viewCount = update.viewCount,
                        recommendCount = update.recommendCount,
                        commentCount = update.commentCount,
                    )
                }

            state.copy(posts = updatedPosts)
        }
    }

    fun deletePost(postId: Long) {
        _uiState.update { state ->
            state.copy(posts = state.posts.filterNot { it.postId == postId })
        }
    }
}
