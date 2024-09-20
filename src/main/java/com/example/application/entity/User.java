package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false, name = "username")
    private String username;

    @Column(unique = true, nullable = true, name = "email")
    private String email;

    @Column(name = "email_verified_at")
    private Date emailVerifiedAt;

    @Column(nullable = false, name = "status")
    private String status;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "cell_number")
    private String cellNumber;

    @Column(name = "is_child", nullable = false)
    private boolean isChild;

    @Column(name = "date_of_birth", nullable = false)
    private Date dateOfBirth;

    @Column(name = "remember_token")
    private String rememberToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Relation avec Role
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Relations avec d'autres entités
    @OneToMany(mappedBy = "user")
    private Set<Availability> availabilities;

    @OneToMany(mappedBy = "member")
    private Set<Loan> loans;

    @OneToMany(mappedBy = "member")
    private Set<Reservation> reservations;

    @OneToMany(mappedBy = "user")
    private Set<SpecialLimit> specialLimits;

    @OneToMany(mappedBy = "parent")
    private Set<UserRelationship> childRelationships;

    @OneToMany(mappedBy = "child")
    private Set<UserRelationship> parentRelationships;

    @OneToMany(mappedBy = "member")
    private Set<Communication> communications;


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

    public Date getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(Date emailVerifiedAt) {
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

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRememberToken() {
        return rememberToken;
    }

    public void setRememberToken(String rememberToken) {
        this.rememberToken = rememberToken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Availability> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(Set<Availability> availabilities) {
        this.availabilities = availabilities;
    }

    public Set<Loan> getLoans() {
        return loans;
    }

    public void setLoans(Set<Loan> loans) {
        this.loans = loans;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Set<SpecialLimit> getSpecialLimits() {
        return specialLimits;
    }

    public void setSpecialLimits(Set<SpecialLimit> specialLimits) {
        this.specialLimits = specialLimits;
    }

    public Set<UserRelationship> getChildRelationships() {
        return childRelationships;
    }

    public void setChildRelationships(Set<UserRelationship> childRelationships) {
        this.childRelationships = childRelationships;
    }

    public Set<UserRelationship> getParentRelationships() {
        return parentRelationships;
    }

    public void setParentRelationships(Set<UserRelationship> parentRelationships) {
        this.parentRelationships = parentRelationships;
    }

    public Set<Communication> getCommunications() {
        return communications;
    }

    public void setCommunications(Set<Communication> communications) {
        this.communications = communications;
    }

    public User() {
    }

    public User(Long id, String firstName, String lastName, String username, String email, Date emailVerifiedAt, String status, String password, String phoneNumber, String cellNumber, boolean isChild, Date dateOfBirth, String rememberToken, Date createdAt, Date updatedAt, Role role, Set<Availability> availabilities, Set<Loan> loans, Set<Reservation> reservations, Set<SpecialLimit> specialLimits, Set<UserRelationship> childRelationships, Set<UserRelationship> parentRelationships, Set<Communication> communications) {
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
        this.dateOfBirth = dateOfBirth;
        this.rememberToken = rememberToken;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.role = role;
        this.availabilities = availabilities;
        this.loans = loans;
        this.reservations = reservations;
        this.specialLimits = specialLimits;
        this.childRelationships = childRelationships;
        this.parentRelationships = parentRelationships;
        this.communications = communications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isChild == user.isChild && Objects.equals(id, user.id) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(username, user.username) && Objects.equals(email, user.email) && Objects.equals(emailVerifiedAt, user.emailVerifiedAt) && Objects.equals(status, user.status) && Objects.equals(password, user.password) && Objects.equals(phoneNumber, user.phoneNumber) && Objects.equals(cellNumber, user.cellNumber) && Objects.equals(dateOfBirth, user.dateOfBirth) && Objects.equals(rememberToken, user.rememberToken) && Objects.equals(createdAt, user.createdAt) && Objects.equals(updatedAt, user.updatedAt) && Objects.equals(role, user.role) && Objects.equals(availabilities, user.availabilities) && Objects.equals(loans, user.loans) && Objects.equals(reservations, user.reservations) && Objects.equals(specialLimits, user.specialLimits) && Objects.equals(childRelationships, user.childRelationships) && Objects.equals(parentRelationships, user.parentRelationships) && Objects.equals(communications, user.communications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, username, email, emailVerifiedAt, status, password, phoneNumber, cellNumber, isChild, dateOfBirth, rememberToken, createdAt, updatedAt, role, availabilities, loans, reservations, specialLimits, childRelationships, parentRelationships, communications);
    }
}
