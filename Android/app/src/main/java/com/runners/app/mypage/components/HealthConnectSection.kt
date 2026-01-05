package com.runners.app.mypage.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.runners.app.healthconnect.HealthConnectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HealthConnectSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf<String?>(null) }
    var isWorking by remember { mutableStateOf(false) }
    var missingPermissions by remember { mutableStateOf<Set<String>>(emptySet()) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
        onResult = { granted: Set<String> ->
            scope.launch {
                isWorking = true
                message = null
                try {
                    val missing = HealthConnectRepository.requiredPermissions - granted
                    missingPermissions = missing
                    if (missing.isNotEmpty()) {
                        val labels = missing.map { HealthConnectRepository.requiredPermissionLabels[it] ?: it }
                        message =
                            "Health Connect 권한이 필요해요: ${labels.joinToString(", ")}. " +
                                "권한 화면이 뜨지 않으면 아래에서 Health Connect 설정을 열어 확인해주세요."
                        return@launch
                    }

                    val client = HealthConnectRepository.getClient(context)
                    val runningCount = withContext(Dispatchers.IO) {
                        HealthConnectRepository.readRunningSessionCount(client)
                    }
                    message = "러닝 기록 ${runningCount}개를 읽었어요."
                } catch (e: Exception) {
                    message = e.message ?: "Health Connect 처리 중 오류가 발생했어요."
                } finally {
                    isWorking = false
                }
            }
        }
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "러닝 거리/기록을 가져오려면 Health Connect 권한이 필요해요.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "버튼을 누르면 Health Connect 권한 화면이 열리고, 여기서 운동 세션/거리 접근을 허용해야 해요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = {
                if (isWorking) return@Button

                when (HealthConnectRepository.getSdkStatus(context)) {
                    HealthConnectClient.SDK_AVAILABLE -> {
                        scope.launch {
                            isWorking = true
                            message = null
                            try {
                                val client = HealthConnectRepository.getClient(context)
                                val hasAll = HealthConnectRepository.hasAllPermissions(client)
                                if (hasAll) {
                                    val runningCount = withContext(Dispatchers.IO) {
                                        HealthConnectRepository.readRunningSessionCount(client)
                                    }
                                    missingPermissions = emptySet()
                                    message = "이미 권한이 있어요. 러닝 기록 ${runningCount}개를 읽었어요."
                                } else {
                                    missingPermissions = HealthConnectRepository.requiredPermissions
                                    permissionsLauncher.launch(HealthConnectRepository.requiredPermissions)
                                }
                            } catch (e: Exception) {
                                message = e.message ?: "Health Connect 처리 중 오류가 발생했어요."
                            } finally {
                                isWorking = false
                            }
                        }
                    }
                    HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata"))
                        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"))
                        runCatching { context.startActivity(market) }.getOrElse { context.startActivity(web) }
                    }
                    else -> {
                        message = "이 기기에서 Health Connect를 사용할 수 없어요."
                    }
                }
            },
            enabled = !isWorking,
        ) {
            Text(if (isWorking) "처리 중..." else "Health Connect 연결/권한 요청")
        }

        if (missingPermissions.isNotEmpty()) {
            Button(
                onClick = { permissionsLauncher.launch(HealthConnectRepository.requiredPermissions) },
                enabled = !isWorking,
            ) {
                Text("권한 다시 요청")
            }

            Button(
                onClick = {
                    runCatching {
                        context.startActivity(HealthConnectClient.getHealthConnectManageDataIntent(context))
                    }
                },
                enabled = !isWorking,
            ) {
                Text("Health Connect 설정 열기")
            }
        }

        if (message != null) {
            Text(message!!, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
