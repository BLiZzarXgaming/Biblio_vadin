package com.example.application.service.implementation;

import com.example.application.entity.Book;
import com.example.application.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl {

    private BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public Book findByItemId(long itemId) {
        return bookRepository.findByItemId(itemId);
    }

    public int save(Book book) {
        Book existingBook = bookRepository.findByIsbn(book.getIsbn());

        if (existingBook != null) {
            return 0;
        }

        bookRepository.save(book);

        return 1;

    }
}
