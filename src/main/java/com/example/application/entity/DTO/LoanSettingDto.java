package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.LoanSetting}
 */
public class LoanSettingDto implements Serializable {
    private Long id;
    @NotNull
    private Integer loanDurationDays;
    @NotNull
    private Integer maxLoansAdult;
    @NotNull
    private Integer maxLoansChild;
    @NotNull
    private Integer maxReservationsAdult;
    @NotNull
    private Integer maxReservationsChild;
    private Instant createdAt;
    private Instant updatedAt;

    public LoanSettingDto() {
    }

    public LoanSettingDto(Long id, Integer loanDurationDays, Integer maxLoansAdult, Integer maxLoansChild, Integer maxReservationsAdult, Integer maxReservationsChild, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.loanDurationDays = loanDurationDays;
        this.maxLoansAdult = maxLoansAdult;
        this.maxLoansChild = maxLoansChild;
        this.maxReservationsAdult = maxReservationsAdult;
        this.maxReservationsChild = maxReservationsChild;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanSettingDto entity = (LoanSettingDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.loanDurationDays, entity.loanDurationDays) &&
                Objects.equals(this.maxLoansAdult, entity.maxLoansAdult) &&
                Objects.equals(this.maxLoansChild, entity.maxLoansChild) &&
                Objects.equals(this.maxReservationsAdult, entity.maxReservationsAdult) &&
                Objects.equals(this.maxReservationsChild, entity.maxReservationsChild) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, loanDurationDays, maxLoansAdult, maxLoansChild, maxReservationsAdult, maxReservationsChild, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "loanDurationDays = " + loanDurationDays + ", " +
                "maxLoansAdult = " + maxLoansAdult + ", " +
                "maxLoansChild = " + maxLoansChild + ", " +
                "maxReservationsAdult = " + maxReservationsAdult + ", " +
                "maxReservationsChild = " + maxReservationsChild + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}