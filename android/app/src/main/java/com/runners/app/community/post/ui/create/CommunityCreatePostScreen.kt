package com.runners.app.community.post.ui.create

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runners.app.community.post.ui.editor.CommunityPostEditorForm
import com.runners.app.community.post.ui.editor.CommunityPostEditorImageItem
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
            CommunityPostEditorForm(
                title = uiState.createTitle,
                content = uiState.createContent,
                selectedBoardType = uiState.createBoardType,
                images = uiState.createImageUris.map { uriString ->
                    CommunityPostEditorImageItem(
                        id = uriString,
                        model = Uri.parse(uriString),
                    )
                },
                isSubmitting = uiState.isCreating,
                errorMessage = uiState.createErrorMessage,
                onBoardTypeChange = viewModel::onCreateBoardTypeChange,
                onTitleChange = { viewModel.onCreateTitleChange(it.take(200)) },
                onContentChange = viewModel::onCreateContentChange,
                onAddImages = {
                    if (uiState.isCreating) return@CommunityPostEditorForm
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemoveImage = { image ->
                    viewModel.removeCreateImage(image.id)
                },
                contentPlaceholder = contentPlaceholder,
                addImageButtonLabel = "사진 선택",
            )
        }
    }
}
