package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Loan}
 */
public class LoanDto implements Serializable {
    private Long id;
    @NotNull
    private CopyDto copy;
    @NotNull
    private UserDto member;
    @NotNull
    private LocalDate loanDate;
    @NotNull
    private LocalDate returnDueDate;
    @NotNull
    @Size(max = 255)
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public LoanDto() {
    }

    public LoanDto(Long id, CopyDto copy, UserDto member, LocalDate loanDate, LocalDate returnDueDate, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.copy = copy;
        this.member = member;
        this.loanDate = loanDate;
        this.returnDueDate = returnDueDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CopyDto getCopy() {
        return copy;
    }

    public void setCopy(CopyDto copy) {
        this.copy = copy;
    }

    public UserDto getMember() {
        return member;
    }

    public void setMember(UserDto member) {
        this.member = member;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getReturnDueDate() {
        return returnDueDate;
    }

    public void setReturnDueDate(LocalDate returnDueDate) {
        this.returnDueDate = returnDueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        LoanDto entity = (LoanDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.copy, entity.copy) &&
                Objects.equals(this.member, entity.member) &&
                Objects.equals(this.loanDate, entity.loanDate) &&
                Objects.equals(this.returnDueDate, entity.returnDueDate) &&
                Objects.equals(this.status, entity.status) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, copy, member, loanDate, returnDueDate, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "copy = " + copy + ", " +
                "member = " + member + ", " +
                "loanDate = " + loanDate + ", " +
                "returnDueDate = " + returnDueDate + ", " +
                "status = " + status + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}