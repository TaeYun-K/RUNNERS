package com.runners.app.network

import com.runners.app.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class CommunityPostSummaryResult(
    val postId: Long,
    val authorId: Long,
    val authorName: String?,
    val title: String,
    val viewCount: Int,
    val recommendCount: Int,
    val commentCount: Int,
    val createdAt: String,
)

data class CommunityPostCursorListResult(
    val posts: List<CommunityPostSummaryResult>,
    val nextCursor: String?,
)

object BackendCommunityApi {
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
            val nextCursor = json.optString("nextCursor").takeIf { it.isNotBlank() }
            return CommunityPostCursorListResult(posts = posts, nextCursor = nextCursor)
        }
    }

    private fun parsePosts(array: JSONArray): List<CommunityPostSummaryResult> {
        val result = ArrayList<CommunityPostSummaryResult>(array.length())
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            result.add(
                CommunityPostSummaryResult(
                    postId = item.getLong("postId"),
                    authorId = item.getLong("authorId"),
                    authorName = item.optString("authorName").takeIf { it.isNotBlank() },
                    title = item.getString("title"),
                    viewCount = item.optInt("viewCount", 0),
                    recommendCount = item.optInt("recommendCount", 0),
                    commentCount = item.optInt("commentCount", 0),
                    createdAt = item.optString("createdAt"),
                )
            )
        }
        return result
    }
}

