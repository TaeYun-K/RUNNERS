package com.runners.app.notification

/**
 * FCM 알림 채널 및 데이터 키 상수
 */
object NotificationConstants {
    /** 알림 채널 ID (Android 8.0+) */
    const val CHANNEL_ID = "runners_comment_notifications"

    /** 알림 채널 표시 이름 */
    const val CHANNEL_NAME = "댓글 알림"

    /** FCM 데이터 페이로드 키 (백엔드 FcmService와 일치) */
    const val DATA_KEY_NOTIFICATION_ID = "notificationId"
    const val DATA_KEY_TYPE = "type"
    const val DATA_KEY_POST_ID = "postId"
    const val DATA_KEY_COMMENT_ID = "commentId"
    const val DATA_KEY_ACTOR_ID = "actorId"

    /** 알림 클릭 시 MainActivity로 전달하는 Intent Extra 키 */
    const val EXTRA_POST_ID = "com.runners.app.notification.EXTRA_POST_ID"
    const val EXTRA_COMMENT_ID = "com.runners.app.notification.EXTRA_COMMENT_ID"
}
