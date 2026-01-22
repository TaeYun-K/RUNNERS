package com.runners.app.community.post.service;

import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.entity.CommunityPostImage;
import com.runners.app.community.post.entity.CommunityPostImageStatus;
import com.runners.app.community.post.dto.request.CreateCommunityPostRequest;
import com.runners.app.community.post.dto.response.CommunityPostCursorListResponse;
import com.runners.app.community.post.dto.response.CommunityPostResponse;
import com.runners.app.community.post.dto.response.CommunityPostDetailResponse;
import com.runners.app.community.post.dto.response.CommunityPostSummaryResponse;
import com.runners.app.community.post.repository.CommunityPostImageRepository;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.community.upload.service.CommunityUploadService;
import com.runners.app.community.view.CommunityPostViewTracker;
import com.runners.app.user.repository.UserRepository;
import com.runners.app.user.service.UserProfileImageResolver;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostViewTracker communityPostViewTracker;
    private final UserRepository userRepository;
    private final CommunityUploadService communityUploadService;
    private final CommunityPostImageRepository communityPostImageRepository;
    private final UserProfileImageResolver userProfileImageResolver;

    public CommunityPostService(
            CommunityPostRepository communityPostRepository,
            CommunityPostViewTracker communityPostViewTracker,
            UserRepository userRepository,
            CommunityUploadService communityUploadService,
            CommunityPostImageRepository communityPostImageRepository,
            UserProfileImageResolver userProfileImageResolver
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityPostViewTracker = communityPostViewTracker;
        this.userRepository = userRepository;
        this.communityUploadService = communityUploadService;
        this.communityPostImageRepository = communityPostImageRepository;
        this.userProfileImageResolver = userProfileImageResolver;
    }

    @Transactional
    public CommunityPostResponse createPost(Long authorId, CreateCommunityPostRequest request) {
        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CommunityPost post = CommunityPost.builder()
                .author(author)
                .title(request.title())
                .content(request.content())
                .build();

        applyImageKeys(post, request.imageKeys());
        CommunityPost saved = communityPostRepository.save(post);

        return new CommunityPostResponse(
                saved.getId(),
                author.getId(),
                author.getDisplayName(),
                userProfileImageResolver.resolve(author),
                saved.getTitle(),
                saved.getContent(),
                saved.getViewCount(),
                saved.getRecommendCount(),
                saved.getCommentCount(),
                saved.getCreatedAt(),
                toImageUrls(saved)
        );
    }

    @Transactional
    public CommunityPostResponse updatePost(Long authorId, Long postId, CreateCommunityPostRequest request) {
        var author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // 작성자 검증
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to update this post");
        }

        post.updateContent(request.title(), request.content());
        if (request.imageKeys() != null) {
            applyImageKeys(post, request.imageKeys());
        }

        return new CommunityPostResponse(
            post.getId(),
            author.getId(),
            author.getDisplayName(),
            userProfileImageResolver.resolve(author),
            post.getTitle(),
            post.getContent(),
            post.getViewCount(),
            post.getRecommendCount(),
            post.getCommentCount(),
            post.getCreatedAt(),
            toImageUrls(post)
        );
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        var post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // 작성자 검증
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }

        // 이미 삭제된 경우
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            return;
        }

        post.markDeleted();
        if (post.getImages() != null) {
            post.getImages().forEach(CommunityPostImage::markDeleted);
        }
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

        boolean isFirstViewToday = communityPostViewTracker.markViewedTodayIfFirst(postId, viewerId);
        if (isFirstViewToday) {
            post.increaseViewCount();
        }

        var author = post.getAuthor();
        return new CommunityPostDetailResponse(
                post.getId(),
                author.getId(),
                author.getDisplayName(),
                userProfileImageResolver.resolve(author),
                author.getTotalDistanceKm(),
                post.getTitle(),
                post.getContent(),
                toImageKeys(post),
                toImageUrls(post),
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

        Map<Long, String> thumbnailUrlByPostId = buildThumbnailUrlByPostId(pageItems);

        var posts = pageItems.stream()
                .map(post -> new CommunityPostSummaryResponse(
                        post.getId(),
                        post.getAuthor().getId(),
                        post.getAuthor().getDisplayName(),
                        userProfileImageResolver.resolve(post.getAuthor()),
                        post.getAuthor().getTotalDistanceKm(),
                        post.getTitle(),
                        toContentPreview(post.getContent()),
                        thumbnailUrlByPostId.get(post.getId()),
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

    @Transactional(readOnly = true)
    public CommunityPostCursorListResponse searchPosts(String query, String cursor, int size) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query is required");
        }

        int safeSize = Math.min(50, Math.max(1, size));
        String trimmedQuery = query.trim();

        Cursor decodedCursor = decodeCursor(cursor);
        int fetchSize = safeSize + 1;

        List<Long> fetchedIds = communityPostRepository.searchPostIdsForCursor(
                CommunityContentStatus.ACTIVE.name(),
                CommunityContentStatus.ACTIVE.name(),
                trimmedQuery,
                decodedCursor == null ? null : decodedCursor.createdAt(),
                decodedCursor == null ? Long.MAX_VALUE : decodedCursor.id(),
                fetchSize
        );

        boolean hasNext = fetchedIds.size() > safeSize;
        List<Long> pageIds = hasNext ? fetchedIds.subList(0, safeSize) : fetchedIds;
        if (pageIds.isEmpty()) {
            return new CommunityPostCursorListResponse(List.of(), null);
        }

        List<CommunityPost> fetchedPosts = communityPostRepository.findAllByIdInWithAuthor(
                CommunityContentStatus.ACTIVE,
                pageIds
        );
        Map<Long, CommunityPost> postById = fetchedPosts.stream()
                .collect(Collectors.toMap(CommunityPost::getId, post -> post));

        List<CommunityPost> orderedPosts = new ArrayList<>(pageIds.size());
        for (Long id : pageIds) {
            CommunityPost post = postById.get(id);
            if (post != null) {
                orderedPosts.add(post);
            }
        }

        Map<Long, String> thumbnailUrlByPostId = buildThumbnailUrlByPostId(orderedPosts);

        var posts = orderedPosts.stream()
                .map(post -> new CommunityPostSummaryResponse(
                        post.getId(),
                        post.getAuthor().getId(),
                        post.getAuthor().getDisplayName(),
                        userProfileImageResolver.resolve(post.getAuthor()),
                        post.getAuthor().getTotalDistanceKm(),
                        post.getTitle(),
                        toContentPreview(post.getContent()),
                        thumbnailUrlByPostId.get(post.getId()),
                        post.getViewCount(),
                        post.getRecommendCount(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ))
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasNext && !orderedPosts.isEmpty()) {
            CommunityPost last = orderedPosts.get(orderedPosts.size() - 1);
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

    private Map<Long, String> buildThumbnailUrlByPostId(List<CommunityPost> posts) {
        Map<Long, String> thumbnailUrlByPostId = new HashMap<>();
        if (posts == null || posts.isEmpty()) return thumbnailUrlByPostId;

        List<Long> postIds = posts.stream().map(CommunityPost::getId).collect(Collectors.toList());
        List<CommunityPostImage> images =
                communityPostImageRepository.findByPostIdsAndStatus(postIds, CommunityPostImageStatus.ACTIVE);

        Map<Long, CommunityPostImage> firstImageByPostId = new HashMap<>();
        for (CommunityPostImage image : images) {
            Long postId = image.getPost().getId();
            firstImageByPostId.putIfAbsent(postId, image);
        }

        for (Map.Entry<Long, CommunityPostImage> entry : firstImageByPostId.entrySet()) {
            thumbnailUrlByPostId.put(
                    entry.getKey(),
                    communityUploadService.toPublicFileUrl(entry.getValue().getS3Key())
            );
        }

        return thumbnailUrlByPostId;
    }

    private List<String> toImageUrls(CommunityPost post) {
        if (post.getImages() == null || post.getImages().isEmpty()) return List.of();
        return post.getImages().stream()
                .filter(CommunityPostImage::isActive)
                .map(image -> communityUploadService.toPublicFileUrl(image.getS3Key()))
                .collect(Collectors.toList());
    }

    private List<String> toImageKeys(CommunityPost post) {
        if (post.getImages() == null || post.getImages().isEmpty()) return List.of();
        return post.getImages().stream()
                .filter(CommunityPostImage::isActive)
                .map(image -> image.getS3Key())
                .collect(Collectors.toList());
    }

    private void applyImageKeys(CommunityPost post, List<String> requestedImageKeys) {
        List<String> normalized = normalizeImageKeys(requestedImageKeys);

        Map<String, CommunityPostImage> activeByKey = new HashMap<>();
        Map<String, CommunityPostImage> deletedByKey = new HashMap<>();
        if (post.getImages() != null) {
            for (CommunityPostImage image : post.getImages()) {
                if (image == null || image.getS3Key() == null) continue;
                if (image.isActive()) {
                    activeByKey.put(image.getS3Key(), image);
                } else {
                    deletedByKey.put(image.getS3Key(), image);
                }
            }
        }

        Set<String> desired = new HashSet<>(normalized);
        if (post.getImages() != null) {
            for (CommunityPostImage image : post.getImages()) {
                if (image == null || !image.isActive()) continue;
                if (!desired.contains(image.getS3Key())) {
                    image.markDeleted();
                }
            }
        }

        List<CommunityPostImage> images = post.getImages();
        if (images == null) {
            images = new ArrayList<>();
            // should never happen because builder default, but keep safe
            throw new IllegalStateException("Post images is null");
        }

        for (int i = 0; i < normalized.size(); i++) {
            String key = normalized.get(i);
            CommunityPostImage existingActive = activeByKey.get(key);
            if (existingActive != null) {
                existingActive.setSortOrder(i);
                continue;
            }

            CommunityPostImage existingDeleted = deletedByKey.get(key);
            if (existingDeleted != null) {
                existingDeleted.restore(i);
                continue;
            }

            images.add(CommunityPostImage.create(post, key, i));
        }
    }

    private List<String> normalizeImageKeys(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) return List.of();
        List<String> result = new ArrayList<>(Math.min(10, imageKeys.size()));
        Set<String> seen = new HashSet<>();
        for (String key : imageKeys) {
            if (key == null) continue;
            String trimmed = key.trim();
            if (trimmed.isBlank()) continue;
            if (seen.add(trimmed)) {
                result.add(trimmed);
            }
            if (result.size() >= 10) break;
        }
        return result;
    }
}
