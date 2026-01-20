package com.runners.app.community.recommend.service;

import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.community.recommend.entity.CommunityPostRecommend;
import com.runners.app.community.recommend.entity.CommunityPostRecommendId;
import com.runners.app.community.recommend.dto.response.CommunityPostRecommendResponse;
import com.runners.app.community.recommend.repository.CommunityPostRecommendRepository;
import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityPostRecommendService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostRecommendRepository communityPostRecommendRepository;
    private final UserRepository userRepository;

    public CommunityPostRecommendService(
            CommunityPostRepository communityPostRepository,
            CommunityPostRecommendRepository communityPostRecommendRepository,
            UserRepository userRepository
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityPostRecommendRepository = communityPostRecommendRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CommunityPostRecommendResponse getRecommendStatus(Long userId, Long postId) {
        CommunityPost post = findActivePostOrThrow(postId);
        User user = findUserOrThrow(userId);
        boolean recommended = communityPostRecommendRepository.existsById(new CommunityPostRecommendId(post.getId(), user.getId()));
        return new CommunityPostRecommendResponse(post.getId(), recommended, post.getRecommendCount());
    }

    @Transactional
    public CommunityPostRecommendResponse recommend(Long userId, Long postId) {
        CommunityPost post = findActivePostForUpdateOrThrow(postId);
        User user = findUserOrThrow(userId);

        CommunityPostRecommendId id = new CommunityPostRecommendId(post.getId(), user.getId());
        boolean alreadyRecommended = communityPostRecommendRepository.existsById(id);
        if (alreadyRecommended) {
            return new CommunityPostRecommendResponse(post.getId(), true, post.getRecommendCount());
        }

        communityPostRecommendRepository.save(
                CommunityPostRecommend.builder()
                        .id(id)
                        .post(post)
                        .user(user)
                        .build()
        );
        post.increaseRecommendCount();

        return new CommunityPostRecommendResponse(post.getId(), true, post.getRecommendCount());
    }

    @Transactional
    public CommunityPostRecommendResponse unrecommend(Long userId, Long postId) {
        CommunityPost post = findActivePostForUpdateOrThrow(postId);
        User user = findUserOrThrow(userId);

        CommunityPostRecommendId id = new CommunityPostRecommendId(post.getId(), user.getId());
        boolean alreadyRecommended = communityPostRecommendRepository.existsById(id);
        if (!alreadyRecommended) {
            return new CommunityPostRecommendResponse(post.getId(), false, post.getRecommendCount());
        }

        communityPostRecommendRepository.deleteById(id);
        post.decreaseRecommendCount();

        return new CommunityPostRecommendResponse(post.getId(), false, post.getRecommendCount());
    }

    private void validatePositiveIdOrThrow(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName);
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

    private CommunityPost findActivePostForUpdateOrThrow(Long postId) {
        validatePositiveIdOrThrow(postId, "postId");
        CommunityPost post = communityPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return post;
    }

    private User findUserOrThrow(Long userId) {
        validatePositiveIdOrThrow(userId, "userId");
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
