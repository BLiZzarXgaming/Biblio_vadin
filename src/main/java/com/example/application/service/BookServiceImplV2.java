package com.example.application.service;

import com.example.application.entity.DTO.BookDto;
import com.example.application.entity.Mapper.BookMapper;
import com.example.application.entity.Mapper.ItemMapper;
import com.example.application.repository.BookRepositoryV2;
import com.example.application.repository.ItemRepositoryV2;
import com.example.application.service.implementation.BookServiceV2;
import com.example.application.utils.DateUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookServiceImplV2 implements BookServiceV2 {
    private final ItemRepositoryV2 itemRepositoryV2;
    private BookRepositoryV2 bookRepository;
    private ItemMapper itemMapper;
    private BookMapper bookMapper;


    public BookServiceImplV2(BookRepositoryV2 bookRepository, ItemRepositoryV2 itemRepositoryV2, ItemMapper itemMapper, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.itemRepositoryV2 = itemRepositoryV2;
        this.itemMapper = itemMapper;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream().map(bookMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<BookDto> findById(Long id) {
        return bookRepository.findById(id).map(bookMapper::toDto);
    }

    @Override
    public List<BookDto> findByAuthor(String author) {
        return bookRepository.findByAuthorContaining(author).stream().map(bookMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<BookDto> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn).stream().findFirst().map(bookMapper::toDto);
    }

    @Override
    public List<BookDto> findByPublicationDateRange(Date startDate, Date endDate) {
        return bookRepository.findByPublicationDateBetween(startDate, endDate).stream().map(bookMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookDto save(BookDto book) {

        itemRepositoryV2.save(itemMapper.toEntity(book.getItem()));

        if (book.getId() == null) {
            System.out.println("BookServiceImplV2: save: book.getId() == null");
        }
        return bookMapper.toDto(bookRepository.save(bookMapper.toEntity(book)));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public int insertBook(BookDto book) {

        Optional<BookDto> booktest = findByIsbn(book.getIsbn());
        if (booktest.isPresent()) {
            throw new IllegalArgumentException("A book with the same ISBN already exists.");
        }

        return bookRepository.insertBook(book.getItem().getId(), book.getAuthor(), book.getIsbn(), DateUtils.convertToDateViaInstant(book.getPublicationDate()));
    }
}
