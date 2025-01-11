package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.SpecialLimit}
 */
public class SpecialLimitDto implements Serializable {
    private Long id;
    @NotNull
    private UserDto user;
    @NotNull
    private Integer maxLoans;
    @NotNull
    private Integer maxReservations;
    @NotNull
    @Size(max = 255)
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public SpecialLimitDto() {
    }

    public SpecialLimitDto(Long id, UserDto user, Integer maxLoans, Integer maxReservations, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.maxLoans = maxLoans;
        this.maxReservations = maxReservations;
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

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public Integer getMaxLoans() {
        return maxLoans;
    }

    public void setMaxLoans(Integer maxLoans) {
        this.maxLoans = maxLoans;
    }

    public Integer getMaxReservations() {
        return maxReservations;
    }

    public void setMaxReservations(Integer maxReservations) {
        this.maxReservations = maxReservations;
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
        SpecialLimitDto entity = (SpecialLimitDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.user, entity.user) &&
                Objects.equals(this.maxLoans, entity.maxLoans) &&
                Objects.equals(this.maxReservations, entity.maxReservations) &&
                Objects.equals(this.status, entity.status) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, maxLoans, maxReservations, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "user = " + user + ", " +
                "maxLoans = " + maxLoans + ", " +
                "maxReservations = " + maxReservations + ", " +
                "status = " + status + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}