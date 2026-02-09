package com.runners.app.notification.entity;

public enum NotificationType {
    COMMENT_ON_MY_POST,              // 내가 쓴 글에 댓글 달림
    COMMENT_ON_MY_COMMENTED_POST,    // 내가 댓글단 글에 댓글 달림 (일반 댓글)
    REPLY_TO_MY_COMMENT,             // 내 댓글에 대댓글 달림 (대댓글만)
    RECOMMEND_ON_MY_POST,            // 내가 쓴 글에 추천 눌림
    RECOMMEND_ON_MY_COMMENT          // 내가 쓴 댓글에 추천 눌림
}
