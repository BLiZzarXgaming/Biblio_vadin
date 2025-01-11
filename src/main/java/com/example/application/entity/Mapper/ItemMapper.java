package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface ItemMapper {

    Item toEntity(ItemDto item);
    ItemDto toDto(Item item);
}
