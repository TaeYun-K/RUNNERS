package com.runners.app.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.runners.app.notification.entity.DeviceToken;
import com.runners.app.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FcmService {

    @Value("${firebase.service-account.path:classpath:firebase-service-account.json}")
    private Resource firebaseServiceAccountResource;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.warn("Firebase is disabled. FCM notifications will not be sent.");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (!firebaseServiceAccountResource.exists()) {
                    log.warn("Firebase service account file not found at: {}. FCM notifications will not be sent.", 
                            firebaseServiceAccountResource);
                    return;
                }

                InputStream serviceAccount = firebaseServiceAccountResource.getInputStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            } else {
                log.info("Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK. FCM notifications will not be sent.", e);
            // 초기화 실패 시에도 앱은 계속 실행되도록 예외를 던지지 않음
        } catch (Exception e) {
            log.error("Unexpected error during Firebase initialization. FCM notifications will not be sent.", e);
        }
    }

    /**
     * 단일 사용자에게 푸시 알림 발송
     */
    public void sendNotification(List<DeviceToken> tokens, Notification notification) {
        if (!isFirebaseInitialized()) {
            log.debug("Firebase not initialized, skipping FCM notification");
            return;
        }

        if (tokens == null || tokens.isEmpty()) {
            log.debug("No device tokens found for user");
            return;
        }

        String title = getNotificationTitle(notification);
        String body = getNotificationBody(notification);

        List<Message> messages = tokens.stream()
                .map(token -> createMessage(token.getToken(), title, body, notification))
                .collect(Collectors.toList());

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages);
            log.info("Sent {} messages, {} successful, {} failed", 
                    messages.size(), response.getSuccessCount(), response.getFailureCount());

            // 실패한 토큰 처리
            List<String> invalidTokens = new ArrayList<>();
            for (int i = 0; i < response.getResponses().size(); i++) {
                SendResponse sendResponse = response.getResponses().get(i);
                if (!sendResponse.isSuccessful()) {
                    String token = tokens.get(i).getToken();
                    log.warn("Failed to send notification to token {}: {}", 
                            token, sendResponse.getException().getMessage());
                    
                    if (isInvalidToken(sendResponse.getException())) {
                        invalidTokens.add(token);
                    }
                }
            }

            // 무효한 토큰은 나중에 정리 (NotificationService에서 처리)
            if (!invalidTokens.isEmpty()) {
                log.info("Found {} invalid tokens to clean up", invalidTokens.size());
            }

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notifications", e);
            throw new RuntimeException("FCM send failed", e);
        }
    }

    /**
     * 여러 사용자에게 일괄 푸시 알림 발송 (멀티캐스트)
     */
    public void sendMulticastNotification(
            Map<Long, List<DeviceToken>> tokensByUser,
            List<Notification> notifications
    ) {
        if (!isFirebaseInitialized()) {
            log.debug("Firebase not initialized, skipping FCM multicast notification");
            return;
        }

        if (tokensByUser == null || tokensByUser.isEmpty()) {
            log.debug("No device tokens found for users");
            return;
        }

        List<Message> messages = new ArrayList<>();
        List<DeviceToken> tokenList = new ArrayList<>();

        for (Notification notification : notifications) {
            Long userId = notification.getRecipient().getId();
            List<DeviceToken> tokens = tokensByUser.get(userId);
            
            if (tokens != null && !tokens.isEmpty()) {
                String title = getNotificationTitle(notification);
                String body = getNotificationBody(notification);

                for (DeviceToken token : tokens) {
                    messages.add(createMessage(token.getToken(), title, body, notification));
                    tokenList.add(token);
                }
            }
        }

        if (messages.isEmpty()) {
            log.debug("No messages to send");
            return;
        }

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages);
            log.info("Sent {} multicast messages, {} successful, {} failed", 
                    messages.size(), response.getSuccessCount(), response.getFailureCount());

            // 실패한 토큰 처리
            List<String> invalidTokens = new ArrayList<>();
            for (int i = 0; i < response.getResponses().size(); i++) {
                SendResponse sendResponse = response.getResponses().get(i);
                if (!sendResponse.isSuccessful()) {
                    String token = tokenList.get(i).getToken();
                    log.warn("Failed to send multicast notification to token {}: {}", 
                            token, sendResponse.getException().getMessage());
                    
                    if (isInvalidToken(sendResponse.getException())) {
                        invalidTokens.add(token);
                    }
                }
            }

            if (!invalidTokens.isEmpty()) {
                log.info("Found {} invalid tokens to clean up from multicast", invalidTokens.size());
            }

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast FCM notifications", e);
            throw new RuntimeException("FCM multicast send failed", e);
        }
    }

    /**
     * FCM 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        if (!isFirebaseInitialized()) {
            log.debug("Firebase not initialized, cannot validate token");
            return false;
        }

        try {
            FirebaseMessaging.getInstance().send(
                    Message.builder()
                            .setToken(token)
                            .putData("type", "validation")
                            .build(),
                    true  // dry run
            );
            return true;
        } catch (FirebaseMessagingException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Firebase가 초기화되었는지 확인
     */
    private boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }

    private Message createMessage(String token, String title, String body, Notification notification) {
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("notificationId", notification.getId().toString())
                .putData("type", notification.getType().name());

        if (notification.getRelatedPost() != null) {
            builder.putData("postId", notification.getRelatedPost().getId().toString());
        }

        if (notification.getRelatedComment() != null) {
            builder.putData("commentId", notification.getRelatedComment().getId().toString());
        }

        if (notification.getActor() != null) {
            builder.putData("actorId", notification.getActor().getId().toString());
        }

        return builder.build();
    }

    private String getNotificationTitle(Notification notification) {
        return switch (notification.getType()) {
            case COMMENT_ON_MY_POST -> "새 댓글이 달렸습니다";
            case COMMENT_ON_MY_COMMENTED_POST -> "새 댓글이 달렸습니다";
            case REPLY_TO_MY_COMMENT -> "대댓글이 달렸습니다";
            case RECOMMEND_ON_MY_POST -> "게시글 추천 알림";
            case RECOMMEND_ON_MY_COMMENT -> "댓글 추천 알림";
        };
    }

    private String getNotificationBody(Notification notification) {
        String actorName = notification.getActor() != null 
                ? notification.getActor().getDisplayName() 
                : "누군가";
        
        return switch (notification.getType()) {
            case COMMENT_ON_MY_POST -> actorName + "님이 댓글을 남겼습니다";
            case COMMENT_ON_MY_COMMENTED_POST -> actorName + "님이 댓글을 남겼습니다";
            case REPLY_TO_MY_COMMENT -> actorName + "님이 대댓글을 남겼습니다";
            case RECOMMEND_ON_MY_POST -> actorName + "님이 게시글을 추천했습니다";
            case RECOMMEND_ON_MY_COMMENT -> actorName + "님이 댓글을 추천했습니다";
        };
    }

    private boolean isInvalidToken(Exception exception) {
        if (exception instanceof FirebaseMessagingException) {
            FirebaseMessagingException fme = (FirebaseMessagingException) exception;
            MessagingErrorCode errorCode = fme.getMessagingErrorCode();
            return errorCode == MessagingErrorCode.INVALID_ARGUMENT
                    || errorCode == MessagingErrorCode.UNREGISTERED;
        }
        return false;
    }
}
