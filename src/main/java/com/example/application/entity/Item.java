package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private double value;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Relations avec Copy
    @OneToMany(mappedBy = "item")
    private Set<Copy> copies;

    // Relation avec Category
    @ManyToOne
    @JoinColumn(name = "category", nullable = false)
    private Category category;

    // Relation avec Publisher
    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    // Relation avec Supplier
    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    // Sous-classes
    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL)
    private Book book;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL)
    private Magazine magazine;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL)
    private BoardGame boardGame;
}
