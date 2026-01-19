package com.runners.app.community.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runners.app.network.BackendCommunityApi
import com.runners.app.network.CommunityCommentResult
import com.runners.app.network.CommunityPostDetailResult
import com.runners.app.settings.AppSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDetailScreen(
    postId: Long,
    onBack: (CommunityPostDetailResult?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showTotalDistanceInCommunity =
        AppSettingsStore.showTotalDistanceInCommunityFlow(context)
            .collectAsStateWithLifecycle(initialValue = true)
            .value

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var post by remember { mutableStateOf<CommunityPostDetailResult?>(null) }

    var comments by remember { mutableStateOf<List<CommunityCommentResult>>(emptyList()) }
    var commentsNextCursor by remember { mutableStateOf<String?>(null) }
    var isCommentsLoading by remember { mutableStateOf(false) }
    var commentsErrorMessage by remember { mutableStateOf<String?>(null) }

    var commentDraft by remember { mutableStateOf("") }
    var isSubmittingComment by remember { mutableStateOf(false) }
    var submitCommentErrorMessage by remember { mutableStateOf<String?>(null) }

    fun toSecondPrecision(raw: String): String {
        val normalized = raw.replace('T', ' ')
        return normalized.takeIf { it.length >= 19 }?.substring(0, 19) ?: normalized
    }

    suspend fun loadPost() {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        try {
            post = withContext(Dispatchers.IO) { BackendCommunityApi.getPost(postId) }
        } catch (e: Exception) {
            errorMessage = e.message ?: "게시글을 불러오지 못했어요"
        } finally {
            isLoading = false
        }
    }

    suspend fun loadComments(reset: Boolean) {
        if (isCommentsLoading) return
        if (!reset && commentsNextCursor == null) return

        isCommentsLoading = true
        commentsErrorMessage = null

        val cursor = if (reset) null else commentsNextCursor
        try {
            val result =
                withContext(Dispatchers.IO) { BackendCommunityApi.listComments(postId = postId, cursor = cursor, size = 20) }
            commentsNextCursor = result.nextCursor
            comments = if (reset) result.comments else comments + result.comments
        } catch (e: Exception) {
            commentsErrorMessage = e.message ?: "댓글을 불러오지 못했어요"
        } finally {
            isCommentsLoading = false
        }
    }

    suspend fun submitComment() {
        if (isSubmittingComment) return

        val content = commentDraft.trim()
        if (content.isBlank()) return

        isSubmittingComment = true
        submitCommentErrorMessage = null
        try {
            val result =
                withContext(Dispatchers.IO) {
                    BackendCommunityApi.createComment(postId = postId, content = content, parentId = null)
                }
            commentDraft = ""
            post = post?.copy(commentCount = result.commentCount)
            loadComments(reset = true)
        } catch (e: Exception) {
            submitCommentErrorMessage = e.message ?: "댓글을 작성하지 못했어요"
        } finally {
            isSubmittingComment = false
        }
    }

    LaunchedEffect(postId) {
        loadPost()
        loadComments(reset = true)
    }

    BackHandler(enabled = true) {
        onBack(post)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("게시글") },
                navigationIcon = {
                    IconButton(onClick = { onBack(post) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        when {
            isLoading && post == null -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null && post == null -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { scope.launch { loadPost(); loadComments(reset = true) } }) {
                        Text("다시 시도")
                    }
                }
            }

            else -> {
                val data = post
                if (data != null) {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(data.title, style = MaterialTheme.typography.headlineSmall)

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                CommunityAuthorLine(
                                    nickname = data.authorName ?: "익명",
                                    totalDistanceKm = data.authorTotalDistanceKm,
                                    showTotalDistance = showTotalDistanceInCommunity,
                                )
                                HorizontalDivider()
                                Text(
                                    text = data.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "조회 ${data.viewCount} · 추천 ${data.recommendCount} · 댓글 ${data.commentCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                val createdLabel = toSecondPrecision(data.createdAt)
                                Text(
                                    text = "작성 $createdLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                val updatedAt = data.updatedAt
                                if (!updatedAt.isNullOrBlank()) {
                                    val updatedLabel = toSecondPrecision(updatedAt)
                                    Text(
                                        text = "수정 $updatedLabel",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text("댓글", style = MaterialTheme.typography.titleMedium)

                                OutlinedTextField(
                                    value = commentDraft,
                                    onValueChange = { commentDraft = it },
                                    label = { Text("댓글을 입력하세요") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    enabled = !isSubmittingComment,
                                )

                                if (submitCommentErrorMessage != null) {
                                    Text(submitCommentErrorMessage!!, color = MaterialTheme.colorScheme.error)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TextButton(
                                        onClick = { scope.launch { loadComments(reset = true) } },
                                        enabled = !isCommentsLoading,
                                    ) {
                                        Text("새로고침")
                                    }
                                    Box(Modifier.weight(1f))
                                    Button(
                                        onClick = { scope.launch { submitComment() } },
                                        enabled = commentDraft.trim().isNotBlank() && !isSubmittingComment,
                                    ) {
                                        Text(if (isSubmittingComment) "작성 중..." else "작성")
                                    }
                                }
                            }
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                if (commentsErrorMessage != null && comments.isEmpty()) {
                                    Text(commentsErrorMessage!!, color = MaterialTheme.colorScheme.error)
                                    TextButton(onClick = { scope.launch { loadComments(reset = true) } }) {
                                        Text("다시 시도")
                                    }
                                } else {
                                    if (comments.isEmpty() && isCommentsLoading) {
                                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    } else if (comments.isEmpty()) {
                                        Text(
                                            "아직 댓글이 없어요. 첫 댓글을 남겨보세요!",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    } else {
                                        comments.forEach { comment ->
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                CommunityAuthorLine(
                                                    nickname = comment.authorName ?: "익명",
                                                    totalDistanceKm = comment.authorTotalDistanceKm,
                                                    showTotalDistance = showTotalDistanceInCommunity,
                                                )
                                                Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                                                val createdLabel = toSecondPrecision(comment.createdAt)
                                                val updatedLabel = comment.updatedAt?.let(::toSecondPrecision)
                                                Text(
                                                    text = buildString {
                                                        append(createdLabel)
                                                        if (!updatedLabel.isNullOrBlank() && updatedLabel != createdLabel) {
                                                            append(" · 수정 ")
                                                            append(updatedLabel)
                                                        }
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                HorizontalDivider()
                                            }
                                        }

                                        if (commentsErrorMessage != null) {
                                            Text(commentsErrorMessage!!, color = MaterialTheme.colorScheme.error)
                                        }

                                        if (commentsNextCursor != null) {
                                            TextButton(
                                                onClick = { scope.launch { loadComments(reset = false) } },
                                                enabled = !isCommentsLoading,
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(if (isCommentsLoading) "불러오는 중..." else "댓글 더 보기")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
