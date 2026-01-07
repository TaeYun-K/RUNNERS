package com.runners.app.network

import com.runners.app.BuildConfig
import okhttp3.Request
import org.json.JSONObject

data class UserMeResult(
    val userId: Long,
    val email: String,
    val name: String?,
    val picture: String?,
    val role: String?,
)

object BackendUserApi {
    fun getMe(): UserMeResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/users/me"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Fetch user failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            fun optNullableString(key: String): String? =
                json.optString(key).takeIf { it.isNotBlank() }

            return UserMeResult(
                userId = json.getLong("userId"),
                email = json.getString("email"),
                name = optNullableString("name"),
                picture = optNullableString("picture"),
                role = optNullableString("role"),
            )
        }
    }
}

