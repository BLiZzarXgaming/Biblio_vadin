package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.MagazineDto;
import com.example.application.entity.Magazine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface MagazineMapper {
    Magazine toEntity(MagazineDto magazine);
    MagazineDto toDto(Magazine magazine);
}
