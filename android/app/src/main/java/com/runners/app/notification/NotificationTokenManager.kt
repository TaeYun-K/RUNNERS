package com.runners.app.notification

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.runners.app.network.BackendNotificationApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * FCM 토큰 등록/관리.
 * 앱 시작·로그인 후 백엔드에 토큰 등록, 로그아웃 시 삭제.
 */
object NotificationTokenManager {

    private const val PLATFORM = "android"

    /**
     * FCM 새 토큰 수신 시 호출 (FirebaseMessagingService.onNewToken).
     * 로그인 상태면 백엔드에 재등록하고 저장.
     */
    fun onNewToken(context: Context, token: String) {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                BackendNotificationApi.registerToken(token, deviceId = null, platform = PLATFORM)
                NotificationTokenStore.setToken(context, token)
            }
        }
    }

    /**
     * 로그인 후 또는 앱 시작 시 호출.
     * FCM 토큰을 가져와 백엔드에 등록하고 저장.
     * @param scope 토큰 획득 후 API 호출에 사용할 코루틴 스코프
     */
    fun registerTokenIfNeeded(context: Context, scope: CoroutineScope) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            scope.launch(Dispatchers.IO) {
                runCatching {
                    BackendNotificationApi.registerToken(token, deviceId = null, platform = PLATFORM)
                    NotificationTokenStore.setToken(context, token)
                }
            }
        }
    }

    /**
     * 로그아웃 시 호출.
     * 저장된 토큰으로 DELETE API 호출 후 저장소 삭제.
     */
    suspend fun removeTokenOnLogout(context: Context) {
        withContext(Dispatchers.IO) {
            val token = NotificationTokenStore.getToken(context)
            if (!token.isNullOrBlank()) {
                runCatching { BackendNotificationApi.removeToken(token) }
            }
            NotificationTokenStore.clear(context)
        }
    }
}
