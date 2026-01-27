package com.runners.app.user.service;

import com.runners.app.user.dto.UserMeResponse;
import com.runners.app.user.dto.UserPublicProfileResponse;
import com.runners.app.user.entity.User;
import com.runners.app.user.exception.UserDomainException;
import com.runners.app.user.repository.UserRepository;
import com.runners.app.community.upload.service.CommunityUploadService;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(UserDomainException::userNotFound);

        return toMeResponse(user);
    }

    @Transactional(readOnly = true)
    public UserPublicProfileResponse getPublicProfile(Long userId) {
        if (userId == null || userId <= 0L) {
            throw UserDomainException.userIdInvalid();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserDomainException::userNotFound);

        return new UserPublicProfileResponse(
                user.getId(),
                user.getDisplayName(),
                user.getNickname(),
                user.getIntro(),
                userProfileImageResolver.resolve(user),
                user.getTotalDistanceKm(),
                user.getTotalDurationMinutes(),
                user.getRunCount()
        );
    }

    @Transactional
    public UserMeResponse updateProfile(Long userId, String nickname, String intro) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserDomainException::userNotFound);

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
                .orElseThrow(UserDomainException::userNotFound);

        if (Objects.equals(user.getTotalDistanceKm(), totalDistanceKm)) {
            return toMeResponse(user);
        }

        user.updateTotalDistanceKm(totalDistanceKm);
        userRepository.save(user);
        return toMeResponse(user);
    }

    @Transactional
    public UserMeResponse updateRunningStats(Long userId, Double totalDistanceKm, Long totalDurationMinutes, Integer runCount) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserDomainException::userNotFound);

        boolean same =
                Objects.equals(user.getTotalDistanceKm(), totalDistanceKm)
                        && Objects.equals(user.getTotalDurationMinutes(), totalDurationMinutes)
                        && Objects.equals(user.getRunCount(), runCount);
        if (same) {
            return toMeResponse(user);
        }

        user.updateRunningStats(totalDistanceKm, totalDurationMinutes, runCount);
        userRepository.save(user);
        return toMeResponse(user);
    }

    @Transactional
    public UserMeResponse updateProfileImage(Long userId, String key) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserDomainException::userNotFound);

        String trimmedKey = key == null ? "" : key.trim();
        if (trimmedKey.isBlank()) {
            throw UserDomainException.profileImageKeyRequired();
        }

        if (!communityUploadService.isUserProfileImageKey(userId, trimmedKey)) {
            throw UserDomainException.profileImageKeyInvalid();
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
                .orElseThrow(UserDomainException::userNotFound);

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
            throw UserDomainException.nicknameRequired();
        }

        if (Objects.equals(newNickname, user.getNickname())) {
            return false;
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw UserDomainException.nicknameDuplicated();
        }

        user.updateNickname(newNickname);
        return true;
    }
}
