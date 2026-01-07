package com.runners.app.community.view;

import com.runners.app.community.post.entity.CommunityPost;
import com.runners.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "community_post_views",
        indexes = {
                @Index(name = "idx_community_post_views_post_id_viewed_date", columnList = "post_id,viewed_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPostView {

    @EmbeddedId
    private CommunityPostViewId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_post_views_post_id"))
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_post_views_user_id"))
    private User user;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null && post != null && user != null && post.getId() != null && user.getId() != null) {
            id = new CommunityPostViewId(post.getId(), user.getId(), LocalDate.now());
        }
        if (viewedAt == null) viewedAt = now;
        if (id != null && id.getViewedDate() == null) {
            id = new CommunityPostViewId(id.getPostId(), id.getUserId(), LocalDate.now());
        }
    }
}
