package com.runners.app.community.recommend.service;

import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.community.recommend.entity.CommunityPostRecommend;
import com.runners.app.community.recommend.entity.CommunityPostRecommendId;
import com.runners.app.community.recommend.dto.response.CommunityPostRecommendResponse;
import com.runners.app.community.recommend.repository.CommunityPostRecommendRepository;
import com.runners.app.global.status.CommunityContentStatus;
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

    @Transactional
    public CommunityPostRecommendResponse recommend(Long userId, Long postId) {
        CommunityPost post = communityPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var id = new CommunityPostRecommendId(post.getId(), user.getId());
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
        CommunityPost post = communityPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getStatus() == CommunityContentStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var id = new CommunityPostRecommendId(post.getId(), user.getId());
        boolean alreadyRecommended = communityPostRecommendRepository.existsById(id);
        if (!alreadyRecommended) {
            return new CommunityPostRecommendResponse(post.getId(), false, post.getRecommendCount());
        }

        communityPostRecommendRepository.deleteById(id);
        post.decreaseRecommendCount();

        return new CommunityPostRecommendResponse(post.getId(), false, post.getRecommendCount());
    }
}
