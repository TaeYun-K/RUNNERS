package com.runners.app.community.recommend.service;

import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.community.comment.repository.CommunityCommentRepository;
import com.runners.app.community.recommend.entity.CommunityCommentRecommend;
import com.runners.app.community.recommend.entity.CommunityCommentRecommendId;
import com.runners.app.community.recommend.dto.response.CommunityCommentRecommendResponse;
import com.runners.app.community.recommend.repository.CommunityCommentRecommendRepository;
import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityCommentRecommendService {

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityCommentRecommendRepository communityCommentRecommendRepository;
    private final UserRepository userRepository;

    public CommunityCommentRecommendService(
            CommunityCommentRepository communityCommentRepository,
            CommunityCommentRecommendRepository communityCommentRecommendRepository,
            UserRepository userRepository
    ) {
        this.communityCommentRepository = communityCommentRepository;
        this.communityCommentRecommendRepository = communityCommentRecommendRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CommunityCommentRecommendResponse getRecommendStatus(Long userId, Long postId, Long commentId) {
        CommunityComment comment = findActiveCommentForPostOrThrow(postId, commentId);
        User user = findUserOrThrow(userId);
        boolean recommended = communityCommentRecommendRepository.existsById(new CommunityCommentRecommendId(comment.getId(), user.getId()));
        return new CommunityCommentRecommendResponse(postId, comment.getId(), recommended, comment.getRecommendCount());
    }

    @Transactional
    public CommunityCommentRecommendResponse recommend(Long userId, Long postId, Long commentId) {
        CommunityComment comment = findActiveCommentForUpdateForPostOrThrow(postId, commentId);
        User user = findUserOrThrow(userId);

        CommunityCommentRecommendId id = new CommunityCommentRecommendId(comment.getId(), user.getId());
        boolean alreadyRecommended = communityCommentRecommendRepository.existsById(id);
        if (alreadyRecommended) {
            return new CommunityCommentRecommendResponse(postId, comment.getId(), true, comment.getRecommendCount());
        }

        communityCommentRecommendRepository.save(
                CommunityCommentRecommend.builder()
                        .id(id)
                        .comment(comment)
                        .user(user)
                        .build()
        );
        comment.increaseRecommendCount();

        return new CommunityCommentRecommendResponse(postId, comment.getId(), true, comment.getRecommendCount());
    }

    @Transactional
    public CommunityCommentRecommendResponse unrecommend(Long userId, Long postId, Long commentId) {
        CommunityComment comment = findActiveCommentForUpdateForPostOrThrow(postId, commentId);
        User user = findUserOrThrow(userId);

        CommunityCommentRecommendId id = new CommunityCommentRecommendId(comment.getId(), user.getId());
        boolean alreadyRecommended = communityCommentRecommendRepository.existsById(id);
        if (!alreadyRecommended) {
            return new CommunityCommentRecommendResponse(postId, comment.getId(), false, comment.getRecommendCount());
        }

        communityCommentRecommendRepository.deleteById(id);
        comment.decreaseRecommendCount();

        return new CommunityCommentRecommendResponse(postId, comment.getId(), false, comment.getRecommendCount());
    }

    private void validatePositiveIdOrThrow(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName);
        }
    }

    private CommunityComment findActiveCommentForPostOrThrow(Long postId, Long commentId) {
        validatePositiveIdOrThrow(postId, "postId");
        validatePositiveIdOrThrow(commentId, "commentId");
        CommunityComment comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (comment.getStatus() == CommunityContentStatus.DELETED || !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        return comment;
    }

    private CommunityComment findActiveCommentForUpdateForPostOrThrow(Long postId, Long commentId) {
        validatePositiveIdOrThrow(postId, "postId");
        validatePositiveIdOrThrow(commentId, "commentId");
        CommunityComment comment = communityCommentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (comment.getStatus() == CommunityContentStatus.DELETED || !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        return comment;
    }

    private User findUserOrThrow(Long userId) {
        validatePositiveIdOrThrow(userId, "userId");
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
