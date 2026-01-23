package com.runners.app.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_google_sub", columnNames = "google_sub"),
        @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "google_sub", nullable = false, length = 64)
    private String googleSub; // Google subject (고유 ID)

    @Column(nullable = false, length = 50)
    private String role; // 일단 "USER" 고정

    @Column(length = 100)
    private String name;

    @Column(length = 30)
    private String nickname;

    @Column(length = 30)
    private String intro;

    @Column(length = 500)
    private String picture;

    @Column(name = "custom_picture", length = 500)
    private String customPicture;

    @Column(name = "custom_picture_key", length = 1024)
    private String customPictureKey;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "total_duration_minutes")
    private Long totalDurationMinutes;

    @Column(name = "run_count")
    private Integer runCount;

    public void updateProfile(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateIntro(String intro) {
        this.intro = intro;
    }

    public void updateCustomPicture(String customPicture, String customPictureKey) {
        this.customPicture = customPicture;
        this.customPictureKey = customPictureKey;
    }

    public void clearCustomPicture() {
        this.customPicture = null;
        this.customPictureKey = null;
    }

    public void updateTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public void updateRunningStats(Double totalDistanceKm, Long totalDurationMinutes, Integer runCount) {
        this.totalDistanceKm = totalDistanceKm;
        this.totalDurationMinutes = totalDurationMinutes;
        this.runCount = runCount;
    }

    public String getDisplayName() {
        if (nickname != null && !nickname.isBlank()) return nickname;
        if (name != null && !name.isBlank()) return name;
        return "RUNNERS";
    }
}
