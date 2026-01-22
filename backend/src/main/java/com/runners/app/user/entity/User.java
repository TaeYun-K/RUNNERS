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

    @Column(length = 500)
    private String picture;

    @Column(name = "custom_picture", length = 500)
    private String customPicture;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    public void updateProfile(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateCustomPicture(String customPicture) {
        this.customPicture = customPicture;
    }

    public void updateTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public String getDisplayName() {
        if (nickname != null && !nickname.isBlank()) return nickname;
        if (name != null && !name.isBlank()) return name;
        return "RUNNERS";
    }
}
