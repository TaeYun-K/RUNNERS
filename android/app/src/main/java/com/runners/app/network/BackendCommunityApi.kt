package com.runners.app.network

import com.runners.app.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class CommunityPostSummaryResult(
    val postId: Long,
    val authorId: Long,
    val authorName: String?,
    val authorTotalDistanceKm: Double?,
    val title: String,
    val contentPreview: String?,
    val viewCount: Int,
    val recommendCount: Int,
    val commentCount: Int,
    val createdAt: String,
)

data class CommunityPostCursorListResult(
    val posts: List<CommunityPostSummaryResult>,
    val nextCursor: String?,
)

data class CreateCommunityPostResult(
    val postId: Long,
    val authorId: Long,
    val title: String,
    val content: String,
    val viewCount: Int,
    val recommendCount: Int,
    val commentCount: Int,
    val createdAt: String,
)

data class CommunityPostDetailResult(
    val postId: Long,
    val authorId: Long,
    val authorName: String?,
    val authorPicture: String?,
    val authorTotalDistanceKm: Double?,
    val title: String,
    val content: String,
    val viewCount: Int,
    val recommendCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String?,
)

object BackendCommunityApi {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun listPosts(cursor: String?, size: Int = 20): CommunityPostCursorListResult {
        val baseUrl = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts"
        val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
            .addQueryParameter("size", size.coerceIn(1, 50).toString())

        if (!cursor.isNullOrBlank()) {
            httpUrlBuilder.addQueryParameter("cursor", cursor)
        }

        val request = Request.Builder()
            .url(httpUrlBuilder.build())
            .get()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Fetch community posts failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            val posts = parsePosts(json.optJSONArray("posts") ?: JSONArray())
            val nextCursor = json.optString("nextCursor")
                .takeIf { it.isNotBlank() && it != "null" }
            return CommunityPostCursorListResult(posts = posts, nextCursor = nextCursor)
        }
    }

    private fun parsePosts(array: JSONArray): List<CommunityPostSummaryResult> {
        val result = ArrayList<CommunityPostSummaryResult>(array.length())
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val authorTotalDistanceKm =
                item.optDouble("authorTotalDistanceKm", Double.NaN)
                    .takeIf { !it.isNaN() }
            result.add(
                CommunityPostSummaryResult(
                    postId = item.getLong("postId"),
                    authorId = item.getLong("authorId"),
                    authorName = item.optString("authorName").takeIf { it.isNotBlank() },
                    authorTotalDistanceKm = authorTotalDistanceKm,
                    title = item.getString("title"),
                    contentPreview = item.optString("contentPreview").takeIf { it.isNotBlank() },
                    viewCount = item.optInt("viewCount", 0),
                    recommendCount = item.optInt("recommendCount", 0),
                    commentCount = item.optInt("commentCount", 0),
                    createdAt = item.optString("createdAt"),
                )
            )
        }
        return result
    }

    fun createPost(title: String, content: String): CreateCommunityPostResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts"

        val bodyJson = JSONObject()
            .put("title", title)
            .put("content", content)
            .toString()

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Create community post failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CreateCommunityPostResult(
                postId = json.getLong("postId"),
                authorId = json.getLong("authorId"),
                title = json.getString("title"),
                content = json.getString("content"),
                viewCount = json.optInt("viewCount", 0),
                recommendCount = json.optInt("recommendCount", 0),
                commentCount = json.optInt("commentCount", 0),
                createdAt = json.optString("createdAt"),
            )
        }
    }

    fun getPost(postId: Long): CommunityPostDetailResult {
        require(postId > 0) { "postId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Fetch community post failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            val authorTotalDistanceKm =
                json.optDouble("authorTotalDistanceKm", Double.NaN)
                    .takeIf { !it.isNaN() }
            return CommunityPostDetailResult(
                postId = json.getLong("postId"),
                authorId = json.getLong("authorId"),
                authorName = json.optString("authorName").takeIf { it.isNotBlank() },
                authorPicture = json.optString("authorPicture").takeIf { it.isNotBlank() },
                authorTotalDistanceKm = authorTotalDistanceKm,
                title = json.getString("title"),
                content = json.getString("content"),
                viewCount = json.optInt("viewCount", 0),
                recommendCount = json.optInt("recommendCount", 0),
                commentCount = json.optInt("commentCount", 0),
                createdAt = json.optString("createdAt"),
                updatedAt = json.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" },
            )
        }
    }
}
