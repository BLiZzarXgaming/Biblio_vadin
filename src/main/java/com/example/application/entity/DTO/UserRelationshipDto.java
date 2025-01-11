package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.UserRelationship}
 */
public class UserRelationshipDto implements Serializable {
    private UserRelationshipIdDto id;
    private UserDto parent;
    private UserDto child;
    @NotNull
    @Size(max = 255)
    private String relationshipType;
    private Instant createdAt;
    private Instant updatedAt;

    public UserRelationshipDto() {
    }

    public UserRelationshipDto(UserRelationshipIdDto id, UserDto parent, UserDto child, String relationshipType, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.parent = parent;
        this.child = child;
        this.relationshipType = relationshipType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserRelationshipIdDto getId() {
        return id;
    }

    public void setId(UserRelationshipIdDto id) {
        this.id = id;
    }

    public UserDto getParent() {
        return parent;
    }

    public void setParent(UserDto parent) {
        this.parent = parent;
    }

    public UserDto getChild() {
        return child;
    }

    public void setChild(UserDto child) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRelationshipDto entity = (UserRelationshipDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.parent, entity.parent) &&
                Objects.equals(this.child, entity.child) &&
                Objects.equals(this.relationshipType, entity.relationshipType) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parent, child, relationshipType, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "parent = " + parent + ", " +
                "child = " + child + ", " +
                "relationshipType = " + relationshipType + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}