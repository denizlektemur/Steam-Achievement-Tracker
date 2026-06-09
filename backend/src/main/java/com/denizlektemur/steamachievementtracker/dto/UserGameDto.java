package com.denizlektemur.steamachievementtracker.dto;

import com.denizlektemur.steamachievementtracker.model.GameStatus;
import java.time.LocalDateTime;

public record UserGameDto(
        Long id,
        Long gameId,
        Integer appId,
        String title,
        String headerImageUrl,
        GameStatus status,
        int totalAchievements,
        int unlockedAchievements,
        LocalDateTime lastPlayedAt,
        LocalDateTime addedAt
) {}