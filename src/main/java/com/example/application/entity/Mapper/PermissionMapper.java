package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.PermissionDto;
import com.example.application.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toEntity(PermissionDto permission);
    PermissionDto toDto(Permission permission);
}
