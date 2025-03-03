package com.example.application.service.implementation;

import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.Item;

import java.util.Date;
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

    // Méthodes pour les statistiques
    Map<String, Long> countItemsByType();

    String getMostPopularCategory(Date startDate, Date endDate);

    String getMostBorrowedType(Date startDate, Date endDate);

    double calculateTotalInventoryValue();

    double calculateTotalBorrowedValue(Date startDate, Date endDate);

    int countTotalItems();

    int countRecentAcquisitions();

    String getMostPopularItem(Date startDate, Date endDate);
}
