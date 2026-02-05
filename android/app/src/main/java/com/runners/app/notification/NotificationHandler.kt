package com.runners.app.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.runners.app.MainActivity

/**
 * 푸시 알림 클릭 시 네비게이션 처리.
 * 알림 탭 시 MainActivity를 열고 게시글 상세로 이동할 수 있도록 Intent 생성.
 */
object NotificationHandler {

    /**
     * 알림 클릭 시 실행할 PendingIntent 생성.
     * postId가 있으면 게시글 상세 화면으로 이동 (MainActivity에서 pendingNotificationPostId로 처리).
     *
     * @param context Application context
     * @param postId 관련 게시글 ID (nullable)
     * @param commentId 관련 댓글 ID (nullable, 현재는 게시글 상세로만 이동)
     */
    fun createContentIntent(
        context: Context,
        postId: Long?,
        commentId: Long?
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            postId?.let { putExtra(NotificationConstants.EXTRA_POST_ID, it) }
            commentId?.let { putExtra(NotificationConstants.EXTRA_COMMENT_ID, it) }
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }

        return PendingIntent.getActivity(context, 0, intent, flags)
    }
}
