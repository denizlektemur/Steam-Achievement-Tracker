package com.denizlektemur.steamachievementtracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_games",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    private LocalDateTime lastPlayedAt;

    @Column(updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = GameStatus.BACKLOG;
        }
    }
}
