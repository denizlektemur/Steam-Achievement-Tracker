package com.denizlektemur.steamachievementtracker.service;

import com.denizlektemur.steamachievementtracker.model.*;
import com.denizlektemur.steamachievementtracker.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserGameService {

    private final UserGameRepository userGameRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public List<UserGame> getGamesByUser(Long userId) {
        return userGameRepository.findByUserId(userId);
    }

    public List<UserGame> getGamesByUserAndStatus(Long userId, GameStatus status) {
        return userGameRepository.findByUserIdAndStatus(userId, status);
    }

    public UserGame addGameToUser(Long userId, Long gameId, GameStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + gameId));

        if (userGameRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new IllegalArgumentException("Game already added to user's library");
        }

        UserGame userGame = UserGame.builder()
                .user(user)
                .game(game)
                .status(status != null ? status : GameStatus.BACKLOG)
                .build();

        return userGameRepository.save(userGame);
    }

    public UserGame updateStatus(Long userId, Long gameId, GameStatus newStatus) {
        UserGame userGame = userGameRepository.findByUserIdAndGameId(userId, gameId)
                .orElseThrow(() -> new EntityNotFoundException("No library entry found for this user and game"));

        userGame.setStatus(newStatus);
        return userGameRepository.save(userGame);
    }

    public void removeGameFromUser(Long userId, Long gameId) {
        UserGame userGame = userGameRepository.findByUserIdAndGameId(userId, gameId)
                .orElseThrow(() -> new EntityNotFoundException("No library entry found for this user and game"));

        userGameRepository.delete(userGame);
    }
}