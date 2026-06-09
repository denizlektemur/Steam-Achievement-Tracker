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
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.Map;

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

    @Value("${sync.thread-pool-size:10}")
    private int threadPoolSize;

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

        // Get full achievement definitions from schema
        List<SteamAchievementDto> schema = steamApiClient.getGameSchema(game.getAppId());
        if (schema.isEmpty()) {
            log.info("No schema found for game {}", game.getTitle());
            return 0;
        }

        // Get player's unlock status
        List<SteamAchievementDto> playerAchievements =
                steamApiClient.getPlayerAchievements(user.getSteamId(), game.getAppId());

        // Build a map of apiName -> unlock info for quick lookup
        Map<String, SteamAchievementDto> unlockMap = playerAchievements.stream()
                .collect(Collectors.toMap(
                        SteamAchievementDto::resolvedApiName,
                        dto -> dto,
                        (a, b) -> a
                ));

        int synced = 0;

        for (SteamAchievementDto schemaDef : schema) {
            String apiName = schemaDef.resolvedApiName();
            if (apiName == null || apiName.isBlank()) continue;

            // Upsert achievement definition with full info from schema
            Achievement achievement = achievementRepository
                    .findByGameIdAndApiName(gameId, apiName)
                    .orElseGet(() -> Achievement.builder()
                            .game(game)
                            .apiName(apiName)
                            .build());

            // Always update with latest schema data
            achievement.setDisplayName(schemaDef.resolvedName());
            achievement.setDescription(schemaDef.description());
            achievement.setIconUrl(schemaDef.icon());
            achievementRepository.save(achievement);

            // Check if player has unlocked it
            SteamAchievementDto playerData = unlockMap.get(apiName);
            if (playerData != null && playerData.achieved() != null && playerData.achieved() == 1) {
                boolean alreadyRecorded = userAchievementRepository
                        .findByUserId(userId)
                        .stream()
                        .anyMatch(ua -> ua.getAchievement().getId().equals(achievement.getId()));

                if (!alreadyRecorded) {
                    LocalDateTime unlockedAt = playerData.unlockTime() != null && playerData.unlockTime() > 0
                            ? LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(playerData.unlockTime()),
                            ZoneId.systemDefault())
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

        // Stamp sync time
        userGameRepository.findByUserIdAndGameId(userId, gameId)
                .ifPresent(userGameRepository::save);

        log.info("Synced {} new unlocks for {} in {}", synced, user.getUsername(), game.getTitle());
        return synced;
    }
    
    public SyncResult syncAll(Long userId) {
        log.info("Starting full sync for user {}", userId);

        int newGames = syncGames(userId);
        int totalNewUnlocks;

        List<UserGame> userGames = userGameRepository.findByUserId(userId);

        try (ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize)) {

        List<CompletableFuture<Integer>> futures = userGames.stream()
                .map(userGame -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return syncAchievements(userId, userGame.getGame().getId());
                    } catch (Exception e) {
                        log.warn("Skipping {}: {}", userGame.getGame().getTitle(), e.getMessage());
                        return 0;
                    }
                }, executor))
                .toList();

        totalNewUnlocks = futures.stream()
                .mapToInt(f -> {
                    try {
                        return f.join();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();
        }

        log.info("Full sync complete — {} new games, {} new unlocks", newGames, totalNewUnlocks);
        return new SyncResult(newGames, totalNewUnlocks);
    }

    public record SyncResult(int newGames, int newUnlocks) {}
}