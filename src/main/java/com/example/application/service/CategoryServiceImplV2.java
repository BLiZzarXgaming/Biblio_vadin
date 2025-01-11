package com.example.application.service;

import com.example.application.entity.DTO.CategoryDto;
import com.example.application.entity.Mapper.CategoryMapper;
import com.example.application.repository.CategoryRepositoryV2;
import com.example.application.service.implementation.CategoryServiceV2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImplV2 implements CategoryServiceV2 {
    private final CategoryRepositoryV2 categoryRepository;
    CategoryMapper categoryMapper;

    public CategoryServiceImplV2(CategoryRepositoryV2 categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<CategoryDto> findById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }

    @Override
    public Optional<CategoryDto> findByName(String name) {
        return categoryRepository.findByName(name).map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto save(CategoryDto category) {
        // Vérification d'unicité pour le nom de catégorie
        Optional<CategoryDto> existingCategory = categoryRepository.findByName(category.getName()).map(categoryMapper::toDto);
        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException("A category with the same name already exists.");
        }

        return categoryMapper.toDto(categoryRepository.save(categoryMapper.toEntity(category)));
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
