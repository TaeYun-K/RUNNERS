package com.runners.app.community.post.repository;

import com.runners.app.community.post.entity.CommunityPostImage;
import com.runners.app.community.post.entity.CommunityPostImageStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, Long> {

    @Query("""
            select i from CommunityPostImage i
            where i.post.id in :postIds
              and i.status = :status
            order by i.post.id asc, i.sortOrder asc, i.id asc
            """)
    List<CommunityPostImage> findByPostIdsAndStatus(
            @Param("postIds") List<Long> postIds,
            @Param("status") CommunityPostImageStatus status
    );
}

