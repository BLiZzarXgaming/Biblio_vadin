package com.example.application.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

public class UserRelationshipId implements Serializable {

    private Long parent;
    private Long child;

    // Constructeurs, equals, hashCode
    public UserRelationshipId() {}

    public UserRelationshipId(Long parent, Long child) {
        this.parent = parent;
        this.child = child;
    }

    // getters and setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRelationshipId that = (UserRelationshipId) o;

        return Objects.equals(parent, that.parent) && Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child);
    }
}
