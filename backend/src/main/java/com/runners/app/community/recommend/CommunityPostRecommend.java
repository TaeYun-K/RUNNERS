package com.runners.app.community.recommend;

import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "community_post_recommends",
        indexes = {
                @Index(name = "idx_community_post_recommends_user_id_created_at", columnList = "user_id,created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPostRecommend {

    @EmbeddedId
    private CommunityPostRecommendId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_post_recommends_post_id"))
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_post_recommends_user_id"))
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null && post != null && user != null && post.getId() != null && user.getId() != null) {
            id = new CommunityPostRecommendId(post.getId(), user.getId());
        }
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
