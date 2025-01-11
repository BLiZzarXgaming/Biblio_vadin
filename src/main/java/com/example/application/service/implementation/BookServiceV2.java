package com.example.application.service.implementation;

import com.example.application.entity.Book;
import com.example.application.entity.DTO.BookDto;

import java.util.List;
import java.util.Optional;

public interface BookServiceV2 {
    List<BookDto> findAll();
    Optional<BookDto> findById(Long id);
    List<BookDto> findByAuthor(String author);
    Optional<BookDto> findByIsbn(String isbn);
    List<BookDto> findByPublicationDateRange(java.util.Date startDate, java.util.Date endDate);
    BookDto save(BookDto book);
    void deleteById(Long id);

    int insertBook(BookDto book);
}
