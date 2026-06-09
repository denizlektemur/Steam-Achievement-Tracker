package com.denizlektemur.steamachievementtracker.repository;

import com.denizlektemur.steamachievementtracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySteamId(String steamId);
    boolean existsBySteamId(String steamId);
}
