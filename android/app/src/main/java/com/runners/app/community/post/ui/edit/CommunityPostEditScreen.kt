package com.runners.app.community.post.ui.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.runners.app.community.post.viewmodel.CommunityPostDetailViewModel
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostEditScreen(
    postId: Long,
    onBack: () -> Unit,
    currentUserId: Long,
    modifier: Modifier = Modifier,
    viewModel: CommunityPostDetailViewModel,
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val post = uiState.post

    var title by rememberSaveable(postId) { mutableStateOf("") }
    var content by rememberSaveable(postId) { mutableStateOf("") }
    var initializedFromPost by rememberSaveable(postId) { mutableStateOf(false) }
    var submitRequested by remember { mutableStateOf(false) }
    var existingImageKeys by rememberSaveable(postId) { mutableStateOf(emptyList<String>()) }
    var existingImageUrls by rememberSaveable(postId) { mutableStateOf(emptyList<String>()) }
    var newImageUris by rememberSaveable(postId) { mutableStateOf(emptyList<String>()) }

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

    LaunchedEffect(post?.postId, post?.title, post?.content) {
        if (!initializedFromPost && post != null) {
            title = post.title
            content = post.content
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
        if (!submitRequested) return@LaunchedEffect
        if (uiState.isUpdatingPost) return@LaunchedEffect
        if (uiState.updatePostErrorMessage != null) return@LaunchedEffect
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
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(200) },
                        label = { Text("제목") },
                        placeholder = { Text("제목을 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isUpdatingPost,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("내용") },
                        placeholder = { Text("내용을 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 10,
                        enabled = !uiState.isUpdatingPost,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    "사진",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    "(${existingImageKeys.size + newImageUris.size}/10)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Box(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        if (uiState.isUpdatingPost) return@TextButton
                                        if (existingImageKeys.size + newImageUris.size >= 10) return@TextButton
                                        pickImagesLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    enabled = !uiState.isUpdatingPost,
                                ) {
                                    Text("추가")
                                }
                            }

                            if (existingImageUrls.isNotEmpty() || newImageUris.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(existingImageUrls.size) { index ->
                                        val url = existingImageUrls[index]
                                        Box(modifier = Modifier.size(86.dp)) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = "기존 이미지",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp)),
                                            )
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(26.dp),
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.58f),
                                                shadowElevation = 2.dp,
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (uiState.isUpdatingPost) return@IconButton
                                                        existingImageUrls =
                                                            existingImageUrls.filterIndexed { i, _ -> i != index }
                                                        existingImageKeys =
                                                            existingImageKeys.filterIndexed { i, _ -> i != index }
                                                    },
                                                    enabled = !uiState.isUpdatingPost,
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        contentColor = Color.White,
                                                    ),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Close,
                                                        contentDescription = "삭제",
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    items(newImageUris.size) { index ->
                                        val uriString = newImageUris[index]
                                        Box(modifier = Modifier.size(86.dp)) {
                                            AsyncImage(
                                                model = Uri.parse(uriString),
                                                contentDescription = "새 이미지",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp)),
                                            )
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(26.dp),
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.58f),
                                                shadowElevation = 2.dp,
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (uiState.isUpdatingPost) return@IconButton
                                                        newImageUris =
                                                            newImageUris.filterIndexed { i, _ -> i != index }
                                                    },
                                                    enabled = !uiState.isUpdatingPost,
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        contentColor = Color.White,
                                                    ),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Close,
                                                        contentDescription = "삭제",
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.updatePostErrorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = uiState.updatePostErrorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
