package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Magazine}
 */
public class MagazineDto implements Serializable {
    private Long id;
    @NotNull
    private ItemDto item;
    @NotNull
    @Size(max = 255)
    private String isni;
    @NotNull
    @Size(max = 255)
    private String month;
    @NotNull
    private LocalDate publicationDate;
    private Instant createdAt;
    private Instant updatedAt;
    @NotNull
    @Size(max = 4)
    private String year;

    public MagazineDto() {
    }

    public MagazineDto(Long id, ItemDto item, String isni, String month, LocalDate publicationDate, Instant createdAt, Instant updatedAt, String year) {
        this.id = id;
        this.item = item;
        this.isni = isni;
        this.month = month;
        this.publicationDate = publicationDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.year = year;
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

    public String getIsni() {
        return isni;
    }

    public void setIsni(String isni) {
        this.isni = isni;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagazineDto entity = (MagazineDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.item, entity.item) &&
                Objects.equals(this.isni, entity.isni) &&
                Objects.equals(this.month, entity.month) &&
                Objects.equals(this.publicationDate, entity.publicationDate) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt) &&
                Objects.equals(this.year, entity.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, isni, month, publicationDate, createdAt, updatedAt, year);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "item = " + item + ", " +
                "isni = " + isni + ", " +
                "month = " + month + ", " +
                "publicationDate = " + publicationDate + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ", " +
                "year = " + year + ")";
    }
}