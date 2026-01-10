package com.runners.app.community.comment.service;

import com.runners.app.community.CommunityContentStatus;
import com.runners.app.community.comment.CommunityComment;
import com.runners.app.community.comment.dto.request.CreateCommunityCommentRequest;
import com.runners.app.community.comment.dto.response.CreateCommunityCommentResponse;
import com.runners.app.community.comment.dto.response.CommunityCommentCursorListResponse;
import com.runners.app.community.comment.dto.response.CommunityCommentItemResponse;
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
    public CreateCommunityCommentResponse createComment(Long authorId, Long postId, CreateCommunityCommentRequest request) {
        if (postId == null || postId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid postId");
        }

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CommunityComment parent = null;
        if (request.parentId() != null) {
            parent = communityCommentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));

            if (parent.getStatus() == CommunityContentStatus.DELETED) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found");
            }
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment does not belong to the post");
            }
        }

        CommunityComment saved = communityCommentRepository.save(
                CommunityComment.builder()
                        .post(post)
                        .author(author)
                        .parent(parent)
                        .content(request.content())
                        .build()
        );

        post.increaseCommentCount();

        return new CreateCommunityCommentResponse(
                saved.getId(),
                post.getId(),
                author.getId(),
                parent == null ? null : parent.getId(),
                saved.getContent(),
                post.getCommentCount(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public DeleteCommunityCommentResponse deleteComment(Long requesterId, Long postId, Long commentId) {
        if (postId == null || postId <= 0 || commentId == null || commentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid postId or commentId");
        }

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        CommunityComment comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!Objects.equals(comment.getPost().getId(), postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        if (comment.getStatus() == CommunityContentStatus.DELETED) {
            return new DeleteCommunityCommentResponse(
                    comment.getId(),
                    post.getId(),
                    post.getCommentCount(),
                    comment.getDeletedAt()
            );
        }
        if (!Objects.equals(comment.getAuthor().getId(), requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

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
        if (postId == null || postId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid postId");
        }

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

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

        List<CommunityCommentItemResponse> comments = pageItems.stream()
                .map(comment -> {
                    boolean isDeleted = comment.getStatus() == CommunityContentStatus.DELETED;
                    String content = isDeleted ? "삭제된 댓글입니다" : comment.getContent();
                    return new CommunityCommentItemResponse(
                            comment.getId(),
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
