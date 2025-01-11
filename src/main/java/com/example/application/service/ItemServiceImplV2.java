package com.example.application.service;

import com.example.application.entity.DTO.CategoryDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.PublisherDto;
import com.example.application.entity.Mapper.ItemMapper;
import com.example.application.repository.ItemRepositoryV2;
import com.example.application.service.implementation.ItemServiceV2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImplV2 implements ItemServiceV2 {
    private final ItemRepositoryV2 itemRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImplV2(ItemRepositoryV2 itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    public List<ItemDto> findAll() {
        return itemRepository.findAll().stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<ItemDto> findById(Long id) {
        return itemRepository.findById(id).map(itemMapper::toDto);
    }

    @Override
    public List<ItemDto> findByType(String type) {
        return itemRepository.findByType(type).stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchByTitle(String keyword) {
        return itemRepository.findByTitleContaining(keyword).stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findByCategory(Long categoryId) {
        return itemRepository.findByCategoryId(categoryId).stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto save(ItemDto item) {
        return itemMapper.toDto(itemRepository.save(itemMapper.toEntity(item)));
    }

    @Override
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> fetchItemsWithFilters(Map<String, Object> searchCriteria, String selectedType, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit); // Pageable object for pagination
        if ("Livre".equals(selectedType)) { // Book
            return itemRepository.findBookByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("author"),
                    (String) searchCriteria.get("isbn"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId() : null,
                    pageable
            ).stream().map(itemMapper::toDto).collect(Collectors.toList());
        } else if ("Revue".equals(selectedType)) { // Magazine
            return itemRepository.findMagazineByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("isni"),
                    (String) searchCriteria.get("month"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId() : null,
                    pageable
            ).stream().map(itemMapper::toDto).collect(Collectors.toList());
        } else if ("Jeu".equals(selectedType)) { // BoardGame
            return itemRepository.findBoardGameByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (Integer) searchCriteria.get("numberOfPieces"),
                    (Integer) searchCriteria.get("recommendedAge"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId() : null,
                    searchCriteria.get("gtin") != null ? (String) searchCriteria.get("gtin") : null,
                    pageable
            ).stream().map(itemMapper::toDto).collect(Collectors.toList());
        } else { // All items
            return itemRepository.findByCriteriaWithPagination(
                    (String) searchCriteria.get("keyword"),
                    searchCriteria.get("category") != null ? ((CategoryDto) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((PublisherDto) searchCriteria.get("publisher")).getId() : null,
                    pageable
            ).stream().map(itemMapper::toDto).collect(Collectors.toList());
        }
    }
}
