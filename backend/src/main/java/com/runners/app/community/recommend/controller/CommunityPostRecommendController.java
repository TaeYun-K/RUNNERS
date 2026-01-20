package com.runners.app.community.recommend.controller;

import com.runners.app.community.recommend.dto.response.CommunityPostRecommendResponse;
import com.runners.app.community.recommend.service.CommunityPostRecommendService;
import com.runners.app.global.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/community/posts/{postId}/recommend")
public class CommunityPostRecommendController {

    private final CommunityPostRecommendService communityPostRecommendService;

    public CommunityPostRecommendController(CommunityPostRecommendService communityPostRecommendService) {
        this.communityPostRecommendService = communityPostRecommendService;
    }

    @Operation(summary = "게시글 추천 여부 조회", description = "JWT로 인증된 사용자의 게시글 추천(좋아요) 여부를 조회합니다")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CommunityPostRecommendResponse getRecommendStatus(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityPostRecommendService.getRecommendStatus(userId, postId);
    }

    @Operation(summary = "게시글 추천", description = "JWT로 인증된 사용자가 게시글을 추천(좋아요)합니다 (idempotent)")
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CommunityPostRecommendResponse recommend(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityPostRecommendService.recommend(userId, postId);
    }

    @Operation(summary = "게시글 추천 취소", description = "JWT로 인증된 사용자가 게시글 추천(좋아요)을 취소합니다 (idempotent)")
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public CommunityPostRecommendResponse unrecommend(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityPostRecommendService.unrecommend(userId, postId);
    }
}
