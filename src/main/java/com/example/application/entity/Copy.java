package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "copies")
public class Copy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status;

    @Temporal(TemporalType.DATE)
    @Column(name = "acquisition_date", nullable = false)
    private Date acquisitionDate;

    @Column(nullable = false)
    private double price;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Relation avec Item
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Relations avec Loan
    @OneToMany(mappedBy = "copy")
    private Set<Loan> loans;

    // Relations avec Reservation
    @OneToMany(mappedBy = "copy")
    private Set<Reservation> reservations;
}
