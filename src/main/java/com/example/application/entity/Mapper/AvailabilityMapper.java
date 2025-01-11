package com.example.application.entity.Mapper;

import com.example.application.entity.Availability;
import com.example.application.entity.DTO.AvailabilityDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AvailabilityMapper {
    //AvailabilityMapper INSTANCE = Mappers.getMapper(AvailabilityMapper.class);

    Availability toEntity(AvailabilityDto availabilityDto);
    AvailabilityDto toDto(Availability availability);
}
