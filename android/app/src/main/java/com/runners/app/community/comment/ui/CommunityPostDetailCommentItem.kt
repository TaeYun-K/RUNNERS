package com.runners.app.community.comment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.runners.app.community.post.ui.detail.shouldShowEditedBadge
import com.runners.app.community.post.ui.detail.toSecondPrecision
import com.runners.app.network.CommunityCommentResult

@Composable
internal fun CommunityPostDetailCommentItem(
    comment: CommunityCommentResult,
    currentUserId: Long,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    isEditing: Boolean,
    editingDraft: String,
    onEditingDraftChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditCancel: () -> Unit,
    onEditSave: () -> Unit,
    isEditSaving: Boolean,
    showTotalDistance: Boolean,
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

            Column(modifier = Modifier.weight(1f)) {
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

            if (canManage) {
                Box {
                    IconButton(onClick = { onMenuExpandedChange(!menuExpanded) }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { onMenuExpandedChange(false) },
                    ) {
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

