package com.runners.app.community.comment.event;

/**
 * 댓글 생성 이벤트
 * 불변 객체로 필요한 ID 값만 포함하여 LAZY 로딩 문제를 방지합니다.
 */
public record CommentCreatedEvent(
        Long commentId,
        Long postId,
        Long postAuthorId,
        Long commentAuthorId,
        Long parentCommentId,  // nullable, 대댓글인 경우 부모 댓글 ID
        Long parentCommentAuthorId  // nullable, 대댓글인 경우 부모 댓글 작성자 ID
) {
    /**
     * 대댓글 여부 확인
     * @return 부모 댓글이 있으면 true, 없으면 false
     */
    public boolean isReply() {
        return parentCommentId != null;
    }
}
