package com.runners.app.community.post.service;

import com.runners.app.community.CommunityContentStatus;
import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.dto.CreateCommunityPostRequest;
import com.runners.app.community.post.dto.CreateCommunityPostResponse;
import com.runners.app.community.post.dto.CommunityPostDetailResponse;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.community.view.CommunityPostView;
import com.runners.app.community.view.CommunityPostViewId;
import com.runners.app.community.view.CommunityPostViewRepository;
import com.runners.app.user.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            communityPostViewRepository.saveAndFlush(CommunityPostView.builder()
                    .id(viewId)
                    .post(post)
                    .user(viewer)
                    .build());
            post.increaseViewCount();
        } catch (DataIntegrityViolationException ignored) {
            // already viewed today
        }

        var author = post.getAuthor();

        return new CommunityPostDetailResponse(
                post.getId(),
                author.getId(),
                author.getName(),
                author.getPicture(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getRecommendCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
