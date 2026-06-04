package com.denizlektemur.steamachievementtracker.controller;

import com.denizlektemur.steamachievementtracker.model.UserAchievement;
import com.denizlektemur.steamachievementtracker.service.UserAchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class UserAchievementController {

    private final UserAchievementService userAchievementService;

    @GetMapping("/achievements")
    public List<UserAchievement> getAllByUser(@PathVariable Long userId) {
        return userAchievementService.getByUser(userId);
    }

    @GetMapping("/games/{gameId}/achievements")
    public List<UserAchievement> getByUserAndGame(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        return userAchievementService.getByUserAndGame(userId, gameId);
    }

    @GetMapping("/games/{gameId}/achievements/progress")
    public Map<String, Integer> getProgress(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        return userAchievementService.getProgress(userId, gameId);
    }

    @PostMapping("/achievements/{achievementId}/unlock")
    public ResponseEntity<UserAchievement> unlock(
            @PathVariable Long userId,
            @PathVariable Long achievementId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userAchievementService.unlock(userId, achievementId));
    }

    @DeleteMapping("/achievements/{achievementId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long achievementId) {
        userAchievementService.delete(userId, achievementId);
        return ResponseEntity.noContent().build();
    }
}