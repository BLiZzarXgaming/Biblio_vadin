package com.example.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "user_relationships")
public class UserRelationship {
    @EmbeddedId
    private UserRelationshipId id;

    @MapsId("parentId")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @MapsId("childId")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Size(max = 255)
    @NotNull
    @Column(name = "relationship_type", nullable = false)
    private String relationshipType;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public UserRelationshipId getId() {
        return id;
    }

    public void setId(UserRelationshipId id) {
        this.id = id;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public User getChild() {
        return child;
    }

    public void setChild(User child) {
        this.child = child;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
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