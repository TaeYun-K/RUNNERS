package com.runners.app.notification.service;

import com.runners.app.notification.entity.DeviceToken;
import com.runners.app.notification.repository.DeviceTokenRepository;
import com.runners.app.user.entity.User;
import com.runners.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    /**
     * FCM 토큰 등록/업데이트
     * 
     * 동일한 토큰이 다른 사용자에게 등록되어 있으면 해당 레코드를 삭제하고 새로 등록
     * 동일한 사용자가 동일한 토큰을 다시 등록하면 기존 토큰 반환 (deviceId, platform은 새 값으로 재등록)
     * 
     * @param userId 사용자 ID
     * @param token FCM 토큰
     * @param deviceId 기기 식별자 (선택사항)
     * @param platform 플랫폼 ("android" 등, 선택사항)
     * @return 등록/업데이트된 DeviceToken
     */
    @Transactional
    public DeviceToken registerToken(Long userId, String token, String deviceId, String platform) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 동일한 토큰이 이미 존재하는지 확인
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(token);

        if (existingToken.isPresent()) {
            DeviceToken deviceToken = existingToken.get();
            
            // 동일한 사용자의 토큰이면 기존 토큰 반환
            // (deviceId, platform 업데이트가 필요하면 삭제 후 재등록)
            if (deviceToken.getUser().getId().equals(userId)) {
                // deviceId나 platform이 변경되었는지 확인
                boolean needsUpdate = (deviceId != null && !deviceId.equals(deviceToken.getDeviceId())) ||
                                     (platform != null && !platform.equals(deviceToken.getPlatform()));
                
                if (needsUpdate) {
                    // 업데이트가 필요한 경우 삭제 후 재등록
                    log.debug("Updating device token metadata for user: {}", userId);
                    deviceTokenRepository.delete(deviceToken);
                } else {
                    // 변경사항이 없으면 기존 토큰 반환
                    log.debug("Device token already registered for user: {}", userId);
                    return deviceToken;
                }
            } else {
                // 다른 사용자의 토큰이면 삭제하고 새로 등록
                log.info("Token already registered to another user. Removing old registration and creating new one.");
                deviceTokenRepository.delete(deviceToken);
            }
        }

        // 새 토큰 등록
        try {
            DeviceToken newToken = DeviceToken.builder()
                    .user(user)
                    .token(token)
                    .deviceId(deviceId)
                    .platform(platform)
                    .build();

            return deviceTokenRepository.save(newToken);
        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 인한 중복 등록 시도 시, 기존 토큰 조회 후 반환
            log.warn("Concurrent token registration detected, retrieving existing token");
            return deviceTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Failed to register device token", e));
        }
    }

    /**
     * FCM 토큰 삭제 (로그아웃 시)
     * 
     * @param userId 사용자 ID
     * @param token 삭제할 FCM 토큰
     */
    @Transactional
    public void removeToken(Long userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
        log.debug("Removed device token for user: {}", userId);
    }

    /**
     * 사용자의 모든 FCM 토큰 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자의 모든 DeviceToken 목록
     */
    @Transactional(readOnly = true)
    public List<DeviceToken> getTokensByUserId(Long userId) {
        return deviceTokenRepository.findByUserId(userId);
    }
}
