package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

    @Id
    private Long itemId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String author;

    @Temporal(TemporalType.DATE)
    @Column(name = "publication_date", nullable = false)
    private Date publicationDate;

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

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
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

    public Book() {
    }

    public Book(Long itemId, Item item, String isbn, String author, Date publicationDate, Date createdAt, Date updatedAt) {
        this.itemId = itemId;
        this.item = item;
        this.isbn = isbn;
        this.author = author;
        this.publicationDate = publicationDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
