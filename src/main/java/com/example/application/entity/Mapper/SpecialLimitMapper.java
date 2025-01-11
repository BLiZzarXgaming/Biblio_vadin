package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.SpecialLimitDto;
import com.example.application.entity.SpecialLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SpecialLimitMapper {

        SpecialLimit toEntity(SpecialLimitDto specialLimit);
        SpecialLimitDto toDto(SpecialLimit specialLimit);
}
