package com.runners.app.community.post.entity;

import com.runners.app.global.status.CommunityContentStatus;
import com.runners.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "community_posts",
        indexes = {
                @Index(name = "idx_community_posts_created_at", columnList = "created_at"),
                @Index(name = "idx_community_posts_board_type_created_at", columnList = "board_type,created_at"),
                @Index(name = "idx_community_posts_author_id_created_at", columnList = "author_id,created_at"),
                @Index(name = "idx_community_posts_recommend_count_created_at", columnList = "recommend_count,created_at"),
                @Index(name = "idx_community_posts_view_count_created_at", columnList = "view_count,created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_posts_author_id"))
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityContentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false, length = 20)
    private CommunityPostBoardType boardType;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "recommend_count", nullable = false)
    private int recommendCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<CommunityPostImage> images = new ArrayList<>();

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) status = CommunityContentStatus.ACTIVE;
        if (boardType == null) boardType = CommunityPostBoardType.FREE;
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    public void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
        touchUpdatedAt();
    }

    public CommunityPostBoardType getBoardType() {
        return boardType == null ? CommunityPostBoardType.FREE : boardType;
    }

    public void changeBoardType(CommunityPostBoardType boardType) {
        if (boardType == null) return;
        this.boardType = boardType;
        touchUpdatedAt();
    }

    public void markDeleted() {
        status = CommunityContentStatus.DELETED;
        deletedAt = LocalDateTime.now();
        touchUpdatedAt();
    }

    public void increaseViewCount() {
        viewCount++;
    }

    public void increaseRecommendCount() {
        recommendCount++;
    }

    public void decreaseRecommendCount() {
        recommendCount = Math.max(0, recommendCount - 1);
    }

    public void increaseCommentCount() {
        commentCount++;
    }

    public void decreaseCommentCount() {
        commentCount = Math.max(0, commentCount - 1);
    }
}
