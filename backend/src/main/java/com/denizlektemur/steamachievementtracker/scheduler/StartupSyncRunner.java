package com.denizlektemur.steamachievementtracker.scheduler;

import com.denizlektemur.steamachievementtracker.repository.UserRepository;
import com.denizlektemur.steamachievementtracker.service.SteamSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupSyncRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SteamSyncService steamSyncService;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        log.info("Running startup sync for all users...");
        userRepository.findAll().forEach(user -> {
            try {
                SteamSyncService.SyncResult result = steamSyncService.syncAll(user.getId());
                log.info("Startup sync complete for {} — {} new games, {} new unlocks",
                        user.getUsername(), result.newGames(), result.newUnlocks());
            } catch (Exception e) {
                log.error("Startup sync failed for user {}: {}", user.getUsername(), e.getMessage());
            }
        });
    }
}