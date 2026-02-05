package com.runners.app.notification.entity;

import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_created_at", columnList = "recipient_id,created_at"),
                @Index(name = "idx_notifications_recipient_is_read", columnList = "recipient_id,is_read"),
                @Index(name = "uk_notifications_dedupe_key", columnList = "dedupe_key", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notifications_recipient_id"))
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_post_id", foreignKey = @ForeignKey(name = "fk_notifications_related_post_id"))
    private CommunityPost relatedPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_comment_id", foreignKey = @ForeignKey(name = "fk_notifications_related_comment_id"))
    private CommunityComment relatedComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_notifications_actor_id"))
    private User actor;

    @Column(name = "dedupe_key", nullable = false, length = 100, unique = true)
    private String dedupeKey;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markAsRead() {
        if (!isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
}
