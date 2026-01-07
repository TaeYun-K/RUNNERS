package com.runners.app.community.post.service;

import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.community.post.dto.CreateCommunityPostRequest;
import com.runners.app.community.post.dto.CreateCommunityPostResponse;
import com.runners.app.community.post.repository.CommunityPostRepository;
import com.runners.app.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    public CommunityPostService(
            CommunityPostRepository communityPostRepository,
            UserRepository userRepository
    ) {
        this.communityPostRepository = communityPostRepository;
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
}

