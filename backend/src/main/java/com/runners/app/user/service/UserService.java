package com.runners.app.user.service;

import com.runners.app.user.dto.UserMeResponse;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import com.runners.app.community.upload.service.CommunityUploadService;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CommunityUploadService communityUploadService;
    private final UserProfileImageResolver userProfileImageResolver;

    public UserService(
            UserRepository userRepository,
            CommunityUploadService communityUploadService,
            UserProfileImageResolver userProfileImageResolver
    ) {
        this.userRepository = userRepository;
        this.communityUploadService = communityUploadService;
        this.userProfileImageResolver = userProfileImageResolver;
    }

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return toMeResponse(user);
    }

    @Transactional
    public UserMeResponse updateProfile(Long userId, String nickname, String intro) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean changed = false;
        if (nickname != null) {
            changed |= applyNicknameUpdate(user, nickname);
        }
        if (intro != null) {
            String trimmedIntro = intro.trim();
            String normalizedIntro = trimmedIntro.isBlank() ? null : trimmedIntro;
            if (!Objects.equals(normalizedIntro, user.getIntro())) {
                user.updateIntro(normalizedIntro);
                changed = true;
            }
        }

        if (changed) {
            userRepository.save(user);
        }
        return toMeResponse(user);
    }

    @Transactional
    public UserMeResponse updateTotalDistanceKm(Long userId, Double totalDistanceKm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (Objects.equals(user.getTotalDistanceKm(), totalDistanceKm)) {
            return toMeResponse(user);
        }

        user.updateTotalDistanceKm(totalDistanceKm);
        userRepository.save(user);
        return toMeResponse(user);
    }

    @Transactional
    public UserMeResponse updateProfileImage(Long userId, String key) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String trimmedKey = key == null ? "" : key.trim();
        if (trimmedKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key is required");
        }

        if (!communityUploadService.isUserProfileImageKey(userId, trimmedKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid key");
        }

        String prevKey = user.getCustomPictureKey();
        if (prevKey != null && !prevKey.isBlank() && !prevKey.equals(trimmedKey)) {
            communityUploadService.deleteUserProfileImageObject(userId, prevKey);
        }

        String url = communityUploadService.toPublicFileUrl(trimmedKey);
        user.updateCustomPicture(url, trimmedKey);
        userRepository.save(user);
        return toMeResponse(user);
    }

    @Transactional
    public UserMeResponse deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String prevKey = user.getCustomPictureKey();
        if (prevKey != null && !prevKey.isBlank()) {
            communityUploadService.deleteUserProfileImageObject(userId, prevKey);
        }

        user.clearCustomPicture();
        userRepository.save(user);
        return toMeResponse(user);
    }

    private UserMeResponse toMeResponse(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getIntro(),
                userProfileImageResolver.resolve(user),
                user.getRole(),
                user.getTotalDistanceKm()
        );
    }

    private boolean applyNicknameUpdate(User user, String nickname) {
        String newNickname = nickname == null ? "" : nickname.trim();
        if (newNickname.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname is required");
        }

        if (Objects.equals(newNickname, user.getNickname())) {
            return false;
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname already in use");
        }

        user.updateNickname(newNickname);
        return true;
    }
}
