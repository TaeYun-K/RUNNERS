package com.runners.app.community.post.service;

import com.runners.app.community.CommunityContentStatus;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.dto.request.CreateCommunityPostRequest;
import com.runners.app.community.post.dto.response.CommunityPostCursorListResponse;
import com.runners.app.community.post.dto.response.CreateCommunityPostResponse;
import com.runners.app.community.post.dto.response.CommunityPostDetailResponse;
import com.runners.app.community.post.dto.response.CommunityPostSummaryResponse;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.community.view.CommunityPostView;
import com.runners.app.community.view.CommunityPostViewId;
import com.runners.app.community.view.CommunityPostViewRepository;
import com.runners.app.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostViewRepository communityPostViewRepository;
    private final UserRepository userRepository;

    public CommunityPostService(
            CommunityPostRepository communityPostRepository,
            CommunityPostViewRepository communityPostViewRepository,
            UserRepository userRepository
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityPostViewRepository = communityPostViewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CreateCommunityPostResponse createPost(Long authorId, CreateCommunityPostRequest request) {
        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CommunityPost saved = communityPostRepository.save(
                CommunityPost.builder()
                        .author(author)
                        .title(request.title())
                        .content(request.content())
                        .build()
        );

        return new CreateCommunityPostResponse(
                saved.getId(),
                author.getId(),
                saved.getTitle(),
                saved.getContent(),
                saved.getViewCount(),
                saved.getRecommendCount(),
                saved.getCommentCount(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public CommunityPostDetailResponse getPost(Long viewerId, Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        var viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var viewId = new CommunityPostViewId(postId, viewerId, LocalDate.now());
        boolean alreadyViewedToday = communityPostViewRepository.existsById(viewId);
        if (!alreadyViewedToday) {
            communityPostViewRepository.save(
                    CommunityPostView.builder()
                            .id(viewId)
                            .post(post)
                            .user(viewer)
                            .viewedAt(LocalDateTime.now())
                            .build()
            );
            post.increaseViewCount();
        }

        var author = post.getAuthor();

        return new CommunityPostDetailResponse(
                post.getId(),
                author.getId(),
                author.getDisplayName(),
                author.getPicture(),
                author.getTotalDistanceKm(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getRecommendCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public CommunityPostCursorListResponse listPosts(String cursor, int size) {
        int safeSize = Math.min(50, Math.max(1, size));

        Cursor decodedCursor = decodeCursor(cursor);
        int fetchSize = safeSize + 1;

        List<CommunityPost> fetched = communityPostRepository.findForCursor(
                CommunityContentStatus.ACTIVE,
                decodedCursor == null ? null : decodedCursor.createdAt(),
                decodedCursor == null ? Long.MAX_VALUE : decodedCursor.id(),
                PageRequest.of(0, fetchSize)
        );

        boolean hasNext = fetched.size() > safeSize;
        List<CommunityPost> pageItems = hasNext ? fetched.subList(0, safeSize) : fetched;

        var posts = pageItems.stream()
                .map(post -> new CommunityPostSummaryResponse(
                        post.getId(),
                        post.getAuthor().getId(),
                        post.getAuthor().getDisplayName(),
                        post.getAuthor().getTotalDistanceKm(),
                        post.getTitle(),
                        toContentPreview(post.getContent()),
                        post.getViewCount(),
                        post.getRecommendCount(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ))
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            CommunityPost last = pageItems.get(pageItems.size() - 1);
            nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
        }

        return new CommunityPostCursorListResponse(posts, nextCursor);
    }

    private record Cursor(LocalDateTime createdAt, long id) {}

    private Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank() || "null".equalsIgnoreCase(cursor)) return null;

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid cursor");
            LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
            long id = Long.parseLong(parts[1]);
            if (id <= 0) throw new IllegalArgumentException("Invalid cursor");
            return new Cursor(createdAt, id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    private String encodeCursor(LocalDateTime createdAt, Long id) {
        String raw = createdAt.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String toContentPreview(String content) {
        if (content == null) return "";
        String singleLine = content.replaceAll("\\s+", " ").trim();
        int limit = 120;
        if (singleLine.length() <= limit) return singleLine;
        return singleLine.substring(0, limit).trim() + "…";
    }
}
