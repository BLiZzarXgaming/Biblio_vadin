package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Copy}
 */
public class CopyDto implements Serializable {
    private Long id;
    @NotNull
    private ItemDto item;
    @NotNull
    @Size(max = 255)
    private String status;
    @NotNull
    private LocalDate acquisitionDate;
    @NotNull
    private Double price;
    private Instant createdAt;
    private Instant updatedAt;

    public CopyDto() {
    }

    public CopyDto(Long id, ItemDto item, String status, LocalDate acquisitionDate, Double price, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.item = item;
        this.status = status;
        this.acquisitionDate = acquisitionDate;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopyDto entity = (CopyDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.item, entity.item) &&
                Objects.equals(this.status, entity.status) &&
                Objects.equals(this.acquisitionDate, entity.acquisitionDate) &&
                Objects.equals(this.price, entity.price) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, status, acquisitionDate, price, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "item = " + item + ", " +
                "status = " + status + ", " +
                "acquisitionDate = " + acquisitionDate + ", " +
                "price = " + price + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}