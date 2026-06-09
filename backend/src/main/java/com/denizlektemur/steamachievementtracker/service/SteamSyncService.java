package com.denizlektemur.steamachievementtracker.service;

import com.denizlektemur.steamachievementtracker.client.SteamApiClient;
import com.denizlektemur.steamachievementtracker.dto.SteamAchievementDto;
import com.denizlektemur.steamachievementtracker.dto.SteamGameDto;
import com.denizlektemur.steamachievementtracker.exception.ResourceNotFoundException;
import com.denizlektemur.steamachievementtracker.model.*;
import com.denizlektemur.steamachievementtracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamSyncService {

    private final SteamApiClient steamApiClient;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    @Transactional
    public int syncGames(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<SteamGameDto> steamGames = steamApiClient.getOwnedGames(user.getSteamId());
        log.info("Syncing {} games for user {}", steamGames.size(), user.getUsername());

        int synced = 0;
        for (SteamGameDto dto : steamGames) {
            // Upsert game
            Game game = gameRepository.findByAppId(dto.appId())
                    .orElseGet(() -> gameRepository.save(
                            Game.builder()
                                    .appId(dto.appId())
                                    .title(dto.name())
                                    .build()
                    ));

            // Add to user library if not already there
            if (!userGameRepository.existsByUserIdAndGameId(userId, game.getId())) {
                userGameRepository.save(
                        UserGame.builder()
                                .user(user)
                                .game(game)
                                .status(GameStatus.BACKLOG)
                                .build()
                );
                synced++;
            }
        }

        return synced;
    }

    @Transactional
    public int syncAchievements(Long userId, Long gameId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));

        List<SteamAchievementDto> steamAchievements =
                steamApiClient.getPlayerAchievements(user.getSteamId(), game.getAppId());

        if (steamAchievements.isEmpty()) {
            log.info("No achievements found for game {}", game.getTitle());
            return 0;
        }

        int synced = 0;
        for (SteamAchievementDto dto : steamAchievements) {
            // Upsert achievement definition
            Achievement achievement = achievementRepository
                    .findByGameIdAndApiName(gameId, dto.apiName())
                    .orElseGet(() -> achievementRepository.save(
                            Achievement.builder()
                                    .game(game)
                                    .apiName(dto.apiName())
                                    .displayName(dto.name())
                                    .description(dto.description())
                                    .build()
                    ));

            // If unlocked and not already recorded, save it
            if (dto.achieved() == 1) {
                boolean alreadyRecorded = userAchievementRepository
                        .findByUserId(userId)
                        .stream()
                        .anyMatch(ua -> ua.getAchievement().getId().equals(achievement.getId()));

                if (!alreadyRecorded) {
                    LocalDateTime unlockedAt = dto.unlockTime() > 0
                            ? LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(dto.unlockTime()), ZoneId.systemDefault())
                            : LocalDateTime.now();

                    userAchievementRepository.save(
                            UserAchievement.builder()
                                    .user(user)
                                    .achievement(achievement)
                                    .game(game)
                                    .unlockedAt(unlockedAt)
                                    .build()
                    );
                    synced++;
                }
            }
        }

        log.info("Synced {} new unlocks for {} in {}", synced, user.getUsername(), game.getTitle());
        return synced;
    }
}