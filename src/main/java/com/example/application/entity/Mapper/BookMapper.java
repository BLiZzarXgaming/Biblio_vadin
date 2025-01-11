package com.example.application.entity.Mapper;

import com.example.application.entity.Book;
import com.example.application.entity.DTO.BookDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface BookMapper {

        Book toEntity(BookDto book);
        BookDto toDto(Book book);
}
