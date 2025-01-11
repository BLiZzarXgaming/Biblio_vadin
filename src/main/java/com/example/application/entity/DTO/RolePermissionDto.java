package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.Mapping;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.RolePermission}
 */
public class RolePermissionDto implements Serializable {
    @NotNull
    private RolePermissionIdDto id;
    private RoleDto role;
    @NotNull
    private PermissionDto permission;
    private Instant createdAt;
    private Instant updatedAt;

    public RolePermissionDto() {
    }

    public RolePermissionDto(RolePermissionIdDto id, RoleDto role, PermissionDto permission, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.role = role;
        this.permission = permission;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RolePermissionIdDto getId() {
        return id;
    }

    public void setId(RolePermissionIdDto id) {
        this.id = id;
    }

    public RoleDto getRole() {
        return role;
    }

    public void setRole(RoleDto role) {
        this.role = role;
    }

    public PermissionDto getPermission() {
        return permission;
    }

    public void setPermission(PermissionDto permission) {
        this.permission = permission;
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
        RolePermissionDto entity = (RolePermissionDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.role, entity.role) &&
                Objects.equals(this.permission, entity.permission) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, permission, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "role = " + role + ", " +
                "permission = " + permission + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}