package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Availability}
 */
public class AvailabilityDto implements Serializable {
    private Long id;
    @NotNull
    private UserDto user;
    @NotNull
    @Size(max = 255)
    private String title;
    @NotNull
    private LocalDate date;
    @NotNull
    private LocalTime time;
    @NotNull
    private Integer duration;
    @NotNull
    @Size(max = 255)
    private String details;
    @NotNull
    @Size(max = 255)
    private String type;
    @NotNull
    @Size(max = 255)
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public AvailabilityDto() {
    }

    public AvailabilityDto(Long id, UserDto user, String title, LocalDate date, LocalTime time, Integer duration, String details, String type, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.details = details;
        this.type = type;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        AvailabilityDto entity = (AvailabilityDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.user, entity.user) &&
                Objects.equals(this.title, entity.title) &&
                Objects.equals(this.date, entity.date) &&
                Objects.equals(this.time, entity.time) &&
                Objects.equals(this.duration, entity.duration) &&
                Objects.equals(this.details, entity.details) &&
                Objects.equals(this.type, entity.type) &&
                Objects.equals(this.status, entity.status) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, title, date, time, duration, details, type, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "user = " + user + ", " +
                "title = " + title + ", " +
                "date = " + date + ", " +
                "time = " + time + ", " +
                "duration = " + duration + ", " +
                "details = " + details + ", " +
                "type = " + type + ", " +
                "status = " + status + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}