package com.runners.app.notification

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notification_token")

/** 등록된 FCM 토큰 저장 (로그아웃 시 DELETE API 호출에 사용) */
object NotificationTokenStore {

    private val tokenKey = stringPreferencesKey("fcm_token")

    suspend fun getToken(context: Context): String? {
        return context.notificationDataStore.data.map { it[tokenKey] }.first()
    }

    suspend fun setToken(context: Context, token: String) {
        context.notificationDataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear(context: Context) {
        context.notificationDataStore.edit { it.remove(tokenKey) }
    }
}
