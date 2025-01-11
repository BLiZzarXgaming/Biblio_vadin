package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.BoardGame}
 */
public class BoardGameDto implements Serializable {
    private Long id;
    @NotNull
    private ItemDto item;
    @NotNull
    private Integer numberOfPieces;
    @NotNull
    private Integer recommendedAge;
    @NotNull
    @Size(max = 65535)
    private String gameRules;
    private Instant createdAt;
    private Instant updatedAt;
    @NotNull
    @Size(max = 16)
    private String gtin;

    public BoardGameDto() {
    }

    public BoardGameDto(Long id, ItemDto item, Integer numberOfPieces, Integer recommendedAge, String gameRules, Instant createdAt, Instant updatedAt, String gtin) {
        this.id = id;
        this.item = item;
        this.numberOfPieces = numberOfPieces;
        this.recommendedAge = recommendedAge;
        this.gameRules = gameRules;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.gtin = gtin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }

    public Integer getNumberOfPieces() {
        return numberOfPieces;
    }

    public void setNumberOfPieces(Integer numberOfPieces) {
        this.numberOfPieces = numberOfPieces;
    }

    public Integer getRecommendedAge() {
        return recommendedAge;
    }

    public void setRecommendedAge(Integer recommendedAge) {
        this.recommendedAge = recommendedAge;
    }

    public String getGameRules() {
        return gameRules;
    }

    public void setGameRules(String gameRules) {
        this.gameRules = gameRules;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardGameDto entity = (BoardGameDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.item, entity.item) &&
                Objects.equals(this.numberOfPieces, entity.numberOfPieces) &&
                Objects.equals(this.recommendedAge, entity.recommendedAge) &&
                Objects.equals(this.gameRules, entity.gameRules) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt) &&
                Objects.equals(this.gtin, entity.gtin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, numberOfPieces, recommendedAge, gameRules, createdAt, updatedAt, gtin);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "item = " + item + ", " +
                "numberOfPieces = " + numberOfPieces + ", " +
                "recommendedAge = " + recommendedAge + ", " +
                "gameRules = " + gameRules + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ", " +
                "gtin = " + gtin + ")";
    }
}