package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Reservation}
 */
public class ReservationDto implements Serializable {
    private Long id;
    @NotNull
    private CopyDto copy;
    @NotNull
    private UserDto member;
    @NotNull
    private LocalDate reservationDate;
    @NotNull
    @Size(max = 255)
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ReservationDto() {
    }

    public ReservationDto(Long id, CopyDto copy, UserDto member, LocalDate reservationDate, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.copy = copy;
        this.member = member;
        this.reservationDate = reservationDate;
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

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
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
        ReservationDto entity = (ReservationDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.copy, entity.copy) &&
                Objects.equals(this.member, entity.member) &&
                Objects.equals(this.reservationDate, entity.reservationDate) &&
                Objects.equals(this.status, entity.status) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, copy, member, reservationDate, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "copy = " + copy + ", " +
                "member = " + member + ", " +
                "reservationDate = " + reservationDate + ", " +
                "status = " + status + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}