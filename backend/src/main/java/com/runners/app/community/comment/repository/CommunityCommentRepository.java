package com.runners.app.community.comment.repository;

import com.runners.app.community.comment.entity.CommunityComment;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

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
            org.springframework.data.domain.Pageable pageable
    );
}
