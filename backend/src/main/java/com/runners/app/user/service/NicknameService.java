package com.runners.app.user.service;

import com.runners.app.user.repository.UserRepository;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class NicknameService {

    private static final SecureRandom random = new SecureRandom();

    private static final String[] adjectives = {
            "달리는", "행복한", "용감한", "평화로운", "호기심많은",
            "튼튼한", "성실한", "멋진", "빛나는", "따뜻한"
    };

    private static final String[] nouns = {
            "러너", "토끼", "여우", "고양이", "강아지",
            "호랑이", "사자", "돌고래", "펭귄", "수달"
    };

    private final UserRepository userRepository;

    public NicknameService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUniqueNickname() {
        for (int i = 0; i < 30; i++) {
            String nickname = generateCandidate();
            if (!userRepository.existsByNickname(nickname)) return nickname;
        }
        return "RUNNERS" + (100000 + random.nextInt(900000));
    }

    private static String generateCandidate() {
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        int suffix = random.nextInt(10000);
        return adjective + noun + String.format("%04d", suffix);
    }
}

