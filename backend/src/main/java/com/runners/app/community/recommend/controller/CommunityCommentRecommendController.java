package com.runners.app.community.recommend.controller;

import com.runners.app.community.recommend.dto.response.CommunityCommentRecommendResponse;
import com.runners.app.community.recommend.service.CommunityCommentRecommendService;
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
@RequestMapping("/community/posts/{postId}/comments/{commentId}/recommend")
public class CommunityCommentRecommendController {

    private final CommunityCommentRecommendService communityCommentRecommendService;

    public CommunityCommentRecommendController(CommunityCommentRecommendService communityCommentRecommendService) {
        this.communityCommentRecommendService = communityCommentRecommendService;
    }

    @Operation(summary = "댓글 추천 여부 조회", description = "JWT로 인증된 사용자의 댓글 추천(좋아요) 여부를 조회합니다")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CommunityCommentRecommendResponse getRecommendStatus(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityCommentRecommendService.getRecommendStatus(userId, postId, commentId);
    }

    @Operation(summary = "댓글 추천", description = "JWT로 인증된 사용자가 댓글을 추천(좋아요)합니다 (idempotent)")
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CommunityCommentRecommendResponse recommend(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityCommentRecommendService.recommend(userId, postId, commentId);
    }

    @Operation(summary = "댓글 추천 취소", description = "JWT로 인증된 사용자가 댓글 추천(좋아요)을 취소합니다 (idempotent)")
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public CommunityCommentRecommendResponse unrecommend(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityCommentRecommendService.unrecommend(userId, postId, commentId);
    }
}
