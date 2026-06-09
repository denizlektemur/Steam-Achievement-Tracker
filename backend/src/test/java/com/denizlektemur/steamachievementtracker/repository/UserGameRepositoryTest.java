package com.denizlektemur.steamachievementtracker.repository;

import com.denizlektemur.steamachievementtracker.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserGameRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserGameRepository userGameRepository;

    @Test
    void findByUserId_returnsAllGamesForUser() {
        User user = userRepository.save(User.builder()
                .steamId("76561198012345678")
                .username("deniz")
                .build());

        Game game1 = gameRepository.save(Game.builder().appId(570).title("Dota 2").build());
        Game game2 = gameRepository.save(Game.builder().appId(440).title("TF2").build());

        userGameRepository.save(UserGame.builder().user(user).game(game1).status(GameStatus.IN_PROGRESS).build());
        userGameRepository.save(UserGame.builder().user(user).game(game2).status(GameStatus.BACKLOG).build());

        List<UserGame> result = userGameRepository.findByUserId(user.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void findByUserIdAndStatus_filtersCorrectly() {
        User user = userRepository.save(User.builder()
                .steamId("76561198099999999")
                .username("deniz2")
                .build());

        Game game1 = gameRepository.save(Game.builder().appId(571).title("Dota 2").build());
        Game game2 = gameRepository.save(Game.builder().appId(441).title("TF2").build());

        userGameRepository.save(UserGame.builder().user(user).game(game1).status(GameStatus.COMPLETED).build());
        userGameRepository.save(UserGame.builder().user(user).game(game2).status(GameStatus.BACKLOG).build());

        List<UserGame> completed = userGameRepository.findByUserIdAndStatus(user.getId(), GameStatus.COMPLETED);

        assertThat(completed).hasSize(1);
        assertThat(completed.get(0).getGame().getTitle()).isEqualTo("Dota 2");
    }

    @Test
    void findByUserIdAndGameId_returnsCorrectEntry() {
        User user = userRepository.save(User.builder()
                .steamId("76561198088888888")
                .username("deniz3")
                .build());

        Game game = gameRepository.save(Game.builder().appId(572).title("Dota 2").build());
        userGameRepository.save(UserGame.builder().user(user).game(game).status(GameStatus.IN_PROGRESS).build());

        Optional<UserGame> result = userGameRepository.findByUserIdAndGameId(user.getId(), game.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }
}