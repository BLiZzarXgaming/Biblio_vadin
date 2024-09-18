package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "loan_date", nullable = false)
    private Date loanDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "return_due_date", nullable = false)
    private Date returnDueDate;

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
