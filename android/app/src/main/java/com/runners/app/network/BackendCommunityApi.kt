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
    val thumbnailUrl: String?,
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
    val updatedAt: String? = null,
)

data class PresignCommunityImageUploadFileRequest(
    val fileName: String?,
    val contentType: String,
    val contentLength: Long,
)

data class PresignedCommunityUploadItemResult(
    val key: String,
    val uploadUrl: String,
    val fileUrl: String,
    val contentType: String,
)

data class PresignCommunityImageUploadResult(
    val items: List<PresignedCommunityUploadItemResult>,
    val expiresAt: String?,
)

data class CommunityPostDetailResult(
    val postId: Long,
    val authorId: Long,
    val authorName: String?,
    val authorPicture: String?,
    val authorTotalDistanceKm: Double?,
    val title: String,
    val content: String,
    val imageKeys: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val viewCount: Int,
    val recommendCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String?,
)

data class CommunityPostRecommendResult(
    val postId: Long,
    val recommended: Boolean,
    val recommendCount: Int,
)

data class CommunityCommentResult(
    val commentId: Long,
    val postId: Long,
    val authorId: Long,
    val authorName: String?,
    val authorPicture: String?,
    val authorTotalDistanceKm: Double?,
    val parentId: Long?,
    val content: String,
    val recommendCount: Int,
    val createdAt: String,
    val updatedAt: String?,
)

data class CommunityCommentRecommendResult(
    val postId: Long,
    val commentId: Long,
    val recommended: Boolean,
    val recommendCount: Int,
)

data class CommunityCommentMutationResult(
    val comment: CommunityCommentResult,
    val commentCount: Int,
)

data class DeleteCommunityCommentResult(
    val commentId: Long,
    val postId: Long,
    val commentCount: Int,
    val deletedAt: String?,
)

data class CommunityCommentCursorListResult(
    val comments: List<CommunityCommentResult>,
    val nextCursor: String?,
)

object BackendCommunityApi {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun parseCommentAuthorTotalDistanceKm(json: JSONObject): Double? =
        json.optDouble("authorTotalDistanceKm", Double.NaN).takeIf { !it.isNaN() }

