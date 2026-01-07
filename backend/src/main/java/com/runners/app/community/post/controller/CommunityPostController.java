package com.runners.app.community.post.controller;

import com.runners.app.community.post.dto.request.CreateCommunityPostRequest;
import com.runners.app.community.post.dto.response.CreateCommunityPostResponse;
import com.runners.app.community.post.dto.response.CommunityPostDetailResponse;
import com.runners.app.community.post.dto.response.CommunityPostCursorListResponse;
import com.runners.app.community.post.service.CommunityPostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/community/posts")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    public CommunityPostController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    @Operation(summary = "게시글 작성", description = "JWT로 인증된 사용자가 게시글을 작성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCommunityPostResponse createPost(
            Authentication authentication,
            @Valid @RequestBody CreateCommunityPostRequest request
    ) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject");
        }

        return communityPostService.createPost(userId, request);
    }

    @Operation(summary = "게시글 조회", description = "JWT로 인증된 사용자가 게시글을 조회(유저-일 단위로 조회수 1회 증가)")
    @GetMapping("/{postId}")
    public CommunityPostDetailResponse getPost(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject");
        }

        return communityPostService.getPost(userId, postId);
    }

    @Operation(summary = "게시글 목록 조회", description = "최신순 커서 기반 목록 조회(nextCursor를 다음 요청의 cursor로 전달)")
    @GetMapping
    public CommunityPostCursorListResponse listPosts(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return communityPostService.listPosts(cursor, size);
    }
}
