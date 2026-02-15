package com.runners.app.community.post.ui.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runners.app.community.post.ui.editor.CommunityPostEditorForm
import com.runners.app.community.post.ui.editor.CommunityPostEditorImageItem
import com.runners.app.community.post.viewmodel.CommunityPostDetailViewModel
import com.runners.app.network.CommunityPostBoardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostEditScreen(
    postId: Long,
    onBack: () -> Unit,
    onEdited: () -> Unit,
    currentUserId: Long,
    modifier: Modifier = Modifier,
    viewModel: CommunityPostDetailViewModel,
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val post = uiState.post

    var title by rememberSaveable(postId) { mutableStateOf("") }
    var content by rememberSaveable(postId) { mutableStateOf("") }
    var boardTypeRaw by rememberSaveable(postId) { mutableStateOf(CommunityPostBoardType.FREE.name) }
    var initializedFromPost by rememberSaveable(postId) { mutableStateOf(false) }
    var submitRequested by remember { mutableStateOf(false) }
    var submitHandled by rememberSaveable(postId) { mutableStateOf(false) }
    var existingImageKeys by rememberSaveable(postId) { mutableStateOf(emptyList<String>()) }
    var existingImageUrls by rememberSaveable(postId) { mutableStateOf(emptyList<String>()) }
    var newImageUris by rememberSaveable(postId) { mutableStateOf(emptyList<String>()) }

    val selectedBoardType = remember(boardTypeRaw) { CommunityPostBoardType.from(boardTypeRaw) }

    val pickImagesLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        ) { uris ->
            if (uris.isEmpty()) return@rememberLauncherForActivityResult
            val currentNew = newImageUris.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }
            val merged = (currentNew + uris).distinct()
            val max = 10 - existingImageKeys.size
            newImageUris = merged.take(max.coerceAtLeast(0)).map { it.toString() }
        }

    LaunchedEffect(post?.postId, post?.title, post?.content, post?.boardType) {
        if (!initializedFromPost && post != null) {
            title = post.title
            content = post.content
            boardTypeRaw = post.boardType.name
            existingImageKeys = post.imageKeys
            existingImageUrls = post.imageUrls
            initializedFromPost = true
        }
    }

    LaunchedEffect(post?.authorId) {
        if (post != null && post.authorId != currentUserId) {
            onBack()
        }
    }

    LaunchedEffect(uiState.isUpdatingPost, uiState.updatePostErrorMessage) {
        if (!submitRequested || submitHandled) return@LaunchedEffect
        if (uiState.isUpdatingPost) return@LaunchedEffect
        if (uiState.updatePostErrorMessage != null) return@LaunchedEffect
        submitHandled = true
        onEdited()
        onBack()
    }

    fun canSubmit(): Boolean {
        if (uiState.isUpdatingPost) return false
        return title.trim().isNotBlank() && content.trim().isNotBlank()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "게시글 수정",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !uiState.isUpdatingPost) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onBack,
                        enabled = !uiState.isUpdatingPost,
                    ) {
                        Text(
                            "취소",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Button(
                    onClick = {
                        submitRequested = true
                        viewModel.updatePost(
                            context = context,
                            title = title,
                            content = content,
                            boardType = selectedBoardType,
                            existingImageKeys = existingImageKeys,
                            newImageUris = newImageUris.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() },
                        )
                    },
                    enabled = canSubmit(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    if (uiState.isUpdatingPost) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("저장")
                    }
                }
            }
        },
    ) { padding ->
        when {
            uiState.isPostLoading && post == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.postErrorMessage != null && post == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = uiState.postErrorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = viewModel::refresh) {
                        Text("다시 시도")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CommunityPostEditorForm(
                        title = title,
                        content = content,
                        selectedBoardType = selectedBoardType,
                        images = buildList {
                            existingImageUrls.forEachIndexed { index, url ->
                                add(
                                    CommunityPostEditorImageItem(
                                        id = "existing:$index",
                                        model = url,
                                        contentDescription = "기존 이미지",
                                    )
                                )
                            }
                            newImageUris.forEach { uriString ->
                                add(
                                    CommunityPostEditorImageItem(
                                        id = "new:$uriString",
                                        model = Uri.parse(uriString),
                                        contentDescription = "새 이미지",
                                    )
                                )
                            }
                        },
                        isSubmitting = uiState.isUpdatingPost,
                        errorMessage = uiState.updatePostErrorMessage,
                        onBoardTypeChange = { boardTypeRaw = it.name },
                        onTitleChange = { title = it.take(200) },
                        onContentChange = { content = it },
                        onAddImages = {
                            if (uiState.isUpdatingPost) return@CommunityPostEditorForm
                            if (existingImageKeys.size + newImageUris.size >= 10) {
                                return@CommunityPostEditorForm
                            }
                            pickImagesLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        onRemoveImage = { image ->
                            if (uiState.isUpdatingPost) return@CommunityPostEditorForm
                            when {
                                image.id.startsWith("existing:") -> {
                                    val index = image.id.removePrefix("existing:")
                                        .toIntOrNull() ?: return@CommunityPostEditorForm
                                    existingImageUrls = existingImageUrls.filterIndexed { i, _ ->
                                        i != index
                                    }
                                    existingImageKeys = existingImageKeys.filterIndexed { i, _ ->
                                        i != index
                                    }
                                }
                                image.id.startsWith("new:") -> {
                                    val uriString = image.id.removePrefix("new:")
                                    newImageUris = newImageUris.filterNot { it == uriString }
                                }
                            }
                        },
                        addImageButtonLabel = "추가",
                    )
                }
            }
        }
    }
}
