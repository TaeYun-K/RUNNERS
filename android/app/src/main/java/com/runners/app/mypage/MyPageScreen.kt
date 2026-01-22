package com.runners.app.mypage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Logout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.runners.app.R
import com.runners.app.mypage.components.HealthConnectSection
import com.runners.app.auth.AuthTokenStore
import com.runners.app.network.PresignCommunityImageUploadFileRequest
import com.runners.app.network.BackendUserApi
import com.runners.app.network.GoogleLoginResult
import com.runners.app.network.PresignedUploadClient
import com.runners.app.network.UserMeResult
import com.runners.app.settings.AppSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MyPageScreen(
    session: GoogleLoginResult,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.google_web_client_id)
    var isLogoutDialogOpen by remember { mutableStateOf(false) }
    var isReLoginDialogOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userMe by remember { mutableStateOf<UserMeResult?>(null) }
    var isNicknameDialogOpen by remember { mutableStateOf(false) }
    var nicknameDraft by remember { mutableStateOf("") }
    var nicknameErrorMessage by remember { mutableStateOf<String?>(null) }
    var isNicknameSaving by remember { mutableStateOf(false) }
    var isProfileImageUploading by remember { mutableStateOf(false) }

    val showTotalDistanceInCommunity =
        AppSettingsStore.showTotalDistanceInCommunityFlow(context)
            .collectAsStateWithLifecycle(initialValue = true)
            .value

    suspend fun refreshUser() {
        if (isLoading) return

        if (AuthTokenStore.peekRefreshToken().isNullOrBlank()) {
            errorMessage = null
            isReLoginDialogOpen = true
            return
        }

        isLoading = true
        errorMessage = null
        try {
            userMe = withContext(Dispatchers.IO) { BackendUserApi.getMe() }
        } catch (e: Exception) {
            if (AuthTokenStore.peekRefreshToken().isNullOrBlank()) {
                errorMessage = null
                isReLoginDialogOpen = true
            } else {
                errorMessage = e.message ?: "내 정보 불러오기에 실패했어요"
            }
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshUser()
    }

    val pickProfileImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            if (isLoading || isProfileImageUploading) return@rememberLauncherForActivityResult

            scope.launch {
                isProfileImageUploading = true
                errorMessage = null
                try {
                    val bytes =
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        } ?: throw IllegalStateException("이미지를 읽을 수 없어요")

                    val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val presigned =
                        withContext(Dispatchers.IO) {
                            BackendUserApi.presignProfileImageUpload(
                                PresignCommunityImageUploadFileRequest(
                                    fileName = uri.lastPathSegment ?: "profile",
                                    contentType = contentType,
                                    contentLength = bytes.size.toLong(),
                                )
                            )
                        }
                    val item = presigned.items.firstOrNull()
                        ?: throw IllegalStateException("업로드 URL을 발급받지 못했어요")

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 헤더
        Text(
            text = "마이페이지",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // 프로필 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 프로필 아바타
                    val profileImageUrl = userMe?.picture ?: session.picture
                    if (!profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = ((userMe?.name ?: session.name)?.firstOrNull() ?: "R").toString().uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userMe?.name ?: session.name ?: "RUNNERS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = userMe?.email ?: session.email ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            pickProfileImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isLoading && !isProfileImageUploading,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (isProfileImageUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Text("  업로드 중...")
                        } else {
                            Text("프로필 사진 변경")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (isLoading || isProfileImageUploading) return@OutlinedButton
                            scope.launch {
                                isProfileImageUploading = true
                                errorMessage = null
                                try {
                                    userMe = withContext(Dispatchers.IO) { BackendUserApi.deleteProfileImage() }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "프로필 사진 삭제에 실패했어요"
                                } finally {
                                    isProfileImageUploading = false
                                }
                            }
                        },
                        enabled = !isLoading && !isProfileImageUploading,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("프로필 사진 삭제")
                    }
                }

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "내 정보 불러오는 중...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                        onClick = { scope.launch { refreshUser() } },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("다시 시도")
                    }
                }
            }
        }

        // 개인정보 섹션
        SectionTitle(title = "개인정보")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column {
                InfoListItem(
                    label = "사용자 ID",
                    value = "${userMe?.userId ?: session.userId}",
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InfoListItem(
                    label = "이메일",
                    value = userMe?.email ?: session.email ?: "-",
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ListItem(
                    headlineContent = {
                        Text(
                            "닉네임",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    supportingContent = {
                        Text(
                            userMe?.nickname ?: session.nickname ?: "-",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    trailingContent = {
                        OutlinedButton(
                            onClick = {
                                nicknameDraft = (userMe?.nickname ?: session.nickname).orEmpty()
                                nicknameErrorMessage = null
                                isNicknameDialogOpen = true
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text("  변경")
                        }
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InfoListItem(
                    label = "권한",
                    value = userMe?.role ?: "-",
                )
            }
        }

        // 앱 설정 섹션
        SectionTitle(title = "앱 설정")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(Modifier.padding(4.dp)) {
                ListItem(
                    headlineContent = {
                        Text(
                            "커뮤니티 누적 거리 표시",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    supportingContent = {
                        Text(
                            "게시글에서 닉네임 옆에 누적 km 표시",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = showTotalDistanceInCommunity,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    AppSettingsStore.setShowTotalDistanceInCommunity(context, checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    },
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        "헬스 커넥트",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HealthConnectSection()
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 로그아웃 버튼
        OutlinedButton(
            onClick = { isLogoutDialogOpen = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                "  로그아웃",
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    // 로그아웃 다이얼로그
    if (isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { isLogoutDialogOpen = false },
            title = {
                Text(
                    "로그아웃",
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = { Text("정말 로그아웃 하시겠어요?") },
            confirmButton = {
                Button(
                    onClick = {
                        isLogoutDialogOpen = false
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestIdToken(webClientId)
                            .build()
                        GoogleSignIn.getClient(context, gso).signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("로그아웃")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isLogoutDialogOpen = false }) {
                    Text("취소")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }

    // 재로그인 다이얼로그
    if (isReLoginDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                isReLoginDialogOpen = false
                onLogout()
            },
            title = {
                Text(
                    "다시 로그인해주세요",
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = { Text("로그인이 만료되었어요. 로그인 화면으로 이동할게요.") },
            confirmButton = {
                Button(
                    onClick = {
                        isReLoginDialogOpen = false
                        onLogout()
                    },
                ) {
                    Text("확인")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }

    // 닉네임 변경 다이얼로그
    if (isNicknameDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                if (!isNicknameSaving) {
                    isNicknameDialogOpen = false
                }
            },
            title = {
                Text(
                    "닉네임 변경",
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nicknameDraft,
                        onValueChange = {
                            nicknameDraft = it
                            nicknameErrorMessage = null
                        },
                        singleLine = true,
                        label = { Text("닉네임") },
                        enabled = !isNicknameSaving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (nicknameErrorMessage != null) {
                        Text(
                            text = nicknameErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            "2~20자, 한글/영문/숫자/언더바(_)만 가능",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isNicknameSaving) return@Button
                        isNicknameSaving = true
                        nicknameErrorMessage = null
                        scope.launch {
                            try {
                                val updated = withContext(Dispatchers.IO) {
                                    BackendUserApi.updateNickname(nicknameDraft)
                                }
                                userMe = updated
                                isNicknameDialogOpen = false
                            } catch (e: Exception) {
                                nicknameErrorMessage = e.message ?: "닉네임 변경에 실패했어요"
                            } finally {
                                isNicknameSaving = false
                            }
                        }
                    },
                    enabled = !isNicknameSaving,
                ) {
                    Text(if (isNicknameSaving) "변경 중..." else "변경")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isNicknameDialogOpen = false },
                    enabled = !isNicknameSaving,
                ) {
                    Text("취소")
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = 8.dp),
    )
}

@Composable
private fun InfoListItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        supportingContent = {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        modifier = modifier,
    )
}
