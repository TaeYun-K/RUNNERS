package com.runners.app.community.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val pullToRefreshState = rememberPullToRefreshState()

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "게시글",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack(post) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val canSubmit = commentDraft.trim().isNotBlank() && !isSubmittingComment

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // 사용자 아바타
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "U",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }

                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            placeholder = {
                                Text(
                                    "댓글을 입력하세요...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !isSubmittingComment,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            ),
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSubmit) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(
                                onClick = { scope.launch { submitComment() } },
                                enabled = canSubmit,
                                modifier = Modifier.size(44.dp),
                            ) {
                                if (isSubmittingComment) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "댓글 작성",
                                        modifier = Modifier.size(20.dp),
                                        tint = if (canSubmit) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    if (submitCommentErrorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = submitCommentErrorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading || isCommentsLoading,
            onRefresh = { scope.launch { loadPost(); loadComments(reset = true) } },
            state = pullToRefreshState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                isLoading && post == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                        )
                    }
                }

                errorMessage != null && post == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
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
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // 제목
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            // 작성자 정보
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                CommunityAuthorLine(
                                    nickname = data.authorName ?: "익명",
                                    totalDistanceKm = data.authorTotalDistanceKm,
                                    showTotalDistance = showTotalDistanceInCommunity,
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // 본문
                            Text(
                                text = data.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )

                            // 날짜 + 통계
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val createdLabel = toSecondPrecision(data.createdAt)
                                    Text(
                                        text = createdLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    val updatedAt = data.updatedAt
                                    if (!updatedAt.isNullOrBlank()) {
                                        Text(
                                            text = "  ·  (수정됨)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CommonPostStat(
                                        icon = Icons.Outlined.ThumbUp,
                                        text = "추천 ${data.recommendCount}",
                                        iconColor = MaterialTheme.colorScheme.primary,
                                    )
                                    CommonPostStat(
                                        icon = Icons.Outlined.ChatBubbleOutline,
                                        text = "댓글 ${data.commentCount}",
                                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    CommonPostStat(
                                        icon = Icons.Outlined.RemoveRedEye,
                                        text = "조회수 ${data.viewCount}",
                                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }

                            // 댓글 섹션
                            Column (
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = "댓글",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "${comments.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                if (commentsErrorMessage != null && comments.isEmpty()) {
                                    Text(
                                        text = commentsErrorMessage!!,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                    TextButton(onClick = { scope.launch { loadComments(reset = true) } }) {
                                        Text("다시 시도")
                                    }
                                } else {
                                    if (comments.isEmpty() && isCommentsLoading) {
                                        Box(
                                            Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                            )
                                        }
                                    } else if (comments.isEmpty()) {
                                        Text(
                                            text = "아직 댓글이 없어요. 첫 댓글을 남겨보세요!",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    } else {
                                        comments.forEach { comment ->
                                            CommentItem(
                                                comment = comment,
                                                showTotalDistance = showTotalDistanceInCommunity,
                                                toSecondPrecision = ::toSecondPrecision,
                                            )
                                        }

                                        if (commentsErrorMessage != null) {
                                            Text(
                                                text = commentsErrorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }

                                        if (commentsNextCursor != null) {
                                            TextButton(
                                                onClick = { scope.launch { loadComments(reset = false) } },
                                                enabled = !isCommentsLoading,
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(
                                                    text = if (isCommentsLoading) "불러오는 중..." else "댓글 더 보기",
                                                    fontWeight = FontWeight.Medium,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommonPostStat(
    icon: ImageVector,
    text: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CommentItem(
    comment: CommunityCommentResult,
    showTotalDistance: Boolean,
    toSecondPrecision: (String) -> String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 아바타
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (comment.authorName?.firstOrNull() ?: "?").toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = comment.authorName ?: "익명",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (showTotalDistance && comment.authorTotalDistanceKm != null) {
                        Text(
                            text = String.format("%.1fkm", comment.authorTotalDistanceKm),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                val createdLabel = toSecondPrecision(comment.createdAt)
                val updatedLabel = comment.updatedAt?.let(toSecondPrecision)
                Text(
                    text = buildString {
                        append(createdLabel)
                        if (!updatedLabel.isNullOrBlank() && updatedLabel != createdLabel) {
                            append(" (수정됨)")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 38.dp),
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
