package com.runners.app.community.recommend.entity;

import com.runners.app.community.comment.entity.CommunityComment;
import com.runners.app.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "community_comment_recommends",
        indexes = {
                @Index(name = "idx_community_comment_recommends_user_id_created_at", columnList = "user_id,created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityCommentRecommend {

    @EmbeddedId
    private CommunityCommentRecommendId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_comment_recommends_comment_id"))
    private CommunityComment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_comment_recommends_user_id"))
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null && comment != null && user != null && comment.getId() != null && user.getId() != null) {
            id = new CommunityCommentRecommendId(comment.getId(), user.getId());
        }
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}

