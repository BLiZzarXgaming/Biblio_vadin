package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.User}
 */
public class UserDto implements Serializable {
    private Long id;
    @NotNull
    @Size(max = 255)
    private String firstName;
    @NotNull
    @Size(max = 255)
    private String lastName;
    @NotNull
    @Size(max = 255)
    private String username;
    @Size(max = 255)
    private String email;
    private Instant emailVerifiedAt;
    @NotNull
    @Size(max = 255)
    private String status;
    @NotNull
    @Size(max = 255)
    private String password;
    @Size(max = 255)
    private String phoneNumber;
    @Size(max = 255)
    private String cellNumber;
    @NotNull
    private Boolean isChild = false;
    @NotNull
    private RoleDto role;
    @NotNull
    private Instant dateOfBirth;
    @Size(max = 100)
    private String rememberToken;
    private Instant createdAt;
    private Instant updatedAt;

    public UserDto() {
    }

    public UserDto(Long id, String firstName, String lastName, String username, String email, Instant emailVerifiedAt,
            String status, String password, String phoneNumber, String cellNumber, Boolean isChild, RoleDto role,
            Instant dateOfBirth, String rememberToken, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.emailVerifiedAt = emailVerifiedAt;
        this.status = status;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.cellNumber = cellNumber;
        this.isChild = isChild;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.rememberToken = rememberToken;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(Instant emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCellNumber() {
        return cellNumber;
    }

    public void setCellNumber(String cellNumber) {
        this.cellNumber = cellNumber;
    }

    public Boolean getIsChild() {
        return isChild;
    }

    public void setIsChild(Boolean isChild) {
        this.isChild = isChild;
    }

    public RoleDto getRole() {
        return role;
    }

    public void setRole(RoleDto role) {
        this.role = role;
    }

    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Instant dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRememberToken() {
        return rememberToken;
    }

    public void setRememberToken(String rememberToken) {
        this.rememberToken = rememberToken;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDto entity = (UserDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.firstName, entity.firstName) &&
                Objects.equals(this.lastName, entity.lastName) &&
                Objects.equals(this.username, entity.username) &&
                Objects.equals(this.email, entity.email) &&
                Objects.equals(this.emailVerifiedAt, entity.emailVerifiedAt) &&
                Objects.equals(this.status, entity.status) &&
                Objects.equals(this.password, entity.password) &&
                Objects.equals(this.phoneNumber, entity.phoneNumber) &&
                Objects.equals(this.cellNumber, entity.cellNumber) &&
                Objects.equals(this.isChild, entity.isChild) &&
                Objects.equals(this.role, entity.role) &&
                Objects.equals(this.dateOfBirth, entity.dateOfBirth) &&
                Objects.equals(this.rememberToken, entity.rememberToken) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, username, email, emailVerifiedAt, status, password, phoneNumber,
                cellNumber, isChild, role, dateOfBirth, rememberToken, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "firstName = " + firstName + ", " +
                "lastName = " + lastName + ", " +
                "username = " + username + ", " +
                "email = " + email + ", " +
                "emailVerifiedAt = " + emailVerifiedAt + ", " +
                "status = " + status + ", " +
                "password = " + password + ", " +
                "phoneNumber = " + phoneNumber + ", " +
                "cellNumber = " + cellNumber + ", " +
                "isChild = " + isChild + ", " +
                "role = " + role + ", " +
                "dateOfBirth = " + dateOfBirth + ", " +
                "rememberToken = " + rememberToken + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}