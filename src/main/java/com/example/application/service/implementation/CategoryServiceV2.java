package com.example.application.service.implementation;

import com.example.application.entity.Category;
import com.example.application.entity.DTO.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryServiceV2 {
    List<CategoryDto> findAll();
    Optional<CategoryDto> findById(Long id);
    Optional<CategoryDto> findByName(String name);
    CategoryDto save(CategoryDto category);
    void deleteById(Long id);
}
