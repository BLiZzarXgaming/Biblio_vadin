package com.example.application.entity.Mapper;

import com.example.application.entity.Communication;
import com.example.application.entity.DTO.CommunicationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommunicationMapper {

    Communication toEntity(CommunicationDto communication);
    CommunicationDto toDto(Communication communication);
}
