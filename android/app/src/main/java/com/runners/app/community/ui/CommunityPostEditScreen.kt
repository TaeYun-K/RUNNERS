package com.runners.app.community.ui

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runners.app.community.viewmodel.CommunityPostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostEditScreen(
    postId: Long,
    onBack: () -> Unit,
    currentUserId: Long,
    modifier: Modifier = Modifier,
    viewModel: CommunityPostDetailViewModel,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val post = uiState.post

    var title by rememberSaveable(postId) { mutableStateOf("") }
    var content by rememberSaveable(postId) { mutableStateOf("") }
    var initializedFromPost by rememberSaveable(postId) { mutableStateOf(false) }
    var submitRequested by remember { mutableStateOf(false) }

    LaunchedEffect(post?.postId, post?.title, post?.content) {
        if (!initializedFromPost && post != null) {
            title = post.title
            content = post.content
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
                        viewModel.updatePost(title = title, content = content)
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
