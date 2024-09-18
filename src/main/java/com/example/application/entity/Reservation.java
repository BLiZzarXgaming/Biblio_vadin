package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "reservation_date", nullable = false)
    private Date reservationDate;

    @Column(nullable = false)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Relation avec Copy
    @ManyToOne
    @JoinColumn(name = "copy_id", nullable = false)
    private Copy copy;

    // Relation avec User (Member)
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;
}
