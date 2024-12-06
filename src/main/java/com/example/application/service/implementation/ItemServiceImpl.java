package com.example.application.service.implementation;

import com.example.application.entity.*;

import com.example.application.repository.*;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl {

    @Autowired
    private final BookRepository bookRepository;

    private final MagazineRepository magazineRepository;

    private final BoardGameRepository boardGameRepository;

    private final ItemRepository itemRepository;

    private final CategoryRepository categoryRepository;

    private final PublisherRepository publisherRepository;

    public ItemServiceImpl(BookRepository bookRepository, MagazineRepository magazineRepository, BoardGameRepository boardGameRepository, ItemRepository itemRepository, CategoryRepository categoryRepository, PublisherRepository publisherRepository) {
        this.bookRepository = bookRepository;
        this.magazineRepository = magazineRepository;
        this.boardGameRepository = boardGameRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll();
    }

    public List<Item> fetchItemsWithFilters(Map<String, Object> searchCriteria, String selectedType, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit); // Pageable object for pagination
        if ("Livre".equals(selectedType)) { // Book
            return itemRepository.findBookByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("author"),
                    (String) searchCriteria.get("isbn"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    searchCriteria.get("category") != null ? ((Category) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((Publisher) searchCriteria.get("publisher")).getId() : null,
                    pageable
            );
        } else if ("Revue".equals(selectedType)) { // Magazine
            return itemRepository.findMagazineByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("isni"),
                    (String) searchCriteria.get("month"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    searchCriteria.get("category") != null ? ((Category) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((Publisher) searchCriteria.get("publisher")).getId() : null,
                    pageable
            );
        } else if ("Jeu".equals(selectedType)) { // BoardGame
            return itemRepository.findBoardGameByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (Integer) searchCriteria.get("numberOfPieces"),
                    (Integer) searchCriteria.get("recommendedAge"),
                    searchCriteria.get("category") != null ? ((Category) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((Publisher) searchCriteria.get("publisher")).getId() : null,
                    searchCriteria.get("gtin") != null ? (String) searchCriteria.get("gtin") : null,
                    pageable
            );
        } else { // All items
            return itemRepository.findByCriteriaWithPagination(
                    (String) searchCriteria.get("keyword"),
                    searchCriteria.get("category") != null ? ((Category) searchCriteria.get("category")).getId() : null,
                    searchCriteria.get("publisher") != null ? ((Publisher) searchCriteria.get("publisher")).getId() : null,
                    pageable
            );
        }
    }

    public Item findItemById(Long itemId) {
        return itemRepository.findByIdItem(itemId);

    }

    public Book findBookByItemId(Long itemId) {
        return bookRepository.findById(itemId).orElse(null);
    }

    public Magazine findMagazineByItemId(Long itemId) {
        return magazineRepository.findById(itemId).orElse(null);
    }

    public BoardGame findBoardGameByItemId(Long itemId) {
        return boardGameRepository.findById(itemId).orElse(null);
    }

    public Long save(Item item) {

        if (item.getId() != null) {
            Item existingItem = itemRepository.findByIdItem(item.getId());
            if (existingItem != null) {
                return 0L;
            }
        }

        return itemRepository.save(item).getId();
    }

}
