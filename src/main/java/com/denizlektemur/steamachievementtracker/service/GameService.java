package com.denizlektemur.steamachievementtracker.service;

import com.denizlektemur.steamachievementtracker.model.Game;
import com.denizlektemur.steamachievementtracker.repository.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with id: " + id));
    }

    public Game getGameByAppId(Integer appId) {
        return gameRepository.findByAppId(appId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with appId: " + appId));
    }

    public Game createGame(Game game) {
        if (gameRepository.existsByAppId(game.getAppId())) {
            throw new IllegalArgumentException("Game already exists with appId: " + game.getAppId());
        }
        return gameRepository.save(game);
    }

    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new EntityNotFoundException("Game not found with id: " + id);
        }
        gameRepository.deleteById(id);
    }
}