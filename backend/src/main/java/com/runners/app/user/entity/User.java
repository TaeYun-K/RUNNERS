package com.runners.app.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_google_sub", columnNames = "google_sub")
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

    @Column(length = 500)
    private String picture;

    public void updateProfile(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }
}
