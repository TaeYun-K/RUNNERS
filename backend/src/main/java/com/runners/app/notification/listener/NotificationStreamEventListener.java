package com.runners.app.notification.listener;

import com.runners.app.community.comment.event.CommentCreatedEvent;
import com.runners.app.notification.service.NotificationStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 트랜잭션 커밋 후에만 실행됨
 * 댓글 저장이 롤백되면 이 리스너는 실행되지 않음
 * Redis Stream에 이벤트 발행 시도, 실패 시 Outbox 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamEventListener {

    private final NotificationStreamService streamService;

    /**
     * 트랜잭션 커밋 후에만 실행됨
     * 댓글 저장이 롤백되면 이 리스너는 실행되지 않음
     * Redis Stream에 이벤트 발행 시도, 실패 시 Outbox 저장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            // Redis Stream에 이벤트 발행 시도
            streamService.publishEvent(event);
        } catch (Exception e) {
            // Stream 발행 실패는 로깅만 (Outbox에 저장되므로 유실 없음)
            log.error("Failed to publish notification event to stream for comment: {}", 
                    event.commentId(), e);
        }
    }
}
