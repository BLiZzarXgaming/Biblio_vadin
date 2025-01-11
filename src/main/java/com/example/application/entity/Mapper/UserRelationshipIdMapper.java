package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.UserRelationshipIdDto;
import com.example.application.entity.UserRelationshipId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRelationshipIdMapper {

        UserRelationshipId toEntity(UserRelationshipIdDto userRelationshipId);
        UserRelationshipIdDto toDto(UserRelationshipId userRelationshipId);
}
