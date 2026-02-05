package com.runners.app.network

import com.runners.app.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * FCM 디바이스 토큰 등록/삭제 API.
 * 백엔드 DeviceTokenController와 연동 (POST /api/device-tokens, DELETE /api/device-tokens/{token}).
 */
object BackendNotificationApi {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * FCM 토큰 등록 또는 업데이트.
     * 인증된 사용자만 호출 가능.
     */
    fun registerToken(
        token: String,
        deviceId: String? = null,
        platform: String? = "android",
    ) {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/device-tokens"

        val bodyJson = JSONObject()
            .put("token", token)
        deviceId?.takeIf { it.isNotBlank() }?.let { bodyJson.put("deviceId", it) }
        platform?.takeIf { it.isNotBlank() }?.let { bodyJson.put("platform", it) }

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Device token registration failed: HTTP ${response.code} ${responseBody.take(300)}"
                )
            }
        }
    }

    /**
     * FCM 토큰 삭제 (로그아웃 시 호출).
     * token은 URL 경로에 포함되므로 인코딩 필요.
     */
    fun removeToken(token: String) {
        val encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8.name())
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/device-tokens/$encodedToken"

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 404) {
                val responseBody = response.body?.string().orEmpty()
                throw IllegalStateException(
                    "Device token removal failed: HTTP ${response.code} ${responseBody.take(300)}"
                )
            }
        }
    }
}
