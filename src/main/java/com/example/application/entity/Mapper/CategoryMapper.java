package com.example.application.entity.Mapper;

import com.example.application.entity.Category;
import com.example.application.entity.DTO.CategoryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryDto category);
    CategoryDto toDto(Category category);
}
