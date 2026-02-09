package com.runners.app.community.recommend.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 댓글 추천 이벤트
 * 알림 처리에 필요한 ID 값만 포함합니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentRecommendedEvent(
        Long commentId,
        Long postId,
        Long commentAuthorId,
        Long recommenderId
) {
}
