package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.RoleDto;
import com.example.application.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleDto role);
    RoleDto toDto(Role role);
}
