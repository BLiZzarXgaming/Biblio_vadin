package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.UserRelationshipId}
 */
public class UserRelationshipIdDto implements Serializable {
    @NotNull
    private Long parentId;
    @NotNull
    private Long childId;

    public UserRelationshipIdDto() {
    }

    public UserRelationshipIdDto(Long parentId, Long childId) {
        this.parentId = parentId;
        this.childId = childId;
    }

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
        if (o == null || getClass() != o.getClass()) return false;
        UserRelationshipIdDto entity = (UserRelationshipIdDto) o;
        return Objects.equals(this.parentId, entity.parentId) &&
                Objects.equals(this.childId, entity.childId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, childId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "parentId = " + parentId + ", " +
                "childId = " + childId + ")";
    }
}