package com.denizlektemur.steamachievementtracker.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "games")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer appId;

    @Column(nullable = false)
    private String title;

    private String headerImageUrl;
}