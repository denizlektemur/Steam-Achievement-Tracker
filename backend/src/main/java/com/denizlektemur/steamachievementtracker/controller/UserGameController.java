package com.denizlektemur.steamachievementtracker.controller;

import com.denizlektemur.steamachievementtracker.dto.UserGameDto;
import com.denizlektemur.steamachievementtracker.model.GameStatus;
import com.denizlektemur.steamachievementtracker.model.UserGame;
import com.denizlektemur.steamachievementtracker.service.UserGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/games")
@RequiredArgsConstructor
public class UserGameController {

    private final UserGameService userGameService;

    @GetMapping
    public List<UserGameDto> getUserGames(@PathVariable Long userId) {
        return userGameService.getGamesWithProgress(userId);
    }

    @GetMapping("/status/{status}")
    public List<UserGameDto> getUserGamesByStatus(
            @PathVariable Long userId,
            @PathVariable GameStatus status) {
        return userGameService.getGamesWithProgressByStatus(userId, status);
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<UserGame> addGame(
            @PathVariable Long userId,
            @PathVariable Long gameId,
            @RequestParam(required = false) GameStatus status) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userGameService.addGameToUser(userId, gameId, status));
    }

    @PatchMapping("/{gameId}/status")
    public UserGame updateStatus(
            @PathVariable Long userId,
            @PathVariable Long gameId,
            @RequestParam GameStatus status) {
        return userGameService.updateStatus(userId, gameId, status);
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> removeGame(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        userGameService.removeGameFromUser(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{gameId}")
    public UserGame getUserGame(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        return userGameService.getUserGameByIds(userId, gameId);
    }
}