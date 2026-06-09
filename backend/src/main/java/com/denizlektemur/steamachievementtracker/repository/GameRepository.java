package com.denizlektemur.steamachievementtracker.repository;

import com.denizlektemur.steamachievementtracker.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByAppId(Integer appId);
    boolean existsByAppId(Integer appId);
}
