package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Item}
 */
public class ItemDto implements Serializable {
    private Long id;
    @NotNull
    @Size(max = 255)
    private String type;
    @NotNull
    @Size(max = 255)
    private String title;
    @NotNull
    private CategoryDto category;
    @NotNull
    private PublisherDto publisher;
    @NotNull
    private SupplierDto supplier;
    @NotNull
    private Double value;
    private Instant createdAt;
    private Instant updatedAt;
    @Size(max = 2000)
    private String link;

    public ItemDto() {
    }

    public ItemDto(Long id, String type, String title, CategoryDto category, PublisherDto publisher, SupplierDto supplier, Double value, Instant createdAt, Instant updatedAt, String link) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.category = category;
        this.publisher = publisher;
        this.supplier = supplier;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDto entity = (ItemDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.type, entity.type) &&
                Objects.equals(this.title, entity.title) &&
                Objects.equals(this.category, entity.category) &&
                Objects.equals(this.publisher, entity.publisher) &&
                Objects.equals(this.supplier, entity.supplier) &&
                Objects.equals(this.value, entity.value) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt) &&
                Objects.equals(this.link, entity.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, title, category, publisher, supplier, value, createdAt, updatedAt, link);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "type = " + type + ", " +
                "title = " + title + ", " +
                "category = " + category + ", " +
                "publisher = " + publisher + ", " +
                "supplier = " + supplier + ", " +
                "value = " + value + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ", " +
                "link = " + link + ")";
    }
}