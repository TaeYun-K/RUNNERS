package com.runners.app.community.recommend.repository;

import com.runners.app.community.recommend.entity.CommunityCommentRecommend;
import com.runners.app.community.recommend.entity.CommunityCommentRecommendId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRecommendRepository extends JpaRepository<CommunityCommentRecommend, CommunityCommentRecommendId> {
}

