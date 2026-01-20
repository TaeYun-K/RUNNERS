package com.runners.app.user.service;

import com.runners.app.user.dto.UserMeResponse;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return toMeResponse(user);
    }

    @Transactional
    public UserMeResponse updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newNickname = nickname == null ? "" : nickname.trim();
        if (newNickname.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname is required");
        }

        if (Objects.equals(newNickname, user.getNickname())) {
            return toMeResponse(user);
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname already in use");
        }

        user.updateNickname(newNickname);
        userRepository.save(user);
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

    private static UserMeResponse toMeResponse(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPicture(),
                user.getRole(),
                user.getTotalDistanceKm()
        );
    }
}

