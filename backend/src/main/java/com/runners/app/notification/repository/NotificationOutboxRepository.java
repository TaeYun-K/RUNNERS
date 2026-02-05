package com.runners.app.notification.repository;

import com.runners.app.notification.entity.NotificationOutbox;
import com.runners.app.notification.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    /**
     * PENDING 상태의 Outbox 조회 (재발행용)
     * status와 created_at 인덱스를 활용하여 성능 최적화
     */
    @Query("""
            select o from NotificationOutbox o
            where o.status = :status
            order by o.createdAt asc
            """)
    List<NotificationOutbox> findPendingForRepublish(
            @Param("status") OutboxStatus status,
            Pageable pageable
    );

    /**
     * 상태를 PUBLISHED로 변경
     */
    @Modifying
    @Query("""
            update NotificationOutbox o
            set o.status = 'PUBLISHED',
                o.publishedAt = CURRENT_TIMESTAMP
            where o.id = :id
            """)
    void updateStatusToPublished(@Param("id") Long id);
}
