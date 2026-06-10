package com.denizlektemur.steamachievementtracker.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "api_name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private String apiName;

    private String displayName;
    private String description;
    private String iconUrl;
    private Double globalPercentage;
}
