package com.runners.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.runners.app.R

/**
 * FCM 메시지 수신 처리.
 * 댓글 알림 등 푸시 알림을 수신하고 시스템 알림으로 표시하며,
 * 알림 클릭 시 게시글 상세로 이동할 수 있도록 PendingIntent 설정.
 */
class RunnersFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: getString(R.string.notification_default_title)
        val body = remoteMessage.notification?.body ?: remoteMessage.notification?.body.orEmpty()

        val postId = remoteMessage.data[NotificationConstants.DATA_KEY_POST_ID]?.toLongOrNull()
        val commentId = remoteMessage.data[NotificationConstants.DATA_KEY_COMMENT_ID]?.toLongOrNull()

        createNotificationChannel()
        showNotification(
            title = title,
            body = body,
            postId = postId,
            commentId = commentId,
            notificationId = remoteMessage.data[NotificationConstants.DATA_KEY_NOTIFICATION_ID]?.toIntOrNull() ?: System.currentTimeMillis().toInt().and(Int.MAX_VALUE),
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        NotificationTokenManager.onNewToken(this, token)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationConstants.CHANNEL_ID,
                NotificationConstants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "커뮤니티 댓글 알림"
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        postId: Long?,
        commentId: Long?,
        notificationId: Int,
    ) {
        val contentIntent = NotificationHandler.createContentIntent(this, postId, commentId)

        val notification = NotificationCompat.Builder(this, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
}
