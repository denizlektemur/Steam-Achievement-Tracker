package com.denizlektemur.steamachievementtracker.service;

import com.denizlektemur.steamachievementtracker.model.Achievement;
import com.denizlektemur.steamachievementtracker.repository.AchievementRepository;
import com.denizlektemur.steamachievementtracker.repository.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final GameRepository gameRepository;

    public List<Achievement> getAchievementsByGame(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new EntityNotFoundException("Game not found with id: " + gameId);
        }
        return achievementRepository.findByGameId(gameId);
    }

    public Achievement getAchievementById(Long id) {
        return achievementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Achievement not found with id: " + id));
    }

    public int countByGame(Long gameId) {
        return achievementRepository.countByGameId(gameId);
    }
}