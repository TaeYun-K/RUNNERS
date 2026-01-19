package com.runners.app.community.comment.service;

import com.runners.app.community.comment.dto.response.CommunityCommentMutationResponse;
import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.community.comment.dto.request.CreateCommunityCommentRequest;
import com.runners.app.community.comment.dto.response.CommunityCommentResponse;
import com.runners.app.community.comment.dto.response.CommunityCommentCursorListResponse;
import com.runners.app.community.comment.dto.response.DeleteCommunityCommentResponse;
import com.runners.app.community.comment.repository.CommunityCommentRepository;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityCommentService {

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    public CommunityCommentService(
            CommunityCommentRepository communityCommentRepository,
            CommunityPostRepository communityPostRepository,
            UserRepository userRepository
    ) {
        this.communityCommentRepository = communityCommentRepository;
        this.communityPostRepository = communityPostRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommunityCommentMutationResponse createComment(Long authorId, Long postId, CreateCommunityCommentRequest request) {
        CommunityPost post = findActivePostOrThrow(postId);

        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CommunityComment parent = findActiveParentCommentForPostOrThrow(request.parentId(), post);

        CommunityComment saved = communityCommentRepository.save(
                CommunityComment.builder()
                        .post(post)
                        .author(author)
                        .parent(parent)
                        .content(request.content())
                        .build()
        );

        post.increaseCommentCount();

        var comment = new CommunityCommentResponse(
            saved.getId(),
            post.getId(),
            author.getId(),
            author.getDisplayName(),
            author.getPicture(),
            author.getTotalDistanceKm(),
            parent == null ? null : parent.getId(),
            saved.getContent(),
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
        validateCommentBelongsToPostOrThrow(comment, post.getId(), HttpStatus.BAD_REQUEST, "Comment does not belong to the post");
        validateAuthorOrThrow(comment, editorId, HttpStatus.FORBIDDEN, "No permission to update comment");

        // 내용 변경
        comment.updateContent(request.content());

        var updated = new CommunityCommentResponse(
            comment.getId(),
            post.getId(),
            comment.getAuthor().getId(),
            comment.getAuthor().getDisplayName(),
            comment.getAuthor().getPicture(),
            comment.getAuthor().getTotalDistanceKm(),
            comment.getParent() == null ? null : comment.getParent().getId(),
            comment.getContent(),
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
        validateCommentBelongsToPostOrThrow(comment, postId, HttpStatus.NOT_FOUND, "Comment not found");
        if (comment.getStatus() == CommunityContentStatus.DELETED) {
            return new DeleteCommunityCommentResponse(
                    comment.getId(),
                    post.getId(),
                    post.getCommentCount(),
                    comment.getDeletedAt()
            );
        }
        validateAuthorOrThrow(comment, requesterId, HttpStatus.FORBIDDEN, "Forbidden");

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
                decodedCursor == null ? Long.MAX_VALUE : decodedCursor.id(),
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
                            comment.getAuthor().getPicture(),
                            comment.getAuthor().getTotalDistanceKm(),
                            comment.getParent() == null ? null : comment.getParent().getId(),
                            content,
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName);
        }
    }

    private void validatePositiveIdsOrThrow(String message, Long... ids) {
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            }
        }
    }

    private CommunityPost findActivePostOrThrow(Long postId) {
        validatePositiveIdOrThrow(postId, "postId");
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return post;
    }

    private CommunityComment findCommentOrThrow(Long commentId) {
        validatePositiveIdOrThrow(commentId, "commentId");
        return communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    private CommunityComment findActiveCommentOrThrow(Long commentId) {
        CommunityComment comment = findCommentOrThrow(commentId);
        if (comment.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        return comment;
    }

    private CommunityComment findActiveParentCommentForPostOrThrow(Long parentId, CommunityPost post) {
        if (parentId == null) return null;

        CommunityComment parent = communityCommentRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));
        if (parent.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found");
        }
        validateCommentBelongsToPostOrThrow(parent, post.getId(), HttpStatus.BAD_REQUEST, "Parent comment does not belong to the post");
        return parent;
    }

    private void validateCommentBelongsToPostOrThrow(
            CommunityComment comment,
            Long postId,
            HttpStatus status,
            String message
    ) {
        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new ResponseStatusException(status, message);
        }
    }

    private void validateAuthorOrThrow(CommunityComment comment, Long userId, HttpStatus status, String message) {
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new ResponseStatusException(status, message);
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
