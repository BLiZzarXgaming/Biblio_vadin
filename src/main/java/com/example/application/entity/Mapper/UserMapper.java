package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

        User toEntity(UserDto user);
        UserDto toDto(User user);
}
