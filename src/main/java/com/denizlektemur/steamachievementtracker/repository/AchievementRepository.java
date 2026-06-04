package com.denizlektemur.steamachievementtracker.repository;

import com.denizlektemur.steamachievementtracker.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByGameId(Long gameId);
    Optional<Achievement> findByGameIdAndApiName(Long gameId, String apiName);
    int countByGameId(Long gameId);
}