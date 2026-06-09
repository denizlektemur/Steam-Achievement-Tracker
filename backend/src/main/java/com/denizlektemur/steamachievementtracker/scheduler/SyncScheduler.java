package com.denizlektemur.steamachievementtracker.scheduler;

import com.denizlektemur.steamachievementtracker.repository.UserRepository;
import com.denizlektemur.steamachievementtracker.service.SteamSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {

    private final UserRepository userRepository;
    private final SteamSyncService steamSyncService;

    @Scheduled(fixedRateString = "${sync.rate:PT1H}")
    public void syncAllUsers() {
        log.info("Running scheduled sync...");
        userRepository.findAll().forEach(user -> {
            try {
                SteamSyncService.SyncResult result = steamSyncService.syncAll(user.getId());
                log.info("Scheduled sync complete for {} — {} new games, {} new unlocks",
                        user.getUsername(), result.newGames(), result.newUnlocks());
            } catch (Exception e) {
                log.error("Scheduled sync failed for user {}: {}", user.getUsername(), e.getMessage());
            }
        });
    }
}