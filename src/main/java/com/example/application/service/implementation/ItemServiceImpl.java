package com.example.application.service.implementation;

import com.example.application.entity.Category;
import com.example.application.entity.Item;
import com.example.application.entity.Publisher;
import com.example.application.entity.Book;
import com.example.application.entity.Magazine;
import com.example.application.entity.BoardGame;

import com.example.application.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl {

    private BookRepository bookRepository;

    private MagazineRepository magazineRepository;

    private BoardGameRepository boardGameRepository;

    private ItemRepository itemRepository;

    private CategoryRepository categoryRepository;

    private PublisherRepository publisherRepository;

    private CopyRepository copyRepository;

    public ItemServiceImpl(BookRepository bookRepository, MagazineRepository magazineRepository, BoardGameRepository boardGameRepository, ItemRepository itemRepository, CategoryRepository categoryRepository, PublisherRepository publisherRepository, CopyRepository copyRepository) {
        this.bookRepository = bookRepository;
        this.magazineRepository = magazineRepository;
        this.boardGameRepository = boardGameRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.copyRepository = copyRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll();
    }

    public List<Item> fetchItemsWithFilters(Map<String, Object> searchCriteria, String selectedType, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        if ("Livre".equals(selectedType)) {
            return bookRepository.findByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("author"),
                    (String) searchCriteria.get("isbn"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher"),
                    pageable
            ).stream().map(Book::getItem).collect(Collectors.toList());
        } else if ("Revue".equals(selectedType)) {
            return magazineRepository.findByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("isni"),
                    (String) searchCriteria.get("month"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher"),
                    pageable
            ).stream().map(Magazine::getItem).collect(Collectors.toList());
        } else if ("Jeu".equals(selectedType)) {
            return boardGameRepository.findByCriteriaWithPagination(
                    (String) searchCriteria.get("title"),
                    (Integer) searchCriteria.get("numberOfPieces"),
                    (Integer) searchCriteria.get("recommendedAge"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher"),
                    pageable
            ).stream().map(BoardGame::getItem).collect(Collectors.toList());
        } else {
            return itemRepository.findByCriteriaWithPagination(
                    (String) searchCriteria.get("keyword"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher"),
                    pageable
            );
        }
    }

    public int countItemsWithFilters(Map<String, Object> searchCriteria, String selectedType) {
        if ("Livre".equals(selectedType)) {
            return bookRepository.countByCriteria(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("author"),
                    (String) searchCriteria.get("isbn"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher")
            );
        } else if ("Revue".equals(selectedType)) {
            return magazineRepository.countByCriteria(
                    (String) searchCriteria.get("title"),
                    (String) searchCriteria.get("isni"),
                    (String) searchCriteria.get("month"),
                    (LocalDate) searchCriteria.get("publicationDate"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher")
            );
        } else if ("Jeu".equals(selectedType)) {
            return boardGameRepository.countByCriteria(
                    (String) searchCriteria.get("title"),
                    (Integer) searchCriteria.get("numberOfPieces"),
                    (Integer) searchCriteria.get("recommendedAge"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher")
            );
        } else {
            return itemRepository.countByCriteria(
                    (String) searchCriteria.get("keyword"),
                    (Long) searchCriteria.get("category"),
                    (Long) searchCriteria.get("publisher")
            );
        }
    }

    public Item findItemById(Long itemId) {
        return itemRepository.findById(itemId).orElse(null);
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
}
