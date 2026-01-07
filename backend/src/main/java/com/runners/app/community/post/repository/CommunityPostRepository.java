package com.runners.app.community.post.repository;

import com.runners.app.community.CommunityContentStatus;
import com.runners.app.community.post.entity.CommunityPost;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @EntityGraph(attributePaths = "author")
    Page<CommunityPost> findByStatus(CommunityContentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query("""
            select p from CommunityPost p
            where p.status = :status
              and (
                :cursorCreatedAt is null
                or p.createdAt < :cursorCreatedAt
                or (p.createdAt = :cursorCreatedAt and p.id < :cursorId)
              )
            order by p.createdAt desc, p.id desc
            """)
    List<CommunityPost> findForCursor(
            @Param("status") CommunityContentStatus status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
