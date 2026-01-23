package com.runners.app.community.comment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.runners.app.community.post.ui.detail.shouldShowEditedBadge
import com.runners.app.community.post.ui.detail.toSecondPrecision
import com.runners.app.network.CommunityCommentResult
import coil.compose.AsyncImage

@Composable
internal fun CommunityPostDetailCommentItem(
    comment: CommunityCommentResult,
    isReply: Boolean,
    currentUserId: Long,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    isEditing: Boolean,
    editingDraft: String,
    onEditingDraftChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReplyClick: () -> Unit,
    isRecommended: Boolean,
    onLikeClick: () -> Unit,
    onEditCancel: () -> Unit,
    onEditSave: () -> Unit,
    isEditSaving: Boolean,
    showTotalDistance: Boolean,
    onAuthorClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDeleted = comment.content == "삭제된 댓글입니다"
    val canManage = !isDeleted && comment.authorId == currentUserId
    val createdLabel = remember(comment.createdAt) { toSecondPrecision(comment.createdAt) }
    val showEdited = remember(comment.createdAt, comment.updatedAt, isDeleted) {
        !isDeleted && shouldShowEditedBadge(comment.createdAt, comment.updatedAt)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isReply) {
                Text(
                    text = "↳",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = { onAuthorClick(comment.authorId) }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val authorPicture = comment.authorPicture
                if (!authorPicture.isNullOrBlank()) {
                    AsyncImage(
                        model = authorPicture,
                        contentDescription = "작성자 프로필",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                    )
                } else {
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
                    Text(
                        text = buildString {
                            append(createdLabel)
                            if (showEdited) {
                                append(" (수정됨)")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!isDeleted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (!isReply) {
                        IconButton(
                            onClick = onReplyClick,
                            enabled = !isEditing && !isEditSaving,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Reply",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    IconButton(
                        onClick = onLikeClick,
                        enabled = !isEditing && !isEditSaving,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isRecommended) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Recommend",
                            tint = if (isRecommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = comment.recommendCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Box {
                        IconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = { onMenuExpandedChange(!menuExpanded) }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { onMenuExpandedChange(false) },
                        ) {
                            if (!canManage) {
                                DropdownMenuItem(
                                    text = { Text("본인 댓글만 수정/삭제 가능") },
                                    enabled = false,
                                    onClick = { onMenuExpandedChange(false) },
                                )
                                return@DropdownMenu
                            }

                            DropdownMenuItem(
                                text = { Text("수정") },
                                onClick = {
                                    onMenuExpandedChange(false)
                                    onEditClick()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("삭제") },
                                onClick = {
                                    onMenuExpandedChange(false)
                                    onDeleteClick()
                                },
                            )
                        }
                    }
                }
            }
        }

        if (isEditing) {
            Column(modifier = Modifier.padding(start = 38.dp)) {
                OutlinedTextField(
                    value = editingDraft,
                    onValueChange = onEditingDraftChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    enabled = !isEditSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onEditCancel,
                        enabled = !isEditSaving,
                    ) {
                        Text("취소")
                    }
                    TextButton(
                        onClick = onEditSave,
                        enabled = !isEditSaving && editingDraft.trim().isNotBlank(),
                    ) {
                        Text(if (isEditSaving) "저장 중..." else "저장")
                    }
                }
            }
        } else {
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 38.dp),
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
