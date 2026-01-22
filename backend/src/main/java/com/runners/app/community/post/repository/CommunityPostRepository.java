package com.runners.app.community.post.repository;

import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.community.post.entity.CommunityPost;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from CommunityPost p
            where p.id = :postId
            """)
    Optional<CommunityPost> findByIdForUpdate(@Param("postId") Long postId);

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

    @Query(
            value = """
            select p.id
            from community_posts p
            where p.status = :postStatus
              and (
                match(p.title) against (:q in natural language mode) > 0
                or exists (
                  select 1
                  from community_comments c
                  where c.post_id = p.id
                    and c.status = :commentStatus
                    and match(c.content) against (:q in natural language mode) > 0
                )
              )
              and (
                :cursorCreatedAt is null
                or p.created_at < :cursorCreatedAt
                or (p.created_at = :cursorCreatedAt and p.id < :cursorId)
              )
            order by p.created_at desc, p.id desc
            limit :limit
            """,
            nativeQuery = true
    )
    List<Long> searchPostIdsForCursor(
            @Param("postStatus") String postStatus,
            @Param("commentStatus") String commentStatus,
            @Param("q") String q,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );

    @EntityGraph(attributePaths = "author")
    @Query("""
            select p from CommunityPost p
            where p.status = :status
              and p.id in :ids
            """)
    List<CommunityPost> findAllByIdInWithAuthor(
            @Param("status") CommunityContentStatus status,
            @Param("ids") List<Long> ids
    );
}
