package com.runners.app.community.comment.controller;

import com.runners.app.community.comment.dto.request.CreateCommunityCommentRequest;
import com.runners.app.community.comment.dto.response.CommunityCommentCursorListResponse;
import com.runners.app.community.comment.dto.response.CommunityCommentResponse;
import com.runners.app.community.comment.dto.response.DeleteCommunityCommentResponse;
import com.runners.app.community.comment.service.CommunityCommentService;
import com.runners.app.global.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/community/posts/{postId}/comments")
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;

    public CommunityCommentController(CommunityCommentService communityCommentService) {
        this.communityCommentService = communityCommentService;
    }

    @Operation(summary = "댓글 작성", description = "JWT로 인증된 사용자가 게시글에 댓글(대댓글 포함)을 작성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityCommentResponse createComment(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommunityCommentRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityCommentService.createComment(userId, postId, request);
    }

    @Operation(summary = "댓글 수정", description = "JWT로 인증된 사용자가 게시글에 댓글(대댓글 포함)을 수정")
    @PutMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommunityCommentResponse updateComment(
        Authentication authentication,
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @Valid @RequestBody CreateCommunityCommentRequest request
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityCommentService.updateComment(userId, postId, commentId, request);
    }

    @Operation(summary = "댓글 목록 조회", description = "커서 기반 페이지네이션(nextCursor를 다음 요청의 cursor로 전달)")
    @GetMapping
    public CommunityCommentCursorListResponse listComments(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityCommentService.listComments(postId, cursor, size);
    }

    @Operation(summary = "댓글 삭제", description = "댓글 작성자만 삭제 가능(soft delete)")
    @DeleteMapping("/{commentId}")
    public DeleteCommunityCommentResponse deleteComment(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Long userId = SecurityUtils.extractUserId(authentication);
        return communityCommentService.deleteComment(userId, postId, commentId);
    }
}
