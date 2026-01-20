package com.runners.app.community.post.ui.detail

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.runners.app.community.comment.ui.CommunityPostDetailCommentComposer
import com.runners.app.community.comment.ui.CommunityPostDetailCommentItem
import com.runners.app.community.post.state.CommunityPostDetailUiState
import com.runners.app.community.post.ui.components.CommunityAuthorLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityPostDetailContent(
    uiState: CommunityPostDetailUiState,
    currentUserId: Long,
    showTotalDistance: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
    onCommentDraftChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onStartEditingComment: (commentId: Long, initialContent: String) -> Unit,
    onCancelEditingComment: () -> Unit,
    onEditingCommentDraftChange: (String) -> Unit,
    onSubmitEditingComment: () -> Unit,
    onRequestDeleteComment: (Long) -> Unit,
    onCancelDeleteComment: () -> Unit,
    onConfirmDeleteComment: () -> Unit,
    onLoadMoreComments: () -> Unit,
    onDeletePost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuOpenedCommentId by remember { mutableStateOf<Long?>(null) }
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "게시글",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    val canEdit = uiState.post?.authorId == currentUserId
                    if (canEdit) {
                        TextButton(
                            onClick = onEdit,
                            enabled = !uiState.isPostLoading &&
                                !uiState.isUpdatingPost &&
                                !uiState.isDeletingPost,
                        ) {
                            Text("수정")
                        }
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            enabled = !uiState.isPostLoading &&
                                !uiState.isUpdatingPost &&
                                !uiState.isDeletingPost,
                        ) {
                            Text(
                                text = "삭제",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            val isSamsung = remember { Build.MANUFACTURER.equals("samsung", ignoreCase = true) }
            val imeInsets =
                if (isSamsung) WindowInsets.ime.exclude(WindowInsets.navigationBars) else WindowInsets.ime

            CommunityPostDetailCommentComposer(
                value = uiState.commentDraft,
                onValueChange = onCommentDraftChange,
                canSubmit = uiState.canSubmitComment,
                isSubmitting = uiState.isSubmittingComment,
                submitErrorMessage = uiState.submitCommentErrorMessage,
                onSubmit = onSubmitComment,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        imeInsets.only(WindowInsetsSides.Bottom),
                    )
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 0.dp),
            )
        },
    ) { innerPadding ->
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("게시글 삭제") },
                text = { Text("정말 이 게시글을 삭제할까요?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            onDeletePost()
                        },
                        enabled = !uiState.isDeletingPost,
                    ) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        enabled = !uiState.isDeletingPost,
                    ) {
                        Text("취소")
                    }
                },
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                if (!uiState.isRefreshing && !uiState.isSubmittingComment) {
                    onRefresh()
                }
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                uiState.isPostLoading && uiState.post == null -> {
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

                uiState.postErrorMessage != null && uiState.post == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = uiState.postErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(onClick = onRefresh) {
                            Text("다시 시도")
                        }
                    }
                }

                else -> {
                    val data = uiState.post
                    if (data != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 12.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item(key = "title") {
                                Text(
                                    text = data.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }

                            item(key = "author") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    CommunityAuthorLine(
                                        nickname = data.authorName ?: "익명",
                                        totalDistanceKm = data.authorTotalDistanceKm,
                                        showTotalDistance = showTotalDistance,
                                    )
                                }
                            }

                            item(key = "divider-author") {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }

                            item(key = "content") {
                                Text(
                                    text = data.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }

                            item(key = "meta") {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val createdLabel = remember(data.createdAt) {
                                            toSecondPrecision(
                                                data.createdAt
                                            )
                                        }
                                        val showEdited = remember(data.createdAt, data.updatedAt) {
                                            shouldShowEditedBadge(
                                                createdAt = data.createdAt,
                                                updatedAt = data.updatedAt
                                            )
                                        }
                                        Text(
                                            text = createdLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )

                                        if (showEdited) {
                                            Text(
                                                text = "  ·  (수정됨)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CommunityPostDetailCommonPostStat(
                                            icon = Icons.Outlined.ThumbUp,
                                            text = "추천 ${data.recommendCount}",
                                            iconColor = MaterialTheme.colorScheme.primary,
                                        )
                                        CommunityPostDetailCommonPostStat(
                                            icon = Icons.Outlined.ChatBubbleOutline,
                                            text = "댓글 ${data.commentCount}",
                                            iconColor = MaterialTheme.colorScheme.secondary,
                                        )
                                        CommunityPostDetailCommonPostStat(
                                            icon = Icons.Outlined.RemoveRedEye,
                                            text = "조회 ${data.viewCount}",
                                            iconColor = MaterialTheme.colorScheme.tertiary,
                                        )
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }

                            item(key = "comments-title") {
                                Text(
                                    text = "댓글",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            when {
                                uiState.commentsErrorMessage != null && uiState.comments.isEmpty() -> {
                                    item(key = "comments-error") {
                                        Text(
                                            text = uiState.commentsErrorMessage!!,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    item(key = "comments-retry") {
                                        TextButton(onClick = onRefresh) {
                                            Text("다시 시도")
                                        }
                                    }
                                }

                                uiState.comments.isEmpty() && uiState.isCommentsLoading -> {
                                    item(key = "comments-loading") {
                                        Box(
                                            Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                            )
                                        }
                                    }
                                }

                                uiState.comments.isEmpty() -> {
                                    item(key = "comments-empty") {
                                        Text(
                                            text = "아직 댓글이 없어요. 첫 댓글을 남겨보세요!",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }

                                else -> {
                                    items(
                                        items = uiState.comments,
                                        key = { it.commentId },
                                    ) { comment ->
                                        CommunityPostDetailCommentItem(
                                            comment = comment,
                                            currentUserId = currentUserId,
                                            menuExpanded = menuOpenedCommentId == comment.commentId,
                                            onMenuExpandedChange = { expanded ->
                                                menuOpenedCommentId =
                                                    if (expanded) comment.commentId else null
                                            },
                                            isEditing = uiState.editingCommentId == comment.commentId,
                                            editingDraft = uiState.editingCommentDraft,
                                            onEditingDraftChange = onEditingCommentDraftChange,
                                            onEditClick = {
                                                menuOpenedCommentId = null
                                                onStartEditingComment(
                                                    comment.commentId,
                                                    comment.content,
                                                )
                                            },
                                            onDeleteClick = {
                                                menuOpenedCommentId = null
                                                onRequestDeleteComment(comment.commentId)
                                            },
                                            onEditCancel = onCancelEditingComment,
                                            onEditSave = onSubmitEditingComment,
                                            isEditSaving = uiState.isUpdatingComment,
                                            showTotalDistance = showTotalDistance,
                                        )
                                    }

                                    if (uiState.updateCommentErrorMessage != null) {
                                        item(key = "comments-update-error") {
                                            Text(
                                                text = uiState.updateCommentErrorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                    if (uiState.deleteCommentErrorMessage != null) {
                                        item(key = "comments-delete-error") {
                                            Text(
                                                text = uiState.deleteCommentErrorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }

                                    if (uiState.commentsErrorMessage != null) {
                                        item(key = "comments-more-error") {
                                            Text(
                                                text = uiState.commentsErrorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }

                                    if (uiState.commentsNextCursor != null) {
                                        item(key = "comments-more") {
                                            TextButton(
                                                onClick = onLoadMoreComments,
                                                enabled = !uiState.isCommentsLoading,
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(
                                                    text = if (uiState.isCommentsLoading) "불러오는 중..." else "댓글 더 보기",
                                                    fontWeight = FontWeight.Medium,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item(key = "bottom-spacer") {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.deleteCommentTargetId != null) {
        AlertDialog(
            onDismissRequest = onCancelDeleteComment,
            title = { Text("댓글 삭제") },
            text = { Text("이 댓글을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDeleteComment,
                    enabled = !uiState.isDeletingComment,
                ) {
                    Text(if (uiState.isDeletingComment) "삭제 중..." else "삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelDeleteComment,
                    enabled = !uiState.isDeletingComment,
                ) {
                    Text("취소")
                }
            },
        )
    }
}
