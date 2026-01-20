package com.runners.app.community.recommend.service;

import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.community.comment.repository.CommunityCommentRepository;
import com.runners.app.community.recommend.entity.CommunityCommentRecommend;
import com.runners.app.community.recommend.entity.CommunityCommentRecommendId;
import com.runners.app.community.recommend.dto.response.CommunityCommentRecommendResponse;
import com.runners.app.community.recommend.repository.CommunityCommentRecommendRepository;
import com.runners.app.global.status.CommunityContentStatus;
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

    @Transactional
    public CommunityCommentRecommendResponse recommend(Long userId, Long postId, Long commentId) {
        CommunityComment comment = communityCommentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (comment.getStatus() == CommunityContentStatus.DELETED || !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var id = new CommunityCommentRecommendId(comment.getId(), user.getId());
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
        CommunityComment comment = communityCommentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (comment.getStatus() == CommunityContentStatus.DELETED || !comment.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var id = new CommunityCommentRecommendId(comment.getId(), user.getId());
        boolean alreadyRecommended = communityCommentRecommendRepository.existsById(id);
        if (!alreadyRecommended) {
            return new CommunityCommentRecommendResponse(postId, comment.getId(), false, comment.getRecommendCount());
        }

        communityCommentRecommendRepository.deleteById(id);
        comment.decreaseRecommendCount();

        return new CommunityCommentRecommendResponse(postId, comment.getId(), false, comment.getRecommendCount());
    }
}
