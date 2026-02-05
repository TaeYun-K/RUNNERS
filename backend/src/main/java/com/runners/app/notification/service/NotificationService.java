package com.runners.app.notification.service;

import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.community.comment.event.CommentCreatedEvent;
import com.runners.app.community.comment.repository.CommunityCommentRepository;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.global.util.CursorUtils;
import com.runners.app.notification.dto.response.NotificationCursorListResponse;
import com.runners.app.notification.dto.response.NotificationResponse;
import com.runners.app.notification.dto.response.UnreadNotificationCountResponse;
import com.runners.app.notification.entity.DeviceToken;
import com.runners.app.notification.entity.Notification;
import com.runners.app.notification.entity.NotificationType;
import com.runners.app.notification.exception.NotificationDomainException;
import com.runners.app.notification.repository.DeviceTokenRepository;
import com.runners.app.notification.repository.NotificationRepository;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import com.runners.app.user.service.UserProfileImageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final UserProfileImageResolver userProfileImageResolver;

    /**
     * Redis Stream에서 받은 이벤트 처리
     * CommentCreatedEvent를 받아서 알림 발송
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEvent(CommentCreatedEvent event) {
        sendCommentNotifications(
                event.commentId(),
                event.postId(),
                event.postAuthorId(),
                event.commentAuthorId(),
                event.parentCommentId(),
                event.parentCommentAuthorId()
        );
    }

    /**
     * 댓글 생성 시 알림 발송
     * CommentCreatedEvent를 받아서 처리하는 메서드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendCommentNotifications(
            Long commentId,
            Long postId,
            Long postAuthorId,
            Long commentAuthorId,
            Long parentCommentId,
            Long parentCommentAuthorId
    ) {
        // ID 값으로 엔티티 조회 (LAZY 로딩 문제 없음)
        CommunityPost post = communityPostRepository.getReferenceById(postId);
        CommunityComment comment = communityCommentRepository.getReferenceById(commentId);
        User commentAuthor = userRepository.getReferenceById(commentAuthorId);

        // 대댓글인 경우: 부모 댓글 작성자에게만 알림
        if (parentCommentId != null) {
            if (parentCommentAuthorId != null && !parentCommentAuthorId.equals(commentAuthorId)) {
                sendNotificationToParentCommentAuthor(
                        post,
                        comment,
                        commentAuthor,
                        parentCommentAuthorId
                );
            }
            return;  // 대댓글은 부모 댓글 작성자에게만 알림
        }

        // 일반 댓글인 경우
        // 1. 게시글 작성자에게 알림 발송
        if (!postAuthorId.equals(commentAuthorId)) {
            sendNotificationToPostAuthor(post, comment, commentAuthor, postAuthorId);
        }

        // 2. 이전 댓글 작성자들에게 알림 발송
        sendNotificationsToPreviousCommentAuthors(
                post,
                comment,
                commentAuthor,
                commentAuthorId,
                postAuthorId
        );
    }

    private void sendNotificationToParentCommentAuthor(
            CommunityPost post,
            CommunityComment comment,
            User commentAuthor,
            Long parentCommentAuthorId
    ) {
        String dedupeKey = generateDedupeKey(
                NotificationType.REPLY_TO_MY_COMMENT,
                parentCommentAuthorId,
                comment.getId()
        );

        User recipient = userRepository.getReferenceById(parentCommentAuthorId);

        try {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .type(NotificationType.REPLY_TO_MY_COMMENT)
                    .relatedPost(post)
                    .relatedComment(comment)
                    .actor(commentAuthor)
                    .dedupeKey(dedupeKey)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

            // 저장 성공 시에만 FCM 푸시 알림 발송
            sendPushNotification(parentCommentAuthorId, notification);

        } catch (DataIntegrityViolationException e) {
            // UNIQUE 제약 위반 시 무시 (idempotent)
            // 중복 알림이므로 푸시 발송하지 않음
            log.debug("Duplicate notification ignored: {}", dedupeKey);
        }
    }

    private void sendNotificationToPostAuthor(
            CommunityPost post,
            CommunityComment comment,
            User commentAuthor,
            Long postAuthorId
    ) {
        String dedupeKey = generateDedupeKey(
                NotificationType.COMMENT_ON_MY_POST,
                postAuthorId,
                comment.getId()
        );

        User recipient = userRepository.getReferenceById(postAuthorId);

        try {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .type(NotificationType.COMMENT_ON_MY_POST)
                    .relatedPost(post)
                    .relatedComment(comment)
                    .actor(commentAuthor)
                    .dedupeKey(dedupeKey)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

            // 저장 성공 시에만 FCM 푸시 알림 발송
            sendPushNotification(postAuthorId, notification);

        } catch (DataIntegrityViolationException e) {
            // UNIQUE 제약 위반 시 무시 (idempotent)
            // 중복 알림이므로 푸시 발송하지 않음
            log.debug("Duplicate notification ignored: {}", dedupeKey);
        }
    }

    private void sendNotificationsToPreviousCommentAuthors(
            CommunityPost post,
            CommunityComment comment,
            User commentAuthor,
            Long commentAuthorId,
            Long postAuthorId
    ) {
        // 이전 댓글 작성자 목록 조회 (현재 댓글 작성자 제외)
        List<Long> previousCommentAuthorIds =
                communityCommentRepository.findDistinctAuthorIdsByPostId(
                        post.getId(),
                        commentAuthorId,  // 현재 댓글 작성자 제외
                        CommunityContentStatus.ACTIVE
                );

        // 게시글 작성자도 제외
        List<Long> recipientIds = previousCommentAuthorIds.stream()
                .filter(id -> !id.equals(postAuthorId))
                .toList();

        if (recipientIds.isEmpty()) {
            return;
        }

        // 개별 저장 시도하면서 성공한 알림만 수집 (중복 방지 및 푸시 발송 보장)
        List<Notification> savedNotifications = new ArrayList<>();
        List<Long> savedUserIds = new ArrayList<>();

        for (Long userId : recipientIds) {
            String dedupeKey = generateDedupeKey(
                    NotificationType.COMMENT_ON_MY_COMMENTED_POST,
                    userId,
                    comment.getId()
            );

            try {
                Notification notification = Notification.builder()
                        .recipient(userRepository.getReferenceById(userId))
                        .type(NotificationType.COMMENT_ON_MY_COMMENTED_POST)
                        .relatedPost(post)
                        .relatedComment(comment)
                        .actor(commentAuthor)
                        .dedupeKey(dedupeKey)
                        .isRead(false)
                        .build();

                // 개별 저장 시도
                notificationRepository.save(notification);

                // 성공한 경우만 리스트에 추가
                savedNotifications.add(notification);
                savedUserIds.add(userId);

            } catch (DataIntegrityViolationException e) {
                // 중복 알림 무시 (idempotent)
                log.debug("Duplicate notification ignored: {}", dedupeKey);
            } catch (Exception e) {
                log.error("Failed to save notification for user: {}", userId, e);
            }
        }

        // 성공한 알림에 대해서만 푸시 발송
        if (!savedNotifications.isEmpty()) {
            sendMulticastPushNotification(savedUserIds, savedNotifications);
        }
    }

    private void sendPushNotification(Long userId, Notification notification) {
        try {
            List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
            if (!tokens.isEmpty()) {
                fcmService.sendNotification(tokens, notification);
            }
        } catch (Exception e) {
            // FCM 발송 실패는 로깅만 하고 알림 DB 저장은 유지
            log.error("Failed to send FCM notification to user: {}", userId, e);
        }
    }

    private void sendMulticastPushNotification(
            List<Long> userIds,
            List<Notification> notifications
    ) {
        try {
            List<DeviceToken> allTokens = deviceTokenRepository.findByUserIds(userIds);
            Map<Long, List<DeviceToken>> tokensByUser = allTokens.stream()
                    .collect(Collectors.groupingBy(token -> token.getUser().getId()));

            fcmService.sendMulticastNotification(tokensByUser, notifications);
        } catch (Exception e) {
            log.error("Failed to send multicast FCM notifications", e);
        }
    }

    private String generateDedupeKey(NotificationType type, Long recipientId, Long commentId) {
        return String.format("%s:%d:%d", type.name(), recipientId, commentId);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository
                .findByRecipientIdAndId(userId, notificationId)
                .orElseThrow(NotificationDomainException::notificationNotFound);

        // 권한 확인: 자신의 알림만 읽을 수 있음
        if (!notification.getRecipient().getId().equals(userId)) {
            throw NotificationDomainException.noPermission();
        }

        notification.markAsRead();
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdForCursor(
                userId, null, null, PageRequest.of(0, Integer.MAX_VALUE)
        );
        unreadNotifications.stream()
                .filter(n -> !n.isRead())
                .forEach(Notification::markAsRead);
    }

    /**
     * 알림 목록 조회 (커서 기반 페이지네이션)
     */
    @Transactional(readOnly = true)
    public NotificationCursorListResponse listNotifications(Long userId, String cursor, int size) {
        CursorUtils.Cursor decodedCursor;
        try {
            decodedCursor = CursorUtils.decodeCursor(cursor);
        } catch (IllegalArgumentException e) {
            throw NotificationDomainException.notificationNotFound();
        }

        List<Notification> notifications = notificationRepository.findByRecipientIdForCursor(
                userId,
                decodedCursor != null ? decodedCursor.createdAt() : null,
                decodedCursor != null ? decodedCursor.id() : null,
                PageRequest.of(0, size + 1)  // hasNext 확인을 위해 +1
        );

        boolean hasNext = notifications.size() > size;
        List<Notification> pageItems = hasNext 
                ? notifications.subList(0, size) 
                : notifications;

        List<NotificationResponse> responses = pageItems.stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            Notification last = pageItems.get(pageItems.size() - 1);
            nextCursor = CursorUtils.encodeCursor(last.getCreatedAt(), last.getId());
        }

        return new NotificationCursorListResponse(responses, hasNext, nextCursor);
    }

    /**
     * 안읽음 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countUnreadByRecipientId(userId);
        return new UnreadNotificationCountResponse(count);
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        User actor = notification.getActor();
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getRelatedPost() != null ? notification.getRelatedPost().getId() : null,
                notification.getRelatedComment() != null ? notification.getRelatedComment().getId() : null,
                actor != null ? actor.getId() : null,
                actor != null ? actor.getDisplayName() : null,
                actor != null ? userProfileImageResolver.resolve(actor) : null,
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

}
