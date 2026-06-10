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
                UserGame userGame = UserGame.builder()
                        .user(user)
                        .game(game)
                        .status(GameStatus.BACKLOG)
                        .build();

                if (dto.lastPlayedTimestamp() != null && dto.lastPlayedTimestamp() > 0) {
                    userGame.setLastPlayedAt(LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(dto.lastPlayedTimestamp()),
                            ZoneId.systemDefault()));
                }

                userGameRepository.save(userGame);
                synced++;
            } else {
                // Update last played even if game already exists
                userGameRepository.findByUserIdAndGameId(userId, game.getId())
                        .ifPresent(ug -> {
                            if (dto.lastPlayedTimestamp() != null && dto.lastPlayedTimestamp() > 0) {
                                ug.setLastPlayedAt(LocalDateTime.ofInstant(
                                        Instant.ofEpochSecond(dto.lastPlayedTimestamp()),
                                        ZoneId.systemDefault()));
                                userGameRepository.save(ug);
                            }
                        });
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

        List<SteamAchievementDto> schema = steamApiClient.getGameSchema(game.getAppId());

        // Handle no achievements — auto ignore before early return
        if (schema.isEmpty()) {
            userGameRepository.findByUserIdAndGameId(userId, gameId).ifPresent(ug -> {
                if (ug.getStatus() != GameStatus.IGNORED) {
                    ug.setStatus(GameStatus.IGNORED);
                    userGameRepository.save(ug);
                    log.info("Auto-ignoring {} for user {} — no achievements found",
                            game.getTitle(), user.getUsername());
                }
            });
            return 0;
        }

        List<SteamAchievementDto> playerAchievements =
                steamApiClient.getPlayerAchievements(user.getSteamId(), game.getAppId());

        // Fetch global percentages
        Map<String, Double> globalPercentages =
                steamApiClient.getGlobalAchievementPercentages(game.getAppId());

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

            Achievement achievement = achievementRepository
                    .findByGameIdAndApiName(gameId, apiName)
                    .orElseGet(() -> Achievement.builder()
                            .game(game)
                            .apiName(apiName)
                            .build());

            achievement.setDisplayName(schemaDef.resolvedName());
            achievement.setDescription(schemaDef.description());
            achievement.setIconUrl(schemaDef.icon());
            achievement.setGlobalPercentage(globalPercentages.getOrDefault(apiName, null));
            achievementRepository.save(achievement);

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

        // Auto-update game status based on completion
        userGameRepository.findByUserIdAndGameId(userId, gameId).ifPresent(ug -> {
            int total = achievementRepository.countByGameId(gameId);
            int unlocked = userAchievementRepository.countByUserIdAndGameId(userId, gameId);

            if (total == 0 && ug.getStatus() != GameStatus.IGNORED) {
                // No achievements — auto ignore
                ug.setStatus(GameStatus.IGNORED);
                log.info("Auto-ignoring {} for user {} — no achievements",
                        game.getTitle(), user.getUsername());
            } else if (total > 0 && ug.getStatus() != GameStatus.IGNORED) {
                GameStatus currentStatus = ug.getStatus();
                GameStatus newStatus = currentStatus;

                if (unlocked == total) {
                    if (currentStatus != GameStatus.COMPLETED) {
                        newStatus = GameStatus.COMPLETED;
                        log.info("Auto-completing {} for user {} ({}/{})",
                                game.getTitle(), user.getUsername(), unlocked, total);
                    }
                } else if (currentStatus == GameStatus.COMPLETED && unlocked < total) {
                    newStatus = GameStatus.BACKLOG;
                    log.info("Reverting {} to backlog for user {} — new achievements added ({}/{})",
                            game.getTitle(), user.getUsername(), unlocked, total);
                }

                ug.setStatus(newStatus);
            }

            userGameRepository.save(ug);
        });

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