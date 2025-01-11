package com.example.application.entity.Mapper;

import com.example.application.entity.Copy;
import com.example.application.entity.DTO.CopyDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface CopyMapper {

        Copy toEntity(CopyDto copy);
        CopyDto toDto(Copy copy);
}
