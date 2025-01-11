package com.example.application.entity.Mapper;

import com.example.application.entity.DTO.PublisherDto;
import com.example.application.entity.Publisher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublisherMapper {

    Publisher toEntity(PublisherDto publisher);
    PublisherDto toDto(Publisher publisher);
}
