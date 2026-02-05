package com.runners.app.community.comment.repository;

import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.community.comment.entity.CommunityComment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c from CommunityComment c
            join fetch c.author a
            left join fetch c.parent p
            where c.id = :commentId
            """)
    Optional<CommunityComment> findByIdForUpdate(@Param("commentId") Long commentId);

    @Query("""
            select c from CommunityComment c
            join fetch c.author a
            left join fetch c.parent p
            where c.post.id = :postId
              and (
                :cursorCreatedAt is null
                or c.createdAt > :cursorCreatedAt
                or (c.createdAt = :cursorCreatedAt and c.id > :cursorId)
              )
            order by c.createdAt asc, c.id asc
            """)
    List<CommunityComment> findForCursor(
            @Param("postId") Long postId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select c from CommunityComment c
            join fetch c.post p
            join fetch p.author
            where c.author.id = :authorId
              and c.status = :status
              and (
                :cursorCreatedAt is null
                or c.createdAt < :cursorCreatedAt
                or (c.createdAt = :cursorCreatedAt and c.id < :cursorId)
              )
            order by c.createdAt desc, c.id desc
            """)
    List<CommunityComment> findByAuthorIdForCursor(
            @Param("authorId") Long authorId,
            @Param("status") CommunityContentStatus status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select count(distinct c.post.id) from CommunityComment c
            where c.author.id = :authorId and c.status = :status
            """)
    long countDistinctPostIdsByAuthorIdAndStatus(
            @Param("authorId") Long authorId,
            @Param("status") CommunityContentStatus status
    );

    @Query("""
            select distinct c.author.id from CommunityComment c
            where c.post.id = :postId
              and c.status = :status
              and (:excludeAuthorId is null or c.author.id != :excludeAuthorId)
            """)
    List<Long> findDistinctAuthorIdsByPostId(
            @Param("postId") Long postId,
            @Param("excludeAuthorId") Long excludeAuthorId,
            @Param("status") CommunityContentStatus status
    );
}
