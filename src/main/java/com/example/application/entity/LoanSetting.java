package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "loan_settings")
public class LoanSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_duration_days", nullable = false)
    private int loanDurationDays;

    @Column(name = "max_loans_adult", nullable = false)
    private int maxLoansAdult;

    @Column(name = "max_loans_child", nullable = false)
    private int maxLoansChild;

    @Column(name = "max_reservations_adult", nullable = false)
    private int maxReservationsAdult;

    @Column(name = "max_reservations_child", nullable = false)
    private int maxReservationsChild;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}
