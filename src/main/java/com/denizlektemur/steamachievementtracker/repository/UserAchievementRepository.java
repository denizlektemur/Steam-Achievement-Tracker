package com.denizlektemur.steamachievementtracker.repository;

import com.denizlektemur.steamachievementtracker.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserId(Long userId);
    List<UserAchievement> findByUserIdAndGameId(Long userId, Long gameId);
    int countByUserIdAndGameId(Long userId, Long gameId);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.game.id = :gameId")
    int countTotalAchievementsByGameId(@Param("gameId") Long gameId);
}