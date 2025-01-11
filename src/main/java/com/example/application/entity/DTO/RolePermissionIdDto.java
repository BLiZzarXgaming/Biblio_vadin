package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.RolePermissionId}
 */
public class RolePermissionIdDto implements Serializable {
    @NotNull
    private Long roleId;
    @NotNull
    private Long permissionId;

    public RolePermissionIdDto() {
    }

    public RolePermissionIdDto(Long roleId, Long permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePermissionIdDto entity = (RolePermissionIdDto) o;
        return Objects.equals(this.roleId, entity.roleId) &&
                Objects.equals(this.permissionId, entity.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "roleId = " + roleId + ", " +
                "permissionId = " + permissionId + ")";
    }
}