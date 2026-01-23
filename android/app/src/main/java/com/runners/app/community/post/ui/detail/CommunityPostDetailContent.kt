package com.runners.app.community.post.ui.detail

import android.os.Build
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.runners.app.ads.InlineBannerAd
import com.runners.app.community.comment.ui.CommunityPostDetailCommentComposer
import com.runners.app.community.comment.ui.CommunityPostDetailCommentItem
import com.runners.app.community.post.state.CommunityPostDetailUiState
import com.runners.app.community.post.ui.components.CommunityAuthorLine
import com.runners.app.community.post.ui.components.FullScreenImageViewerDialog
import com.runners.app.network.CommunityCommentResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityPostDetailContent(
    uiState: CommunityPostDetailUiState,
    currentUserId: Long,
    showTotalDistance: Boolean,
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
    onTogglePostRecommend: () -> Unit,
    onCommentDraftChange: (String) -> Unit,
    onStartReply: (commentId: Long, authorName: String?) -> Unit,
    onCancelReply: () -> Unit,
    onSubmitComment: () -> Unit,
    onStartEditingComment: (commentId: Long, initialContent: String) -> Unit,
    onCancelEditingComment: () -> Unit,
    onEditingCommentDraftChange: (String) -> Unit,
    onSubmitEditingComment: () -> Unit,
    onRequestDeleteComment: (Long) -> Unit,
    onCancelDeleteComment: () -> Unit,
    onConfirmDeleteComment: () -> Unit,
    onToggleCommentRecommend: (Long) -> Unit,
    onLoadMoreComments: () -> Unit,
    onDeletePost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuOpenedCommentId by remember { mutableStateOf<Long?>(null) }
    var postMenuExpanded by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val threadedComments = remember(uiState.comments) { threadCommunityComments(uiState.comments) }

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
                    val canManage = uiState.post?.authorId == currentUserId
                    val canClick =
                        !uiState.isPostLoading &&
                            !uiState.isUpdatingPost &&
                            !uiState.isDeletingPost

                    IconButton(
                        onClick = { postMenuExpanded = true },
                        enabled = canClick,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More",
                        )
                    }

                    DropdownMenu(
                        expanded = postMenuExpanded,
                        onDismissRequest = { postMenuExpanded = false },
                    ) {
                        if (canManage) {
                            DropdownMenuItem(
                                text = { Text("수정") },
                                onClick = {
                                    postMenuExpanded = false
                                    onEdit()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("삭제") },
                                onClick = {
                                    postMenuExpanded = false
                                    showDeleteConfirm = true
                                },
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("신고 (준비중)") },
                            enabled = false,
                            onClick = { postMenuExpanded = false },
                        )
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
                replyTargetCommentId = uiState.replyTargetCommentId,
                replyTargetAuthorName = uiState.replyTargetAuthorName,
                onCancelReply = onCancelReply,
                canSubmit = uiState.canSubmitComment,
                isSubmitting = uiState.isSubmittingComment,
                submitErrorMessage = uiState.submitCommentErrorMessage,
                onSubmit = onSubmitComment,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        imeInsets.only(WindowInsetsSides.Bottom),
                    )
                    .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 0.dp),
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
                        var viewerOpen by remember { mutableStateOf(false) }
                        var viewerInitialIndex by remember { mutableStateOf(0) }

                        if (viewerOpen) {
                            FullScreenImageViewerDialog(
                                imageUrls = data.imageUrls,
                                initialIndex = viewerInitialIndex,
                                onDismissRequest = { viewerOpen = false },
                            )
                        }

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
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    CommunityAuthorLine(
                                        nickname = data.authorName ?: "익명",
                                        pictureUrl = data.authorPicture,
                                        totalDistanceKm = data.authorTotalDistanceKm,
                                        showTotalDistance = showTotalDistance,
                                        onClick = { onAuthorClick(data.authorId) },
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val createdLabel = remember(data.createdAt) {
                                            toSecondPrecision(data.createdAt)
                                        }
                                        val showEdited = remember(data.createdAt, data.updatedAt) {
                                            shouldShowEditedBadge(
                                                createdAt = data.createdAt,
                                                updatedAt = data.updatedAt,
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
                                }
                            }

                            item(key = "divider-author") {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }

                            if (data.imageUrls.isNotEmpty()) {
                                item(key = "images") {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        itemsIndexed(items = data.imageUrls, key = { _, url -> url }) { index, url ->
                                            AsyncImage(
                                                model = url,
                                                contentDescription = "게시글 이미지",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(220.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        viewerInitialIndex = index
                                                        viewerOpen = true
                                                    },
                                            )
                                        }
                                    }
                                }

                                item(key = "divider-images") {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
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
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        TextButton(
                                            onClick = onTogglePostRecommend,
                                            enabled = !uiState.isTogglingPostRecommend,
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        ) {
                                            CommunityPostDetailCommonPostStat(
                                                icon = if (uiState.isPostRecommended) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                                text = "추천 ${data.recommendCount}",
                                                iconColor = MaterialTheme.colorScheme.primary,
                                            )
                                        }
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

                                    if (uiState.togglePostRecommendErrorMessage != null) {
                                        Text(
                                            text = uiState.togglePostRecommendErrorMessage!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }

                            item(key = "detail-inline-banner-ad") {
                                InlineBannerAd()
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                                        items = threadedComments,
                                        key = { it.comment.commentId },
                                    ) { comment ->
                                        val depthIndent = (comment.depth * 16).dp
                                        val isRecommended =
                                            uiState.commentState.recommendedCommentIds.contains(comment.comment.commentId)
                                        val isRecommendLoading =
                                            uiState.commentState.recommendingCommentIds.contains(comment.comment.commentId)
                                        CommunityPostDetailCommentItem(
                                            comment = comment.comment,
                                            isReply = comment.depth > 0,
                                            currentUserId = currentUserId,
                                            menuExpanded = menuOpenedCommentId == comment.comment.commentId,
                                            onMenuExpandedChange = { expanded ->
                                                menuOpenedCommentId =
                                                    if (expanded) comment.comment.commentId else null
                                            },
                                            isEditing = uiState.editingCommentId == comment.comment.commentId,
                                            editingDraft = uiState.editingCommentDraft,
                                            onEditingDraftChange = onEditingCommentDraftChange,
                                            onEditClick = {
                                                menuOpenedCommentId = null
                                                onStartEditingComment(
                                                    comment.comment.commentId,
                                                    comment.comment.content,
                                                )
                                            },
                                            onDeleteClick = {
                                                menuOpenedCommentId = null
                                                onRequestDeleteComment(comment.comment.commentId)
                                            },
                                            onReplyClick = {
                                                onStartReply(comment.comment.commentId, comment.comment.authorName)
                                            },
                                            isRecommended = isRecommended,
                                            onLikeClick = { onToggleCommentRecommend(comment.comment.commentId) },
                                            onEditCancel = onCancelEditingComment,
                                            onEditSave = onSubmitEditingComment,
                                            isEditSaving = uiState.isUpdatingComment || isRecommendLoading,
                                            showTotalDistance = showTotalDistance,
                                            onAuthorClick = onAuthorClick,
                                            modifier = Modifier.padding(start = depthIndent),
                                        )
                                    }

                                    if (uiState.recommendCommentErrorMessage != null) {
                                        item(key = "comments-recommend-error") {
                                            Text(
                                                text = uiState.recommendCommentErrorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
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

private data class ThreadedCommunityComment(
    val comment: CommunityCommentResult,
    val depth: Int,
)

private fun threadCommunityComments(comments: List<CommunityCommentResult>): List<ThreadedCommunityComment> {
    if (comments.isEmpty()) return emptyList()

    val indexByCommentId = HashMap<Long, Int>(comments.size)
    val commentById = HashMap<Long, CommunityCommentResult>(comments.size)
    for ((index, comment) in comments.withIndex()) {
        indexByCommentId[comment.commentId] = index
        commentById[comment.commentId] = comment
    }

    val childrenByParentId = HashMap<Long, MutableList<CommunityCommentResult>>()
    for (comment in comments) {
        val parentId = comment.parentId ?: continue
        childrenByParentId.getOrPut(parentId) { ArrayList() }.add(comment)
    }

    val roots =
        comments
            .filter { it.parentId == null || commentById[it.parentId] == null }
            .sortedBy { indexByCommentId[it.commentId] ?: Int.MAX_VALUE }

    val result = ArrayList<ThreadedCommunityComment>(comments.size)
    val visited = HashSet<Long>(comments.size)

    fun visit(comment: CommunityCommentResult, depth: Int) {
        if (!visited.add(comment.commentId)) return
        result.add(ThreadedCommunityComment(comment = comment, depth = depth))
        val children =
            childrenByParentId[comment.commentId]
                ?.sortedBy { child -> indexByCommentId[child.commentId] ?: Int.MAX_VALUE }
                .orEmpty()
        for (child in children) {
            visit(child, depth + 1)
        }
    }

    for (root in roots) {
        visit(root, depth = 0)
    }

    // Fallback: include comments that couldn't be threaded (cycles, missing roots).
    if (result.size != comments.size) {
        val leftovers =
            comments
                .filterNot { visited.contains(it.commentId) }
                .sortedBy { indexByCommentId[it.commentId] ?: Int.MAX_VALUE }
        for (comment in leftovers) {
            visit(comment, depth = 0)
        }
    }

    return result
}
