package com.runners.app.network

import com.runners.app.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class UserMeResult(
    val userId: Long,
    val email: String,
    val name: String?,
    val nickname: String?,
    val picture: String?,
    val role: String?,
    val totalDistanceKm: Double?,
)

object BackendUserApi {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

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
            fun optNullableDouble(key: String): Double? =
                json.optDouble(key, Double.NaN).takeIf { !it.isNaN() }

            return UserMeResult(
                userId = json.getLong("userId"),
                email = json.getString("email"),
                name = optNullableString("name"),
                nickname = optNullableString("nickname"),
                picture = optNullableString("picture"),
                role = optNullableString("role"),
                totalDistanceKm = optNullableDouble("totalDistanceKm"),
            )
        }
    }

    fun updateNickname(nickname: String): UserMeResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/users/me/nickname"

        val bodyJson = JSONObject()
            .put("nickname", nickname)
            .toString()

        val request = Request.Builder()
            .url(url)
            .patch(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Update nickname failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            fun optNullableString(key: String): String? =
                json.optString(key).takeIf { it.isNotBlank() }
            fun optNullableDouble(key: String): Double? =
                json.optDouble(key, Double.NaN).takeIf { !it.isNaN() }

            return UserMeResult(
                userId = json.getLong("userId"),
                email = json.getString("email"),
                name = optNullableString("name"),
                nickname = optNullableString("nickname"),
                picture = optNullableString("picture"),
                role = optNullableString("role"),
                totalDistanceKm = optNullableDouble("totalDistanceKm"),
            )
        }
    }

    fun updateTotalDistanceKm(totalDistanceKm: Double): UserMeResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/users/me/total-distance"

        val bodyJson = JSONObject()
            .put("totalDistanceKm", totalDistanceKm)
            .toString()

        val request = Request.Builder()
            .url(url)
            .patch(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Update total distance failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            fun optNullableString(key: String): String? =
                json.optString(key).takeIf { it.isNotBlank() }
            fun optNullableDouble(key: String): Double? =
                json.optDouble(key, Double.NaN).takeIf { !it.isNaN() }

            return UserMeResult(
                userId = json.getLong("userId"),
                email = json.getString("email"),
                name = optNullableString("name"),
                nickname = optNullableString("nickname"),
                picture = optNullableString("picture"),
                role = optNullableString("role"),
                totalDistanceKm = optNullableDouble("totalDistanceKm"),
            )
        }
    }

    fun presignProfileImageUpload(file: PresignCommunityImageUploadFileRequest): PresignCommunityImageUploadResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/users/me/profile-image/presign"

        val filesArray = JSONArray().apply {
            put(
                JSONObject()
                    .put("fileName", file.fileName)
                    .put("contentType", file.contentType)
                    .put("contentLength", file.contentLength)
            )
        }

        val bodyJson = JSONObject()
            .put("files", filesArray)
            .toString()

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Presign profile image upload failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            val itemsJson = json.optJSONArray("items") ?: JSONArray()
            val items = ArrayList<PresignedCommunityUploadItemResult>(itemsJson.length())
            for (i in 0 until itemsJson.length()) {
                val item = itemsJson.getJSONObject(i)
                items.add(
                    PresignedCommunityUploadItemResult(
                        key = item.getString("key"),
                        uploadUrl = item.getString("uploadUrl"),
                        fileUrl = item.optString("fileUrl"),
                        contentType = item.optString("contentType"),
                    )
                )
            }

            return PresignCommunityImageUploadResult(
                items = items,
                expiresAt = json.optString("expiresAt").takeIf { it.isNotBlank() && it != "null" },
            )
        }
    }

    fun commitProfileImage(key: String): UserMeResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/users/me/profile-image/commit"

        val bodyJson = JSONObject()
            .put("key", key)
            .toString()

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Commit profile image failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            fun optNullableString(key: String): String? =
                json.optString(key).takeIf { it.isNotBlank() }
            fun optNullableDouble(key: String): Double? =
                json.optDouble(key, Double.NaN).takeIf { !it.isNaN() }

            return UserMeResult(
                userId = json.getLong("userId"),
                email = json.getString("email"),
                name = optNullableString("name"),
                nickname = optNullableString("nickname"),
                picture = optNullableString("picture"),
                role = optNullableString("role"),
                totalDistanceKm = optNullableDouble("totalDistanceKm"),
            )
        }
    }
}
