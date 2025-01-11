package com.example.application.service.implementation;

import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.Item;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ItemServiceV2 {
    List<ItemDto> findAll();
    Optional<ItemDto> findById(Long id);
    List<ItemDto> findByType(String type);
    List<ItemDto> searchByTitle(String keyword);
    List<ItemDto> findByCategory(Long categoryId);
    ItemDto save(ItemDto item);
    void deleteById(Long id);
    List<ItemDto> fetchItemsWithFilters(Map<String, Object> searchCriteria, String selectedType, int offset, int limit);
}
