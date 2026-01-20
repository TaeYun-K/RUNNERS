package com.runners.app.community.comment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun CommunityPostDetailCommentComposer(
    value: String,
    onValueChange: (String) -> Unit,
    replyTargetCommentId: Long?,
    replyTargetAuthorName: String?,
    onCancelReply: () -> Unit,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    submitErrorMessage: String?,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (replyTargetCommentId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${replyTargetAuthorName ?: "익명"}님에게 답글 작성 중",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(
                            onClick = onCancelReply,
                            enabled = !isSubmitting,
                        ) {
                            Text("취소")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
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
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = {
                            Text(
                                if (replyTargetCommentId != null) "답글을 입력하세요..." else "댓글을 입력하세요...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        ),
                    )

                    IconButton(
                        onClick = onSubmit,
                        enabled = canSubmit,
                        modifier = Modifier.size(44.dp),
                    ) {
                        if (isSubmitting) {
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
                                tint =
                                    if (canSubmit) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (submitErrorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = submitErrorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
