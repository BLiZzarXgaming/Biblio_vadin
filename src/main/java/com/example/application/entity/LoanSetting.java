package com.example.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "loan_settings")
public class LoanSetting {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "loan_duration_days", nullable = false)
    private Integer loanDurationDays;

    @NotNull
    @Column(name = "max_loans_adult", nullable = false)
    private Integer maxLoansAdult;

    @NotNull
    @Column(name = "max_loans_child", nullable = false)
    private Integer maxLoansChild;

    @NotNull
    @Column(name = "max_reservations_adult", nullable = false)
    private Integer maxReservationsAdult;

    @NotNull
    @Column(name = "max_reservations_child", nullable = false)
    private Integer maxReservationsChild;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLoanDurationDays() {
        return loanDurationDays;
    }

    public void setLoanDurationDays(Integer loanDurationDays) {
        this.loanDurationDays = loanDurationDays;
    }

    public Integer getMaxLoansAdult() {
        return maxLoansAdult;
    }

    public void setMaxLoansAdult(Integer maxLoansAdult) {
        this.maxLoansAdult = maxLoansAdult;
    }

    public Integer getMaxLoansChild() {
        return maxLoansChild;
    }

    public void setMaxLoansChild(Integer maxLoansChild) {
        this.maxLoansChild = maxLoansChild;
    }

    public Integer getMaxReservationsAdult() {
        return maxReservationsAdult;
    }

    public void setMaxReservationsAdult(Integer maxReservationsAdult) {
        this.maxReservationsAdult = maxReservationsAdult;
    }

    public Integer getMaxReservationsChild() {
        return maxReservationsChild;
    }

    public void setMaxReservationsChild(Integer maxReservationsChild) {
        this.maxReservationsChild = maxReservationsChild;
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

}