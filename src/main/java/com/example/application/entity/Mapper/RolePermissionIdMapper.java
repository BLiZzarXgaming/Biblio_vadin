package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.RolePermissionIdDto;
import com.example.application.entity.RolePermissionId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RolePermissionIdMapper {

    RolePermissionId toEntity(RolePermissionIdDto rolePermissionId);
    RolePermissionIdDto toDto(RolePermissionId rolePermissionId);
}
