package com.runners.app.community.post.ui.create

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.runners.app.community.post.viewmodel.CommunityViewModel
import com.runners.app.network.CommunityPostBoardType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityCreatePostScreen(
    authorNickname: String,
    authorPictureUrl: String? = null,
    totalDistanceKm: Double?,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel,
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val initialSuccessSignal = remember { uiState.createSuccessSignal }

    val pickImagesLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        ) { uris ->
            viewModel.addCreateImages(uris)
        }

    LaunchedEffect(uiState.createSuccessSignal) {
        if (uiState.createSuccessSignal > initialSuccessSignal) {
            val postId = uiState.lastCreatedPostId
            if (postId != null && postId > 0) {
                viewModel.consumeLastCreatedPostId()
                onCreated(postId)
            } else {
                onBack()
            }
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

    val isSamsung = remember { Build.MANUFACTURER.equals("samsung", ignoreCase = true) }
    val imeInsets =
        if (isSamsung) WindowInsets.ime.exclude(WindowInsets.navigationBars) else WindowInsets.ime

    val isKeyboardVisible = WindowInsets.isImeVisible
    val contentPlaceholder = remember {
        """
        내용을 입력하세요.
        음란물/혐오 표현/불법 홍보성 게시물은 등록할 수 없어요.
        전화번호, 계좌번호 등 개인정보 노출에 주의해 주세요.
        """.trimIndent()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "게시글 작성",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = ::cancelAndBack, enabled = !uiState.isCreating) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(
                        onClick = ::cancelAndBack,
                        enabled = !uiState.isCreating,
                    ) {
                        Text(
                            "취소",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            if (!isKeyboardVisible) {
                Column(
                    Modifier
                        .fillMaxWidth()
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Button(
                        onClick = { viewModel.submitCreatePost(context) },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = if (uiState.isCreating) "작성 중..." else "게시글 등록",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(imeInsets.only(WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LaunchedEffect(Unit) {
                if (uiState.createTitle.isBlank() && uiState.createContent.isBlank()) {
                    val initial = uiState.selectedBoardType ?: CommunityPostBoardType.FREE
                    if (uiState.createBoardType != initial) {
                        viewModel.onCreateBoardTypeChange(initial)
                    }
                }
            }

            // 게시판 선택
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            selected = uiState.createBoardType == type,
                            onClick = { viewModel.onCreateBoardTypeChange(type) },
                            enabled = !uiState.isCreating,
                            label = { Text(type.labelKo) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            )
                        )
                    }
                }
            }

            // 제목 입력
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "제목",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                TextField(
                    value = uiState.createTitle,
                    onValueChange = { viewModel.onCreateTitleChange(it.take(200)) },
                    placeholder = { Text("제목을 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isCreating,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(10.dp),
                    supportingText = {
                        Text(
                            text = "${uiState.createTitle.length}/200",
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

            // 내용 입력
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "내용",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                TextField(
                    value = uiState.createContent,
                    onValueChange = viewModel::onCreateContentChange,
                    placeholder = {
                        Text(
                            text = contentPlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 10,
                    enabled = !uiState.isCreating,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
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

            // 에러 메시지
            if (uiState.createErrorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = uiState.createErrorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            // 사진 첨부
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
                            "(${uiState.createImageUris.size}/10)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                if (uiState.isCreating) return@TextButton
                                pickImagesLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !uiState.isCreating,
                        ) {
                            Text("사진 선택")
                        }
                    }

                    if (uiState.createImageUris.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.createImageUris.size) { index ->
                                val uriString = uiState.createImageUris[index]
                                Box(
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                ) {
                                    AsyncImage(
                                        model = Uri.parse(uriString),
                                        contentDescription = "첨부 이미지",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp)),
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeCreateImage(uriString) },
                                        modifier = Modifier.align(Alignment.TopEnd),
                                        enabled = !uiState.isCreating,
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
}
