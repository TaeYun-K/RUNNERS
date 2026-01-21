package com.runners.app.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.runners.app.R
import com.runners.app.ui.theme.Blue40
import com.runners.app.ui.theme.Blue60

@Composable
fun LoginScreen(
    onIdToken: (String) -> Unit,
    onKakaoLogin: () -> Unit = {},
    onNaverLogin: () -> Unit = {},
    isLoading: Boolean = false,
    backendErrorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val signInClient = remember(webClientId) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (data == null) {
            errorMessage = "로그인이 취소됐어요."
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                errorMessage = "로그인 토큰을 가져오지 못했어요. 잠시 후 다시 시도해주세요."
            } else {
                errorMessage = null
                onIdToken(idToken)
            }
        } catch (exception: ApiException) {
            errorMessage = "구글 로그인에 실패했어요. (${exception.statusCode})"
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 로고 아이콘
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Blue40, Blue60)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "RUNNERS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "누적 km로 성장하는 러닝 커뮤니티",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = spacedBy(12.dp),
                ) {
                    // Google 로그인 버튼 - Primary 스타일
                    Button(
                        onClick = { launcher.launch(signInClient.signInIntent) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp,
                        ),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("로그인 중...", fontWeight = FontWeight.SemiBold)
                        } else {
                            ProviderMark(
                                letter = "G",
                                backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Google로 시작하기", fontWeight = FontWeight.SemiBold)
                        }
                    }

//                    // 카카오 로그인 버튼
//                    Button(
//                        onClick = onKakaoLogin,
//                        enabled = false,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFFEE500),
//                            contentColor = Color(0xFF191919),
//                            disabledContainerColor = Color(0xFFFEE500).copy(alpha = 0.4f),
//                            disabledContentColor = Color(0xFF191919).copy(alpha = 0.4f),
//                        ),
//                    ) {
//                        ProviderMark(
//                            letter = "K",
//                            backgroundColor = Color(0xFF191919).copy(alpha = 0.1f),
//                            contentColor = Color(0xFF191919).copy(alpha = 0.6f),
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text("카카오로 시작하기", fontWeight = FontWeight.SemiBold)
//                        Spacer(modifier = Modifier.weight(1f))
//                        Text(
//                            "준비 중",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = Color(0xFF191919).copy(alpha = 0.5f),
//                        )
//                    }

//                    // 네이버 로그인 버튼
//                    Button(
//                        onClick = onNaverLogin,
//                        enabled = false,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF03C75A),
//                            contentColor = Color.White,
//                            disabledContainerColor = Color(0xFF03C75A).copy(alpha = 0.4f),
//                            disabledContentColor = Color.White.copy(alpha = 0.5f),
//                        ),
//                    ) {
//                        ProviderMark(
//                            letter = "N",
//                            backgroundColor = Color.White.copy(alpha = 0.2f),
//                            contentColor = Color.White.copy(alpha = 0.7f),
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text("네이버로 시작하기", fontWeight = FontWeight.SemiBold)
//                        Spacer(modifier = Modifier.weight(1f))
//                        Text(
//                            "준비 중",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = Color.White.copy(alpha = 0.6f),
//                        )
//                    }
                }

                val shownError = backendErrorMessage ?: errorMessage
                if (!shownError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = shownError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "로그인하면 서비스 이용약관 및\n개인정보 처리방침에 동의한 것으로 간주돼요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ProviderMark(
    letter: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}
