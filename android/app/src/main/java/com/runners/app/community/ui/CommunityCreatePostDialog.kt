package com.runners.app.community.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun CommunityCreatePostDialog(
    isOpen: Boolean,
    authorNickname: String,
    totalDistanceKm: Double?,
    showTotalDistance: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    isCreating: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = { Text("게시글 작성") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AuthorLine(
                    nickname = authorNickname,
                    totalDistanceKm = totalDistanceKm,
                    showTotalDistance = showTotalDistance,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { onTitleChange(it.take(200)) },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isCreating,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("내용") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    enabled = !isCreating,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            val canSubmit = title.isNotBlank() && content.isNotBlank() && !isCreating
            Button(onClick = onSubmit, enabled = canSubmit) {
                Text(if (isCreating) "작성 중..." else "작성")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isCreating) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun AuthorLine(
    nickname: String,
    totalDistanceKm: Double?,
    showTotalDistance: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = nickname.ifBlank { "RUNNERS" },
            style = MaterialTheme.typography.titleSmall,
        )

        if (showTotalDistance) {
            Spacer(Modifier.width(8.dp))
            val distanceLabel = totalDistanceKm?.let { "· ${formatKm(it)}" } ?: "· km 정보 없음"
            Text(
                text = distanceLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatKm(km: Double): String =
    String.format(Locale.getDefault(), "%.1fkm", km.coerceAtLeast(0.0))
