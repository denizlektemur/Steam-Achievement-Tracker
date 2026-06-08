package com.denizlektemur.steamachievementtracker.service;

import com.denizlektemur.steamachievementtracker.exception.ResourceNotFoundException;
import com.denizlektemur.steamachievementtracker.model.*;
import com.denizlektemur.steamachievementtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;

    public List<UserAchievement> getByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return userAchievementRepository.findByUserId(userId);
    }

    public List<UserAchievement> getByUserAndGame(Long userId, Long gameId) {
        return userAchievementRepository.findByUserIdAndGameId(userId, gameId);
    }

    public Map<String, Integer> getProgress(Long userId, Long gameId) {
        int total = achievementRepository.countByGameId(gameId);
        int unlocked = userAchievementRepository.countByUserIdAndGameId(userId, gameId);
        return Map.of("total", total, "unlocked", unlocked);
    }

    @Transactional
    public UserAchievement unlock(Long userId, Long achievementId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + achievementId));

        UserAchievement ua = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .game(achievement.getGame())
                .unlockedAt(LocalDateTime.now())
                .build();

        return userAchievementRepository.save(ua);
    }

    @Transactional
    public void delete(Long userId, Long achievementId) {
        List<UserAchievement> matches = userAchievementRepository.findByUserId(userId)
                .stream()
                .filter(ua -> ua.getAchievement().getId().equals(achievementId))
                .toList();

        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("No unlock record found for this user and achievement");
        }

        userAchievementRepository.deleteAll(matches);
    }
}