package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "board_games")
public class BoardGame {

    @Id
    private Long itemId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "number_of_pieces", nullable = false)
    private int numberOfPieces;

    @Column(name = "recommended_age", nullable = false)
    private int recommendedAge;

    @Column(name = "game_rules", columnDefinition = "TEXT", nullable = false)
    private String gameRules;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}
