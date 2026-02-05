package com.runners.app.community.comment.service;

import com.runners.app.community.comment.dto.response.CommunityCommentMutationResponse;
import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.community.comment.dto.request.CreateCommunityCommentRequest;
import com.runners.app.community.comment.dto.response.CommunityCommentResponse;
import com.runners.app.community.comment.dto.response.CommunityCommentCursorListResponse;
import com.runners.app.community.comment.dto.response.DeleteCommunityCommentResponse;
import com.runners.app.community.comment.event.CommentCreatedEvent;
import com.runners.app.community.comment.repository.CommunityCommentRepository;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.community.exception.CommunityDomainException;
import com.runners.app.user.repository.UserRepository;
import com.runners.app.user.service.UserProfileImageResolver;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityCommentService {

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    private final UserProfileImageResolver userProfileImageResolver;
    private final ApplicationEventPublisher eventPublisher;

    public CommunityCommentService(
            CommunityCommentRepository communityCommentRepository,
            CommunityPostRepository communityPostRepository,
            UserRepository userRepository,
            UserProfileImageResolver userProfileImageResolver,
            ApplicationEventPublisher eventPublisher
    ) {
        this.communityCommentRepository = communityCommentRepository;
        this.communityPostRepository = communityPostRepository;
        this.userRepository = userRepository;
        this.userProfileImageResolver = userProfileImageResolver;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CommunityCommentMutationResponse createComment(Long authorId, Long postId, CreateCommunityCommentRequest request) {
        CommunityPost post = findActivePostOrThrow(postId);

        var author = userRepository.findById(authorId)
                .orElseThrow(CommunityDomainException::userNotFound);

        CommunityComment parent = findActiveParentCommentForPostOrThrow(request.parentId(), post);

        // 필요한 ID 값들을 미리 추출 (LAZY 로딩 문제 방지)
        Long postAuthorId = post.getAuthor().getId();
        Long parentCommentId = parent == null ? null : parent.getId();
        Long parentCommentAuthorId = parent == null ? null : parent.getAuthor().getId();

        CommunityComment saved = communityCommentRepository.save(
                CommunityComment.builder()
                        .post(post)
                        .author(author)
                        .parent(parent)
                        .content(request.content())
                        .build()
        );

        post.increaseCommentCount();

        // 트랜잭션 커밋 후 Outbox 적재를 위한 이벤트 발행
        // 트랜잭션이 성공적으로 커밋된 후에만 이벤트 리스너가 실행됨
        eventPublisher.publishEvent(new CommentCreatedEvent(
                saved.getId(),
                post.getId(),
                postAuthorId,
                author.getId(),
                parentCommentId,
                parentCommentAuthorId
        ));

        var comment = new CommunityCommentResponse(
            saved.getId(),
            post.getId(),
            author.getId(),
            author.getDisplayName(),
            userProfileImageResolver.resolve(author),
            author.getTotalDistanceKm(),
            parent == null ? null : parent.getId(),
            saved.getContent(),
            saved.getRecommendCount(),
            saved.getCreatedAt(),
            saved.getUpdatedAt()
        );

        return new CommunityCommentMutationResponse(
            comment,
            post.getCommentCount()
        );
    }

    @Transactional
    public CommunityCommentMutationResponse updateComment(
        Long editorId,
        Long postId,
        Long commentId,
        CreateCommunityCommentRequest request
    ) {
        CommunityPost post = findActivePostOrThrow(postId);
        CommunityComment comment = findActiveCommentOrThrow(commentId);
        validateCommentBelongsToPostOrThrow(comment, post.getId());
        validateAuthorOrThrow(comment, editorId, "No permission to update comment");

        // 내용 변경
        comment.updateContent(request.content());

        var updated = new CommunityCommentResponse(
            comment.getId(),
            post.getId(),
            comment.getAuthor().getId(),
            comment.getAuthor().getDisplayName(),
            userProfileImageResolver.resolve(comment.getAuthor()),
            comment.getAuthor().getTotalDistanceKm(),
            comment.getParent() == null ? null : comment.getParent().getId(),
            comment.getContent(),
            comment.getRecommendCount(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );

        return new CommunityCommentMutationResponse(updated, post.getCommentCount());
    }


    @Transactional
    public DeleteCommunityCommentResponse deleteComment(Long requesterId, Long postId, Long commentId) {
        validatePositiveIdsOrThrow("Invalid postId or commentId", postId, commentId);
        CommunityPost post = findActivePostOrThrow(postId);
        CommunityComment comment = findCommentOrThrow(commentId);
        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw CommunityDomainException.commentNotFound();
        }
        if (comment.getStatus() == CommunityContentStatus.DELETED) {
            return new DeleteCommunityCommentResponse(
                    comment.getId(),
                    post.getId(),
                    post.getCommentCount(),
                    comment.getDeletedAt()
            );
        }
        validateAuthorOrThrow(comment, requesterId, "Forbidden");

        comment.markDeleted();
        post.decreaseCommentCount();

        return new DeleteCommunityCommentResponse(
                comment.getId(),
                post.getId(),
                post.getCommentCount(),
                comment.getDeletedAt()
        );
    }

    @Transactional(readOnly = true)
    public CommunityCommentCursorListResponse listComments(Long postId, String cursor, int size) {
        findActivePostOrThrow(postId);

        int safeSize = Math.min(50, Math.max(1, size));
        Cursor decodedCursor = decodeCursor(cursor);
        int fetchSize = safeSize + 1;

        List<CommunityComment> fetched = communityCommentRepository.findForCursor(
                postId,
                decodedCursor == null ? null : decodedCursor.createdAt(),
                decodedCursor == null ? 0L : decodedCursor.id(),
                org.springframework.data.domain.PageRequest.of(0, fetchSize)
        );

        boolean hasNext = fetched.size() > safeSize;
        List<CommunityComment> pageItems = hasNext ? fetched.subList(0, safeSize) : fetched;

        List<CommunityCommentResponse> comments = pageItems.stream()
                .map(comment -> {
                    boolean isDeleted = comment.getStatus() == CommunityContentStatus.DELETED;
                    String content = isDeleted ? "삭제된 댓글입니다" : comment.getContent();
                     return new CommunityCommentResponse(
                             comment.getId(),
                             postId,
                             comment.getAuthor().getId(),
                             comment.getAuthor().getDisplayName(),
                             userProfileImageResolver.resolve(comment.getAuthor()),
                             comment.getAuthor().getTotalDistanceKm(),
                             comment.getParent() == null ? null : comment.getParent().getId(),
                             content,
                             comment.getRecommendCount(),
                             comment.getCreatedAt(),
                             comment.getUpdatedAt()
                     );
                })
                .toList();

        String nextCursor = null;
        if (hasNext && !pageItems.isEmpty()) {
            CommunityComment last = pageItems.get(pageItems.size() - 1);
            nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
        }

        return new CommunityCommentCursorListResponse(comments, nextCursor);
    }

    private void validatePositiveIdOrThrow(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw CommunityDomainException.invalidId(fieldName);
        }
    }

    private void validatePositiveIdsOrThrow(String message, Long... ids) {
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw CommunityDomainException.badRequest(message);
            }
        }
    }

    private CommunityPost findActivePostOrThrow(Long postId) {
        validatePositiveIdOrThrow(postId, "postId");
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(CommunityDomainException::postNotFound);
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw CommunityDomainException.postNotFound();
        }
        return post;
    }

    private CommunityComment findCommentOrThrow(Long commentId) {
        validatePositiveIdOrThrow(commentId, "commentId");
        return communityCommentRepository.findById(commentId)
                .orElseThrow(CommunityDomainException::commentNotFound);
    }

    private CommunityComment findActiveCommentOrThrow(Long commentId) {
        CommunityComment comment = findCommentOrThrow(commentId);
        if (comment.getStatus() == CommunityContentStatus.DELETED) {
            throw CommunityDomainException.commentNotFound();
        }
        return comment;
    }

    private CommunityComment findActiveParentCommentForPostOrThrow(Long parentId, CommunityPost post) {
        if (parentId == null) return null;

        CommunityComment parent = communityCommentRepository.findById(parentId)
                .orElseThrow(CommunityDomainException::parentCommentNotFound);
        if (parent.getStatus() == CommunityContentStatus.DELETED) {
            throw CommunityDomainException.parentCommentNotFound();
        }
        validateParentCommentBelongsToPostOrThrow(parent, post.getId());
        return parent;
    }

    private void validateCommentBelongsToPostOrThrow(CommunityComment comment, Long postId) {
        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw CommunityDomainException.commentNotInPost();
        }
    }

    private void validateParentCommentBelongsToPostOrThrow(CommunityComment comment, Long postId) {
        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw CommunityDomainException.parentCommentNotInPost();
        }
    }

    private void validateAuthorOrThrow(CommunityComment comment, Long userId, String message) {
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw CommunityDomainException.noPermission(message);
        }
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
}
