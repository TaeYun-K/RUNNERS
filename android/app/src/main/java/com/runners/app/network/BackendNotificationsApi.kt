package com.runners.app.network

import com.runners.app.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

enum class NotificationType {
    COMMENT_ON_MY_POST,
    COMMENT_ON_MY_COMMENTED_POST,
    REPLY_TO_MY_COMMENT,
    RECOMMEND_ON_MY_POST,
    RECOMMEND_ON_MY_COMMENT,
    UNKNOWN;

    companion object {
        fun from(raw: String?): NotificationType {
            if (raw.isNullOrBlank()) return UNKNOWN
            return entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) } ?: UNKNOWN
        }
    }
}

data class NotificationResult(
    val id: Long,
    val type: NotificationType,
    val relatedPostId: Long?,
    val relatedCommentId: Long?,
    val postTitlePreview: String?,
    val commentPreview: String?,
    val actorId: Long?,
    val actorName: String?,
    val actorPicture: String?,
    val isRead: Boolean,
    val createdAt: String,
    val readAt: String?,
)

data class NotificationCursorListResult(
    val notifications: List<NotificationResult>,
    val hasNext: Boolean,
    val nextCursor: String?,
)

object BackendNotificationsApi {

    fun listNotifications(cursor: String?, size: Int = 20): NotificationCursorListResult {
        val baseUrl = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/notifications"
        val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()

        cursor?.takeIf { it.isNotBlank() }?.let { httpUrlBuilder.addQueryParameter("cursor", it) }
        httpUrlBuilder.addQueryParameter("size", size.toString())

        val request = Request.Builder()
            .url(httpUrlBuilder.build())
            .get()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "List notifications failed: HTTP ${response.code} ${responseBody.take(300)}"
                )
            }

            val json = JSONObject(responseBody)
            val itemsJson = json.optJSONArray("notifications") ?: JSONArray()
            val notifications = buildList(itemsJson.length()) {
                for (i in 0 until itemsJson.length()) {
                    add(parseNotification(itemsJson.getJSONObject(i)))
                }
            }

            val hasNext = json.optBoolean("hasNext", false)
            val nextCursor =
                json.optString("nextCursor")
                    .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            return NotificationCursorListResult(
                notifications = notifications,
                hasNext = hasNext,
                nextCursor = nextCursor,
            )
        }
    }

    fun getUnreadCount(): Long {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/notifications/unread-count"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Get unread count failed: HTTP ${response.code} ${responseBody.take(300)}"
                )
            }
            val json = JSONObject(responseBody)
            return json.optLong("unreadCount", 0L).coerceAtLeast(0L)
        }
    }

    fun markAsRead(notificationId: Long) {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/notifications/$notificationId/read"
        val emptyBody = ByteArray(0).toRequestBody(null)
        val request = Request.Builder()
            .url(url)
            .put(emptyBody)
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val responseBody = response.body?.string().orEmpty()
                throw IllegalStateException(
                    "Mark as read failed: HTTP ${response.code} ${responseBody.take(300)}"
                )
            }
        }
    }

    fun markAllAsRead() {
        val url = "${BuildConfig.BACKEND_BASE_URL.trimEnd('/')}/api/notifications/read-all"
        val emptyBody = ByteArray(0).toRequestBody(null)
        val request = Request.Builder()
            .url(url)
            .put(emptyBody)
            .build()

        BackendHttpClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val responseBody = response.body?.string().orEmpty()
                throw IllegalStateException(
                    "Mark all as read failed: HTTP ${response.code} ${responseBody.take(300)}"
                )
            }
        }
    }

    private fun parseNotification(json: JSONObject): NotificationResult {
        val id = json.optLong("id", -1L)
        val type = NotificationType.from(json.optString("type"))

        fun optNullableLong(key: String): Long? {
            if (!json.has(key) || json.isNull(key)) return null
            return runCatching { json.getLong(key) }.getOrNull()
        }

        val isRead = json.optBoolean("isRead", json.optBoolean("read", false))
        return NotificationResult(
            id = id,
            type = type,
            relatedPostId = optNullableLong("relatedPostId"),
            relatedCommentId = optNullableLong("relatedCommentId"),
            postTitlePreview = json.optString("postTitlePreview")
                .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) },
            commentPreview = json.optString("commentPreview")
                .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) },
            actorId = optNullableLong("actorId"),
            actorName = json.optString("actorName").takeIf { it.isNotBlank() },
            actorPicture = json.optString("actorPicture").takeIf { it.isNotBlank() },
            isRead = isRead,
            createdAt = json.optString("createdAt").orEmpty(),
            readAt = json.optString("readAt").takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) },
        )
    }
}