    private fun parseCommunityComment(json: JSONObject): CommunityCommentResult =
        CommunityCommentResult(
            commentId = json.getLong("commentId"),
            postId = json.getLong("postId"),
            authorId = json.getLong("authorId"),
            authorName = json.optString("authorName").takeIf { it.isNotBlank() },
            authorPicture = json.optString("authorPicture").takeIf { it.isNotBlank() },
            authorTotalDistanceKm = parseCommentAuthorTotalDistanceKm(json),
            parentId = json.optLong("parentId", -1L).takeIf { it > 0 },
            content = json.optString("content"),
            recommendCount = json.optInt("recommendCount", 0),
            createdAt = json.optString("createdAt"),
            updatedAt = json.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" },
        )

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
                    thumbnailUrl = item.optString("thumbnailUrl").takeIf { it.isNotBlank() && it != "null" },
                    viewCount = item.optInt("viewCount", 0),
                    recommendCount = item.optInt("recommendCount", 0),
                    commentCount = item.optInt("commentCount", 0),
                    createdAt = item.optString("createdAt"),
                )
            )
        }
        return result
    }

    fun presignCommunityPostImageUploads(files: List<PresignCommunityImageUploadFileRequest>): PresignCommunityImageUploadResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/uploads/presign"

        val filesArray = JSONArray().apply {
            files.forEach { file ->
                put(
                    JSONObject()
                        .put("fileName", file.fileName)
                        .put("contentType", file.contentType)
                        .put("contentLength", file.contentLength)
                )
            }
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
                throw IllegalStateException("Presign community upload failed: HTTP ${response.code} ${responseBody.take(300)}")
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

    fun createPost(title: String, content: String, imageKeys: List<String>? = null): CreateCommunityPostResult {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts"

        val bodyJson = JSONObject()
            .put("title", title)
            .put("content", content)
            .apply {
                if (!imageKeys.isNullOrEmpty()) {
                    put("imageKeys", JSONArray(imageKeys))
                }
            }
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

            val imageUrls =
                json.optJSONArray("imageUrls")?.let { array ->
                    List(array.length()) { idx -> array.optString(idx) }.filter { it.isNotBlank() }
                } ?: emptyList()

            val imageKeys =
                json.optJSONArray("imageKeys")?.let { array ->
                    List(array.length()) { idx -> array.optString(idx) }.filter { it.isNotBlank() }
                } ?: emptyList()

            return CommunityPostDetailResult(
                postId = json.getLong("postId"),
                authorId = json.getLong("authorId"),
                authorName = json.optString("authorName").takeIf { it.isNotBlank() },
                authorPicture = json.optString("authorPicture").takeIf { it.isNotBlank() },
                authorTotalDistanceKm = authorTotalDistanceKm,
                title = json.getString("title"),
                content = json.getString("content"),
                imageKeys = imageKeys,
                imageUrls = imageUrls,
                viewCount = json.optInt("viewCount", 0),
                recommendCount = json.optInt("recommendCount", 0),
                commentCount = json.optInt("commentCount", 0),
                createdAt = json.optString("createdAt"),
                updatedAt = json.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" },
            )
        }
    }

    fun createComment(postId: Long, content: String, parentId: Long? = null): CommunityCommentMutationResult {
        require(postId > 0) { "postId must be positive" }
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/comments"

        val bodyJson = JSONObject()
            .put("content", content)
            .apply {
                if (parentId != null) put("parentId", parentId)
            }
            .toString()

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Create community comment failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityCommentMutationResult(
                comment = parseCommunityComment(json.getJSONObject("comment")),
                commentCount = json.optInt("commentCount", 0),
            )
        }
    }

    fun updatePost(postId: Long, title: String, content: String, imageKeys: List<String>? = null): CreateCommunityPostResult {
        require(postId > 0) { "postId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId"

        val bodyJson = JSONObject()
            .put("title", title)
            .put("content", content)
            .apply {
                if (imageKeys != null) {
                    put("imageKeys", JSONArray(imageKeys))
                }
            }
            .toString()

        val request = Request.Builder()
            .url(url)
            .put(bodyJson.toRequestBody(jsonMediaType))
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Update community post failed: HTTP ${response.code} ${responseBody.take(300)}"
                )
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
                updatedAt = json.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" }, // 추가
            )
        }
    }

    fun deletePost(postId: Long) {
        require(postId > 0) { "postId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId"

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Delete community post failed: HTTP ${response.code} ${responseBody.take(300)}")
            }
        }
    }

    fun recommendPost(postId: Long): CommunityPostRecommendResult {
        require(postId > 0) { "postId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/recommend"
        val requestBody = "{}".toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Recommend community post failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityPostRecommendResult(
                postId = json.getLong("postId"),
                recommended = json.optBoolean("recommended", true),
                recommendCount = json.optInt("recommendCount", 0),
            )
        }
    }

    fun getPostRecommendStatus(postId: Long): CommunityPostRecommendResult {
        require(postId > 0) { "postId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/recommend"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Fetch community post recommend status failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityPostRecommendResult(
                postId = json.getLong("postId"),
                recommended = json.optBoolean("recommended", false),
                recommendCount = json.optInt("recommendCount", 0),
            )
        }
    }

    fun unrecommendPost(postId: Long): CommunityPostRecommendResult {
        require(postId > 0) { "postId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/recommend"

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Unrecommend community post failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityPostRecommendResult(
                postId = json.getLong("postId"),
                recommended = json.optBoolean("recommended", false),
                recommendCount = json.optInt("recommendCount", 0),
            )
        }
    }

    fun recommendComment(postId: Long, commentId: Long): CommunityCommentRecommendResult {
        require(postId > 0) { "postId must be positive" }
        require(commentId > 0) { "commentId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/comments/$commentId/recommend"
        val requestBody = "{}".toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Recommend community comment failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityCommentRecommendResult(
                postId = json.getLong("postId"),
                commentId = json.getLong("commentId"),
                recommended = json.optBoolean("recommended", true),
                recommendCount = json.optInt("recommendCount", 0),
            )
        }
    }

    fun getCommentRecommendStatus(postId: Long, commentId: Long): CommunityCommentRecommendResult {
        require(postId > 0) { "postId must be positive" }
        require(commentId > 0) { "commentId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/comments/$commentId/recommend"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Fetch community comment recommend status failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityCommentRecommendResult(
                postId = json.getLong("postId"),
                commentId = json.getLong("commentId"),
                recommended = json.optBoolean("recommended", false),
                recommendCount = json.optInt("recommendCount", 0),
            )
        }
    }

    fun unrecommendComment(postId: Long, commentId: Long): CommunityCommentRecommendResult {
        require(postId > 0) { "postId must be positive" }
        require(commentId > 0) { "commentId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/comments/$commentId/recommend"

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Unrecommend community comment failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityCommentRecommendResult(
                postId = json.getLong("postId"),
                commentId = json.getLong("commentId"),
                recommended = json.optBoolean("recommended", false),
                recommendCount = json.optInt("recommendCount", 0),
            )
        }
    }

    fun deleteComment(postId: Long, commentId: Long): DeleteCommunityCommentResult {
        require(postId > 0) { "postId must be positive" }
        require(commentId > 0) { "commentId must be positive" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/comments/$commentId"

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Delete community comment failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return DeleteCommunityCommentResult(
                commentId = json.getLong("commentId"),
                postId = json.getLong("postId"),
                commentCount = json.optInt("commentCount", 0),
                deletedAt = json.optString("deletedAt").takeIf { it.isNotBlank() && it != "null" },
            )
        }
    }

    fun updateComment(postId: Long, commentId: Long, content: String): CommunityCommentMutationResult {
        require(postId > 0) { "postId must be positive" }
        require(commentId > 0) { "commentId must be positive" }
        require(content.isNotBlank()) { "content must not be blank" }

        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/comments/$commentId"

        val bodyJson =
            JSONObject()
                .put("content", content)
                .put("parentId", JSONObject.NULL)

        val requestBody = bodyJson.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Update community comment failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            return CommunityCommentMutationResult(
                comment = parseCommunityComment(json.getJSONObject("comment")),
                commentCount = json.optInt("commentCount", 0),
            )
        }
    }

    fun listComments(postId: Long, cursor: String?, size: Int = 20): CommunityCommentCursorListResult {
        require(postId > 0) { "postId must be positive" }

        val baseUrl = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/community/posts/$postId/comments"
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
                throw IllegalStateException("Fetch community comments failed: HTTP ${response.code} ${responseBody.take(300)}")
            }

            val json = JSONObject(responseBody)
            val comments =
                run {
                    val array = json.optJSONArray("comments") ?: JSONArray()
                    val result = ArrayList<CommunityCommentResult>(array.length())
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        result.add(
                            CommunityCommentResult(
                                commentId = item.getLong("commentId"),
                                postId = item.getLong("postId"),
                                authorId = item.getLong("authorId"),
                                authorName = item.optString("authorName").takeIf { it.isNotBlank() },
                                authorPicture = item.optString("authorPicture").takeIf { it.isNotBlank() },
                                authorTotalDistanceKm = parseCommentAuthorTotalDistanceKm(item),
                                parentId = item.optLong("parentId", -1L).takeIf { it > 0 },
                                content = item.optString("content"),
                                recommendCount = item.optInt("recommendCount", 0),
                                createdAt = item.optString("createdAt"),
                                updatedAt = item.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" },
                            )
                        )
                    }
                    result
                }
            val nextCursor = json.optString("nextCursor").takeIf { it.isNotBlank() && it != "null" }
            return CommunityCommentCursorListResult(comments = comments, nextCursor = nextCursor)
        }
    }
}
