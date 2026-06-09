package com.denizlektemur.steamachievementtracker.controller;

import com.denizlektemur.steamachievementtracker.service.SteamSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SteamSyncController {

    private final SteamSyncService steamSyncService;

    @PostMapping("/users/{userId}/games")
    public ResponseEntity<Map<String, Object>> syncGames(@PathVariable Long userId) {
        int count = steamSyncService.syncGames(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Games synced successfully",
                "newGamesAdded", count
        ));
    }

    @PostMapping("/users/{userId}/games/{gameId}/achievements")
    public ResponseEntity<Map<String, Object>> syncAchievements(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        int count = steamSyncService.syncAchievements(userId, gameId);
        return ResponseEntity.ok(Map.of(
                "message", "Achievements synced successfully",
                "newUnlocksRecorded", count
        ));
    }
}