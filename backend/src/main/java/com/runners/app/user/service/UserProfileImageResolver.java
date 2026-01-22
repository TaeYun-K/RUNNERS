package com.runners.app.user.service;

import com.runners.app.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProfileImageResolver {

    @Value("${app.user.default-profile-image-url:/images/default-profile.svg}")
    private String defaultProfileImageUrl;

    public String resolve(User user) {
        if (user == null) return normalizeDefault(defaultProfileImageUrl);
        String custom = user.getCustomPicture();
        if (custom != null && !custom.isBlank()) return custom;
        return normalizeDefault(defaultProfileImageUrl);
    }

    private String normalizeDefault(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isBlank()) return null;
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
        return null;
    }
}
