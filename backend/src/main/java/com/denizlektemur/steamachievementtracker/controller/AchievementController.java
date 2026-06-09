package com.denizlektemur.steamachievementtracker.controller;

import com.denizlektemur.steamachievementtracker.model.Achievement;
import com.denizlektemur.steamachievementtracker.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public List<Achievement> getByGame(@PathVariable Long gameId) {
        return achievementService.getAchievementsByGame(gameId);
    }

    @GetMapping("/{achievementId}")
    public Achievement getById(@PathVariable Long gameId, @PathVariable Long achievementId) {
        return achievementService.getAchievementById(achievementId);
    }

    @GetMapping("/count")
    public int countByGame(@PathVariable Long gameId) {
        return achievementService.countByGame(gameId);
    }
}