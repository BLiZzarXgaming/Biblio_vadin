package com.example.application.entity.Mapper;

import com.example.application.entity.BoardGame;
import com.example.application.entity.DTO.BoardGameDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface BoardGameMapper {

    BoardGame toEntity(BoardGameDto boardGame);
    BoardGameDto toDto(BoardGame boardGame);
}
