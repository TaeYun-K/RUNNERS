package com.runners.app.community.recommend.repository;

import com.runners.app.community.recommend.entity.CommunityPostRecommend;
import com.runners.app.community.recommend.entity.CommunityPostRecommendId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRecommendRepository extends JpaRepository<CommunityPostRecommend, CommunityPostRecommendId> {
}

