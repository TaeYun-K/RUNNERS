package com.runners.app.network

import com.runners.app.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class GoogleLoginResult(
    val userId: Long,
    val email: String?,
    val name: String?,
    val nickname: String?,
    val picture: String?,
    val accessToken: String,
    val isNewUser: Boolean,
)

object BackendAuthApi {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun googleLogin(idToken: String): GoogleLoginResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/auth/google"

        val bodyJson = JSONObject()
            .put("idToken", idToken)
            .toString()

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Backend login failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            fun optNullableString(key: String): String? =
                json.optString(key).takeIf { it.isNotBlank() }

            return GoogleLoginResult(
                userId = json.getLong("userId"),
                email = optNullableString("email"),
                name = optNullableString("name"),
                nickname = optNullableString("nickname"),
                picture = optNullableString("picture"),
                accessToken = json.getString("accessToken"),
                isNewUser = json.optBoolean("newUser", json.optBoolean("isNewUser", false)),
            )
        }
    }

    fun refreshAccessToken(): String {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/auth/refresh"

        val request = Request.Builder()
            .url(url)
            .post(ByteArray(0).toRequestBody(null))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Backend token refresh failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return json.getString("accessToken")
        }
    }
}
