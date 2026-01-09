package com.runners.app.community.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runners.app.community.viewmodel.CommunityViewModel
import com.runners.app.settings.AppSettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityCreatePostScreen(
    authorNickname: String,
    totalDistanceKm: Double?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel,
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val initialSuccessSignal = remember { uiState.createSuccessSignal }
    val showTotalDistanceInCommunity =
        AppSettingsStore.showTotalDistanceInCommunityFlow(context)
            .collectAsStateWithLifecycle(initialValue = true)
            .value

    LaunchedEffect(uiState.createSuccessSignal) {
        if (uiState.createSuccessSignal > initialSuccessSignal) {
            onBack()
        }
    }

    fun cancelAndBack() {
        if (uiState.isCreating) return
        viewModel.resetCreateDraft()
        onBack()
    }

    val canSubmit =
        uiState.createTitle.trim().isNotBlank() &&
            uiState.createContent.trim().isNotBlank() &&
            !uiState.isCreating

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("게시글 작성") },
                navigationIcon = {
                    IconButton(onClick = ::cancelAndBack, enabled = !uiState.isCreating) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = ::cancelAndBack, enabled = !uiState.isCreating) {
                        Text("취소")
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                HorizontalDivider()
                Button(
                    onClick = viewModel::submitCreatePost,
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Text(if (uiState.isCreating) "작성 중..." else "작성")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CommunityAuthorLine(
                nickname = authorNickname,
                totalDistanceKm = totalDistanceKm,
                showTotalDistance = showTotalDistanceInCommunity,
            )

            OutlinedTextField(
                value = uiState.createTitle,
                onValueChange = { viewModel.onCreateTitleChange(it.take(200)) },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isCreating,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                value = uiState.createContent,
                onValueChange = viewModel::onCreateContentChange,
                label = { Text("내용") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8,
                enabled = !uiState.isCreating,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            )

            if (uiState.createErrorMessage != null) {
                Text(uiState.createErrorMessage, color = MaterialTheme.colorScheme.error)
            }

            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("첨부 (준비 중)", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "추후 사진 업로드나 링크 첨부 기능을 여기에 추가할 수 있어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("• 사진", style = MaterialTheme.typography.bodySmall)
                    Text("• 링크", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
