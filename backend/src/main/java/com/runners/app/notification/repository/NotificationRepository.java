package com.runners.app.notification.repository;

import com.runners.app.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n from Notification n
            join fetch n.recipient r
            left join fetch n.relatedPost p
            left join fetch n.relatedComment c
            left join fetch n.actor a
            where n.recipient.id = :recipientId
              and (
                :cursorCreatedAt is null
                or n.createdAt < :cursorCreatedAt
                or (n.createdAt = :cursorCreatedAt and n.id < :cursorId)
              )
            order by n.createdAt desc, n.id desc
            """)
    List<Notification> findByRecipientIdForCursor(
            @Param("recipientId") Long recipientId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select count(n) from Notification n
            where n.recipient.id = :recipientId
              and n.isRead = false
            """)
    long countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    @Query("""
            select n from Notification n
            where n.recipient.id = :recipientId
              and n.id = :notificationId
            """)
    Optional<Notification> findByRecipientIdAndId(
            @Param("recipientId") Long recipientId,
            @Param("notificationId") Long notificationId
    );
}
