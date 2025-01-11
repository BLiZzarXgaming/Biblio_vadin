package com.example.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Hibernate;

import java.util.Objects;

@Embeddable
public class UserRelationshipId implements java.io.Serializable {
    private static final long serialVersionUID = 5882286935219285798L;
    @NotNull
    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @NotNull
    @Column(name = "child_id", nullable = false)
    private Long childId;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserRelationshipId entity = (UserRelationshipId) o;
        return Objects.equals(this.childId, entity.childId) &&
                Objects.equals(this.parentId, entity.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childId, parentId);
    }

}