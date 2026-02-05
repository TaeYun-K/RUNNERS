package com.runners.app.community.post.service;

import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.entity.CommunityPostBoardType;
import com.runners.app.community.post.entity.CommunityPostImage;
import com.runners.app.community.post.entity.CommunityPostImageStatus;
import com.runners.app.community.post.dto.request.CreateCommunityPostRequest;
import com.runners.app.community.post.dto.response.CommunityPostCountResponse;
import com.runners.app.community.post.dto.response.CommunityPostCursorListResponse;
import com.runners.app.community.post.dto.response.CommunityPostResponse;
import com.runners.app.community.post.dto.response.CommunityPostDetailResponse;
import com.runners.app.community.post.dto.response.CommunityPostSummaryResponse;
import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.community.comment.repository.CommunityCommentRepository;
import com.runners.app.community.post.repository.CommunityPostImageRepository;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.community.upload.service.CommunityUploadService;
import com.runners.app.community.view.CommunityPostViewTracker;
import com.runners.app.community.exception.CommunityDomainException;
import com.runners.app.global.util.CursorUtils;
import com.runners.app.user.repository.UserRepository;
import com.runners.app.user.service.UserProfileImageResolver;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostViewTracker communityPostViewTracker;
    private final UserRepository userRepository;
    private final CommunityUploadService communityUploadService;
    private final CommunityPostImageRepository communityPostImageRepository;
    private final UserProfileImageResolver userProfileImageResolver;

    public CommunityPostService(
            CommunityPostRepository communityPostRepository,
            CommunityCommentRepository communityCommentRepository,
            CommunityPostViewTracker communityPostViewTracker,
            UserRepository userRepository,
            CommunityUploadService communityUploadService,
            CommunityPostImageRepository communityPostImageRepository,
            UserProfileImageResolver userProfileImageResolver
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityCommentRepository = communityCommentRepository;
        this.communityPostViewTracker = communityPostViewTracker;
        this.userRepository = userRepository;
        this.communityUploadService = communityUploadService;
        this.communityPostImageRepository = communityPostImageRepository;
        this.userProfileImageResolver = userProfileImageResolver;
    }

    @Transactional
    public CommunityPostResponse createPost(Long authorId, CreateCommunityPostRequest request) {
        var author = userRepository.findById(authorId)
                .orElseThrow(CommunityDomainException::userNotFound);

        CommunityPost post = CommunityPost.builder()
                .author(author)
                .boardType(request.boardType() == null ? CommunityPostBoardType.FREE : request.boardType())
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
                saved.getBoardType(),
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
            .orElseThrow(CommunityDomainException::userNotFound);

        var post = communityPostRepository.findById(postId)
            .orElseThrow(CommunityDomainException::postNotFound);

        // 작성자 검증
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw CommunityDomainException.noPermission("No permission to update this post");
        }

        post.updateContent(request.title(), request.content());
        post.changeBoardType(request.boardType());
        if (request.imageKeys() != null) {
            applyImageKeys(post, request.imageKeys());
        }

        return new CommunityPostResponse(
            post.getId(),
            author.getId(),
            author.getDisplayName(),
            userProfileImageResolver.resolve(author),
            post.getBoardType(),
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
            .orElseThrow(CommunityDomainException::postNotFound);

        // 작성자 검증
        if (!post.getAuthor().getId().equals(userId)) {
            throw CommunityDomainException.noPermission("No permission");
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
                .orElseThrow(CommunityDomainException::postNotFound);

        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw CommunityDomainException.postNotFound();
        }

        if (viewerId != null) {
            userRepository.findById(viewerId)
                    .orElseThrow(CommunityDomainException::userNotFound);

            boolean isFirstViewToday = communityPostViewTracker.markViewedTodayIfFirst(postId, viewerId);
            if (isFirstViewToday) {
                post.increaseViewCount();
            }
        }

        var author = post.getAuthor();
        return new CommunityPostDetailResponse(
                post.getId(),
                author.getId(),
                author.getDisplayName(),
                userProfileImageResolver.resolve(author),
                author.getTotalDistanceKm(),
                post.getBoardType(),
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
    public CommunityPostCursorListResponse listPosts(CommunityPostBoardType boardType, String cursor, int size) {
        int safeSize = Math.min(50, Math.max(1, size));

        CursorUtils.Cursor decodedCursor = CursorUtils.decodeCursor(cursor);
        int fetchSize = safeSize + 1;

        List<CommunityPost> fetched = communityPostRepository.findForCursor(
                CommunityContentStatus.ACTIVE,
                boardType,
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
                        post.getBoardType(),
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
            nextCursor = CursorUtils.encodeCursor(last.getCreatedAt(), last.getId());
        }

        return new CommunityPostCursorListResponse(posts, nextCursor);
    }

    @Transactional(readOnly = true)
    public CommunityPostCursorListResponse listPostsByAuthor(Long userId, String cursor, int size) {
        int safeSize = Math.min(50, Math.max(1, size));

        CursorUtils.Cursor decodedCursor = CursorUtils.decodeCursor(cursor);
        int fetchSize = safeSize + 1;

        List<CommunityPost> fetched = communityPostRepository.findForCursorByAuthorId(
                CommunityContentStatus.ACTIVE,
                userId,
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
                        post.getBoardType(),
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
            nextCursor = CursorUtils.encodeCursor(last.getCreatedAt(), last.getId());
        }

        return new CommunityPostCursorListResponse(posts, nextCursor);
    }

    @Transactional(readOnly = true)
    public CommunityPostCursorListResponse listPostsCommentedByUser(Long userId, String cursor, int size) {
        int safeSize = Math.min(50, Math.max(1, size));
        int fetchCommentsSize = Math.min(150, safeSize * 3) + 1;

        CursorUtils.Cursor decodedCursor = CursorUtils.decodeCursor(cursor);
        List<CommunityComment> fetched = communityCommentRepository.findByAuthorIdForCursor(
                userId,
                CommunityContentStatus.ACTIVE,
                decodedCursor == null ? null : decodedCursor.createdAt(),
                decodedCursor == null ? Long.MAX_VALUE : decodedCursor.id(),
                PageRequest.of(0, fetchCommentsSize)
        );

        List<Long> orderedPostIds = new ArrayList<>(safeSize);
        Set<Long> seenPostIds = new HashSet<>();
        CommunityComment cursorCommentForNext = null;
        for (CommunityComment comment : fetched) {
            Long postId = comment.getPost().getId();
            if (seenPostIds.add(postId)) {
                orderedPostIds.add(postId);
                cursorCommentForNext = comment;
                if (orderedPostIds.size() >= safeSize) {
                    break;
                }
            }
        }

        if (orderedPostIds.isEmpty()) {
            return new CommunityPostCursorListResponse(List.of(), null);
        }

        boolean hasNext = orderedPostIds.size() >= safeSize || fetched.size() >= fetchCommentsSize;
        String nextCursor = null;
        if (hasNext && cursorCommentForNext != null) {
            nextCursor = CursorUtils.encodeCursor(cursorCommentForNext.getCreatedAt(), cursorCommentForNext.getId());
        }

        List<CommunityPost> postsById = communityPostRepository.findAllByIdInWithAuthor(
                CommunityContentStatus.ACTIVE,
                null,
                orderedPostIds
        );
        Map<Long, Integer> orderByIndex = new HashMap<>(orderedPostIds.size());
        for (int i = 0; i < orderedPostIds.size(); i++) {
            orderByIndex.put(orderedPostIds.get(i), i);
        }
        List<CommunityPost> orderedPosts = new ArrayList<>(postsById);
        orderedPosts.sort(Comparator.comparingInt(p -> orderByIndex.getOrDefault(p.getId(), Integer.MAX_VALUE)));

        Map<Long, String> thumbnailUrlByPostId = buildThumbnailUrlByPostId(orderedPosts);
        var posts = orderedPosts.stream()
                .map(post -> new CommunityPostSummaryResponse(
                        post.getId(),
                        post.getAuthor().getId(),
                        post.getAuthor().getDisplayName(),
                        userProfileImageResolver.resolve(post.getAuthor()),
                        post.getAuthor().getTotalDistanceKm(),
                        post.getBoardType(),
                        post.getTitle(),
                        toContentPreview(post.getContent()),
                        thumbnailUrlByPostId.get(post.getId()),
                        post.getViewCount(),
                        post.getRecommendCount(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new CommunityPostCursorListResponse(posts, nextCursor);
    }

    @Transactional(readOnly = true)
    public CommunityPostCountResponse countPostsByAuthor(Long userId) {
        long count = communityPostRepository.countByAuthorIdAndStatus(userId, CommunityContentStatus.ACTIVE);
        return new CommunityPostCountResponse(count);
    }

    @Transactional(readOnly = true)
    public CommunityPostCountResponse countPostsCommentedByUser(Long userId) {
        long count = communityCommentRepository.countDistinctPostIdsByAuthorIdAndStatus(userId, CommunityContentStatus.ACTIVE);
        return new CommunityPostCountResponse(count);
    }

    @Transactional(readOnly = true)
    public CommunityPostCursorListResponse searchPosts(String query, CommunityPostBoardType boardType, String cursor, int size) {
        if (query == null || query.isBlank()) {
            throw CommunityDomainException.queryRequired();
        }

        int safeSize = Math.min(50, Math.max(1, size));
        String trimmedQuery = query.trim();
        String booleanQuery = toBooleanModePrefixQuery(trimmedQuery);

        CursorUtils.Cursor decodedCursor = CursorUtils.decodeCursor(cursor);
        int fetchSize = safeSize + 1;

        List<Long> fetchedIds = communityPostRepository.searchPostIdsForCursor(
                CommunityContentStatus.ACTIVE.name(),
                CommunityContentStatus.ACTIVE.name(),
                boardType == null ? null : boardType.name(),
                booleanQuery,
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
                boardType,
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
                        post.getBoardType(),
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
            nextCursor = CursorUtils.encodeCursor(last.getCreatedAt(), last.getId());
        }

        return new CommunityPostCursorListResponse(posts, nextCursor);
    }

    private String toBooleanModePrefixQuery(String rawQuery) {
        String trimmed = rawQuery == null ? "" : rawQuery.trim();
        if (trimmed.isEmpty()) return trimmed;

        String[] tokens = trimmed.split("\\s+");
        StringBuilder sb = new StringBuilder(trimmed.length() + tokens.length);
        for (String token : tokens) {
            if (token.isBlank()) continue;

            String t = token.trim();
            boolean alreadyBooleanSyntax =
                    t.contains("*") ||
                    t.contains("\"") ||
                    t.startsWith("+") ||
                    t.startsWith("-") ||
                    t.startsWith("~") ||
                    t.startsWith(">") ||
                    t.startsWith("<") ||
                    t.startsWith("(") ||
                    t.startsWith("@");

            if (sb.length() > 0) sb.append(' ');
            sb.append(alreadyBooleanSyntax ? t : (t + "*"));
        }

        return sb.toString();
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
