package com.runners.app.mypage

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.runners.app.R
import com.runners.app.mypage.components.HealthConnectSection
import com.runners.app.network.BackendUserApi
import com.runners.app.network.GoogleLoginResult
import com.runners.app.network.UserMeResult
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
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userMe by remember { mutableStateOf<UserMeResult?>(null) }

    suspend fun refreshUser() {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        try {
            userMe = withContext(Dispatchers.IO) { BackendUserApi.getMe() }
        } catch (e: Exception) {
            errorMessage = e.message ?: "내 정보 불러오기에 실패했어요"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshUser()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("마이페이지", style = MaterialTheme.typography.headlineSmall)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                    Text(
                        text = userMe?.name ?: session.name ?: "RUNNERS",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = userMe?.email ?: session.email ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 4.dp))
                        Text("내 정보 불러오는 중…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { scope.launch { refreshUser() } }) {
                        Text("다시 시도")
                    }
                }
            }
        }

        Text("개인정보", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("사용자 ID") },
                    supportingContent = { Text("${userMe?.userId ?: session.userId}") },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("이메일") },
                    supportingContent = { Text(userMe?.email ?: session.email ?: "-") },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("이름") },
                    supportingContent = { Text(userMe?.name ?: session.name ?: "-") },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("권한") },
                    supportingContent = { Text(userMe?.role ?: "-") },
                )
            }
        }

        Text("앱 설정", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Settings, contentDescription = null)
                    Text("헬스 커넥트", style = MaterialTheme.typography.titleSmall)
                }
                HorizontalDivider()
                HealthConnectSection()
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { isLogoutDialogOpen = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text("로그아웃", color = MaterialTheme.colorScheme.onError)
        }
    }

    if (isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { isLogoutDialogOpen = false },
            title = { Text("로그아웃") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("로그아웃", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                Button(onClick = { isLogoutDialogOpen = false }) {
                    Text("취소")
                }
            },
        )
    }
}
