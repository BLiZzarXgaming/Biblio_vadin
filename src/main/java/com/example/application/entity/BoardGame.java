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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    public void setNumberOfPieces(int numberOfPieces) {
        this.numberOfPieces = numberOfPieces;
    }

    public int getRecommendedAge() {
        return recommendedAge;
    }

    public void setRecommendedAge(int recommendedAge) {
        this.recommendedAge = recommendedAge;
    }

    public String getGameRules() {
        return gameRules;
    }

    public void setGameRules(String gameRules) {
        this.gameRules = gameRules;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BoardGame() {
    }

    public BoardGame(Long itemId, Item item, int numberOfPieces, int recommendedAge, String gameRules, Date createdAt, Date updatedAt) {
        this.itemId = itemId;
        this.item = item;
        this.numberOfPieces = numberOfPieces;
        this.recommendedAge = recommendedAge;
        this.gameRules = gameRules;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
