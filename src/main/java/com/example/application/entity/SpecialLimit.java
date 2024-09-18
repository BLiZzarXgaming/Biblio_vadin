package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "special_limits")
public class SpecialLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_loans", nullable = false)
    private int maxLoans;

    @Column(name = "max_reservations", nullable = false)
    private int maxReservations;

    @Column(nullable = false)
    private String status;

    // Relation avec User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
