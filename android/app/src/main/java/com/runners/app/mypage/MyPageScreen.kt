package com.runners.app.mypage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.runners.app.R
import com.runners.app.ads.InlineBannerAd
import com.runners.app.auth.AuthTokenStore
import com.runners.app.mypage.components.HealthConnectSection
import com.runners.app.network.*
import com.runners.app.settings.AppSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    session: GoogleLoginResult,
    onLogout: () -> Unit,
    onHealthConnectUpdated: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.google_web_client_id)

    // Dialog States
    var isLogoutDialogOpen by remember { mutableStateOf(false) }
    var isReLoginDialogOpen by remember { mutableStateOf(false) }
    var isProfileImageOptionDialogOpen by remember { mutableStateOf(false) } // 사진 변경 옵션 다이얼로그
    var isProfileEditDialogOpen by remember { mutableStateOf(false) }

    // Loading & Data States
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userMe by remember { mutableStateOf<UserMeResult?>(null) }
    var isProfileImageUploading by remember { mutableStateOf(false) }

    // Edit States
    var profileNicknameDraft by remember { mutableStateOf("") }
    var profileIntroDraft by remember { mutableStateOf("") }
    var profileEditErrorMessage by remember { mutableStateOf<String?>(null) }
    var isProfileEditSaving by remember { mutableStateOf(false) }

    val rawNickname = (userMe?.nickname ?: session.nickname).orEmpty()
    val displayNickname = rawNickname.takeUnless { it.isBlank() } ?: "RUNNERS"
    val oneLineIntro = userMe?.intro

    val showTotalDistanceInCommunity = AppSettingsStore.showTotalDistanceInCommunityFlow(context)
        .collectAsStateWithLifecycle(initialValue = true).value

    // --- Logic Functions (기존 유지) ---
    suspend fun refreshUser() {
        if (isLoading) return
        if (AuthTokenStore.peekAccessToken().isNullOrBlank()) {
            errorMessage = null
            isReLoginDialogOpen = true
            return
        }
        isLoading = true
        errorMessage = null
        try {
            userMe = withContext(Dispatchers.IO) { BackendUserApi.getMe() }
        } catch (e: Exception) {
            if (AuthTokenStore.peekAccessToken().isNullOrBlank()) {
                errorMessage = null
                isReLoginDialogOpen = true
            } else {
                errorMessage = e.message ?: "내 정보 불러오기에 실패했어요"
            }
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshUser() }

    val pickProfileImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (isLoading || isProfileImageUploading) return@rememberLauncherForActivityResult

        scope.launch {
            isProfileImageUploading = true
            isProfileImageOptionDialogOpen = false // 다이얼로그 닫기
            errorMessage = null
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw IllegalStateException("이미지를 읽을 수 없어요")

                val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val presigned = withContext(Dispatchers.IO) {
                    BackendUserApi.presignProfileImageUpload(
                        PresignCommunityImageUploadFileRequest(
                            fileName = uri.lastPathSegment ?: "profile",
                            contentType = contentType,
                            contentLength = bytes.size.toLong(),
                        )
                    )
                }
                val item = presigned.items.firstOrNull() ?: throw IllegalStateException("업로드 URL 실패")

                withContext(Dispatchers.IO) {
                    PresignedUploadClient.put(
                        uploadUrl = item.uploadUrl,
                        contentType = item.contentType.ifBlank { contentType },
                        bytes = bytes,
                    )
                }
                userMe = withContext(Dispatchers.IO) { BackendUserApi.commitProfileImage(item.key) }
            } catch (e: Exception) {
                errorMessage = e.message ?: "프로필 사진 업로드에 실패했어요"
            } finally {
                isProfileImageUploading = false
            }
        }
    }

    // --- UI Structure ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
            InlineBannerAd(
                modifier = Modifier.fillMaxWidth(),
                compact = true,
            )

            Spacer(Modifier.height(20.dp))

            // 1. 프로필 섹션 (중앙 집중형)
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(110.dp)
                    .clickable { isProfileImageOptionDialogOpen = true } // 사진 클릭 시 옵션
            ) {
                // 프로필 이미지
                val profileImageUrl = userMe?.picture ?: session.picture
                if (!profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "프로필 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (displayNickname.firstOrNull() ?: 'R').toString().uppercase(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                // 업로드 로딩 인디케이터 or 카메라 아이콘
                if (isProfileImageUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(110.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // 카메라 아이콘 배지
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "사진 변경",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 닉네임 & 소개
            Text(
                text = displayNickname,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = oneLineIntro.takeUnless { it.isNullOrBlank() } ?: "나를 소개하는 한마디를 입력해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
            )

            // 프로필 편집 버튼
            FilledTonalButton(
                onClick = {
                    profileNicknameDraft = rawNickname
                    profileIntroDraft = oneLineIntro.orEmpty()
                    profileEditErrorMessage = null
                    isProfileEditDialogOpen = true
                },
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("프로필 편집")
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(32.dp))

            // 2. 설정 리스트 그룹
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 개인정보 그룹
                SettingsGroup(title = "계정 정보") {
                    SettingsItem(
                        icon = Icons.Outlined.Person,
                        title = "이메일",
                        value = userMe?.email ?: session.email ?: "-"
                    )
                }

                // 앱 설정 그룹
                SettingsGroup(title = "앱 설정") {
                    // 1. 커뮤니티 거리 표시 (기존 유지)
                    ListItem(
                        headlineContent = { Text("커뮤니티 거리 표시", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text("닉네임 옆에 누적 거리를 표시합니다", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingContent = {
                            Switch(
                                checked = showTotalDistanceInCommunity,
                                onCheckedChange = { scope.launch { AppSettingsStore.setShowTotalDistanceInCommunity(context, it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. 헬스 커넥트 (접었다 펼치기 기능 추가)
                    var isHealthConnectExpanded by remember { mutableStateOf(false) }
                    // 화살표 회전 애니메이션
                    val rotationState by animateFloatAsState(
                        targetValue = if (isHealthConnectExpanded) 180f else 0f,
                        label = "Rotation"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isHealthConnectExpanded = !isHealthConnectExpanded } // 클릭 시 토글
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("헬스 커넥트 설정", style = MaterialTheme.typography.bodyLarge)
                                // 접혀있을 때 간단한 안내 문구 표시 (선택사항)
                                if (!isHealthConnectExpanded) {
                                    Text(
                                        "연동 상태를 관리하려면 누르세요",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 화살표 아이콘
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "펼치기",
                                modifier = Modifier.graphicsLayer(rotationZ = rotationState), // 회전 적용
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 펼쳐졌을 때만 내용 보이기 (애니메이션 적용)
                        AnimatedVisibility(visible = isHealthConnectExpanded) {
                            Box(modifier = Modifier.padding(top = 16.dp)) {
                                HealthConnectSection(
                                    onHealthConnectUpdated = onHealthConnectUpdated,
                                )
                            }
                        }
                    }
                }

                // 로그아웃 버튼 (하단 배치)
                TextButton(
                    onClick = { isLogoutDialogOpen = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Outlined.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("로그아웃")
                }
            }

            Spacer(Modifier.height(40.dp))
        }

    // --- Dialogs ---

    // 1. 프로필 사진 변경 옵션 다이얼로그 (새로 추가됨)
    if (isProfileImageOptionDialogOpen) {
        AlertDialog(
            onDismissRequest = { isProfileImageOptionDialogOpen = false },
            title = { Text("프로필 사진", fontWeight = FontWeight.Bold) },
            text = { Text("프로필 사진을 어떻게 하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickProfileImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) { Text("앨범에서 선택") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isProfileImageUploading = true
                                isProfileImageOptionDialogOpen = false
                                try {
                                    userMe = withContext(Dispatchers.IO) { BackendUserApi.deleteProfileImage() }
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                } finally {
                                    isProfileImageUploading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("기본 이미지로 변경") }

                    TextButton(onClick = { isProfileImageOptionDialogOpen = false }) { Text("취소") }
                }
            }
        )
    }

    // 2. 프로필 편집 다이얼로그 (기존 유지 + UI 다듬기)
    if (isProfileEditDialogOpen) {
        AlertDialog(
            onDismissRequest = { if (!isProfileEditSaving) isProfileEditDialogOpen = false },
            title = { Text("프로필 편집", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = profileNicknameDraft,
                        onValueChange = { profileNicknameDraft = it; profileEditErrorMessage = null },
                        label = { Text("닉네임") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = profileIntroDraft,
                        onValueChange = { profileIntroDraft = it; profileEditErrorMessage = null },
                        label = { Text("한줄 소개") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (profileEditErrorMessage != null) {
                        Text(profileEditErrorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                         if (isProfileEditSaving) return@Button
                         val nicknameTrimmed = profileNicknameDraft.trim()
                         val introTrimmed = profileIntroDraft.trim()
                         if (nicknameTrimmed.length !in 2..20) { profileEditErrorMessage = "닉네임은 2~20자여야 합니다."; return@Button }
                         if (introTrimmed.length > 30) { profileEditErrorMessage = "한줄 소개는 최대 30자까지 가능해요."; return@Button }
 
                         isProfileEditSaving = true
                         scope.launch {
                             try {
                                 userMe = withContext(Dispatchers.IO) {
                                     BackendUserApi.updateProfile(
                                         nickname = nicknameTrimmed,
                                         intro = introTrimmed,
                                     )
                                 }
                                 isProfileEditDialogOpen = false
                             } catch (e: BackendApiException) {
                                 profileEditErrorMessage =
                                     if (e.statusCode == 409) "중복된 닉네임입니다."
                                     else e.message
                             } catch (e: Exception) {
                                 profileEditErrorMessage = e.message
                             } finally {
                                 isProfileEditSaving = false
                            }
                        }
                    },
                    enabled = !isProfileEditSaving
                ) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { isProfileEditDialogOpen = false }) { Text("취소") }
            }
        )
    }

    // 3. 로그아웃 다이얼로그 (기존 유지)
    if (isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { isLogoutDialogOpen = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃 하시겠어요?") },
            confirmButton = {
                Button(
                    onClick = {
                        isLogoutDialogOpen = false
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken(webClientId).build()
                        GoogleSignIn.getClient(context, gso).signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("로그아웃") }
            },
            dismissButton = { TextButton(onClick = { isLogoutDialogOpen = false }) { Text("취소") } }
        )
    }

    // 4. 재로그인 다이얼로그 (기존 유지)
    if (isReLoginDialogOpen) {
        AlertDialog(
            onDismissRequest = { isReLoginDialogOpen = false; onLogout() },
            title = { Text("세션 만료") },
            text = { Text("로그인이 만료되었습니다. 다시 로그인해주세요.") },
            confirmButton = { Button(onClick = { isReLoginDialogOpen = false; onLogout() }) { Text("확인") } }
        )
    }
}

// --- Helper Components ---

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        },
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// Modifier 확장 함수 (크기 조절용)
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)
