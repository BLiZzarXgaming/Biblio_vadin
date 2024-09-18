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
}
