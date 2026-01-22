package com.runners.app.community.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "community_post_images",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_community_post_images_post_id_sort_order", columnNames = {"post_id", "sort_order"})
        },
        indexes = {
                @Index(name = "idx_community_post_images_post_id_sort_order", columnList = "post_id,sort_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_community_post_images_post_id"))
    private CommunityPost post;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public String getS3Key() {
        return s3Key;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public static CommunityPostImage create(CommunityPost post, String s3Key, int sortOrder) {
        return CommunityPostImage.builder()
                .post(post)
                .s3Key(s3Key)
                .sortOrder(sortOrder)
                .build();
    }
}
