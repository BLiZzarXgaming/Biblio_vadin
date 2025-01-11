package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Book}
 */
public class BookDto implements Serializable {
    private Long id;
    @NotNull
    private ItemDto item;
    @NotNull
    @Size(max = 255)
    private String isbn;
    @NotNull
    @Size(max = 255)
    private String author;
    @NotNull
    private LocalDate publicationDate;
    private Instant createdAt;
    private Instant updatedAt;

    public BookDto() {
    }

    public BookDto(Long id, ItemDto item, String isbn, String author, LocalDate publicationDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.item = item;
        this.isbn = isbn;
        this.author = author;
        this.publicationDate = publicationDate;
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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDto entity = (BookDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.item, entity.item) &&
                Objects.equals(this.isbn, entity.isbn) &&
                Objects.equals(this.author, entity.author) &&
                Objects.equals(this.publicationDate, entity.publicationDate) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, isbn, author, publicationDate, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "item = " + item + ", " +
                "isbn = " + isbn + ", " +
                "author = " + author + ", " +
                "publicationDate = " + publicationDate + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}