package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.UserRelationshipDto;
import com.example.application.entity.UserRelationship;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, UserRelationshipIdMapper.class})
public interface UserRelationshipMapper {

    UserRelationship toEntity(UserRelationshipDto userRelationship);
    UserRelationshipDto toDto(UserRelationship userRelationship);
}
