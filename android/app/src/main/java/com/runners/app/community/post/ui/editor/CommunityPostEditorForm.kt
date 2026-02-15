package com.runners.app.community.post.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.runners.app.network.CommunityPostBoardType

data class CommunityPostEditorImageItem(
  val id: String,
  val model: Any,
  val contentDescription: String = "첨부 이미지",
)

@Composable
fun CommunityPostEditorForm(
  title: String,
  content: String,
  selectedBoardType: CommunityPostBoardType,
  images: List<CommunityPostEditorImageItem>,
  isSubmitting: Boolean,
  errorMessage: String?,
  onBoardTypeChange: (CommunityPostBoardType) -> Unit,
  onTitleChange: (String) -> Unit,
  onContentChange: (String) -> Unit,
  onAddImages: () -> Unit,
  onRemoveImage: (CommunityPostEditorImageItem) -> Unit,
  modifier: Modifier = Modifier,
  contentPlaceholder: String = "내용을 입력하세요",
  maxImages: Int = 10,
  addImageButtonLabel: String = "사진 선택",
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
      ),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "게시판",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Medium,
        )

        listOf(
          CommunityPostBoardType.FREE,
          CommunityPostBoardType.QNA,
          CommunityPostBoardType.INFO,
        ).forEach { type ->
          FilterChip(
            selected = selectedBoardType == type,
            onClick = { onBoardTypeChange(type) },
            enabled = !isSubmitting,
            label = { Text(type.labelKo) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor =
                  MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor =
                  MaterialTheme.colorScheme.onPrimaryContainer,
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              labelColor = MaterialTheme.colorScheme.onSurface,
            ),
          )
        }
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "제목",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
      )
      TextField(
        value = title,
        onValueChange = onTitleChange,
        placeholder = { Text("제목을 입력하세요") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isSubmitting,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
          imeAction = ImeAction.Next,
        ),
        shape = RoundedCornerShape(10.dp),
        supportingText = {
          Text(
            text = "${title.length}/200",
            style = MaterialTheme.typography.bodySmall,
          )
        },
        colors = TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          disabledContainerColor = MaterialTheme.colorScheme.surface,
          focusedIndicatorColor = MaterialTheme.colorScheme.primary,
          unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
          disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "내용",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
      )
      TextField(
        value = content,
        onValueChange = onContentChange,
        placeholder = {
          Text(
            text = contentPlaceholder,
            style = MaterialTheme.typography.bodyMedium,
          )
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = 10,
        enabled = !isSubmitting,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
          imeAction = ImeAction.Default,
        ),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          disabledContainerColor = MaterialTheme.colorScheme.surface,
          focusedIndicatorColor = MaterialTheme.colorScheme.primary,
          unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
          disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
      )
    }

    if (errorMessage != null) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text(
          text = errorMessage,
          color = MaterialTheme.colorScheme.onErrorContainer,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(16.dp),
        )
      }
    }

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
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
            "사진 첨부",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            "(${images.size}/$maxImages)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Box(modifier = Modifier.weight(1f))
          TextButton(
            onClick = onAddImages,
            enabled = !isSubmitting && images.size < maxImages,
          ) {
            Text(addImageButtonLabel)
          }
        }

        if (images.isNotEmpty()) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items = images, key = { image -> image.id }) { image ->
              Box(
                modifier = Modifier
                  .size(86.dp)
                  .clip(RoundedCornerShape(12.dp)),
              ) {
                AsyncImage(
                  model = image.model,
                  contentDescription = image.contentDescription,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                )
                IconButton(
                  onClick = { onRemoveImage(image) },
                  modifier = Modifier.align(Alignment.TopEnd),
                  enabled = !isSubmitting,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
