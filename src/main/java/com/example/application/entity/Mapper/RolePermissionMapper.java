package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.RolePermissionDto;
import com.example.application.entity.RolePermission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, PermissionMapper.class})
public interface RolePermissionMapper {

    RolePermission toEntity(RolePermissionDto rolePermission);
    RolePermissionDto toDto(RolePermission rolePermission);
}
