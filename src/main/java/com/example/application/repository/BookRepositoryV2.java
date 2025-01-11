package com.example.application.repository;

import com.example.application.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BookRepositoryV2  extends JpaRepository<Book,Long> {
    List<Book> findByAuthorContaining(String author);
    List<Book> findByIsbn(String isbn);
    List<Book> findByPublicationDateBetween(java.util.Date startDate, java.util.Date endDate);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO books (item_id, author, isbn, publication_date) VALUES (:item_id, :author, :isbn, :publicationDate)", nativeQuery = true)
    int insertBook(@Param("item_id") Long itemID,@Param("author") String author,@Param("isbn") String isbn,@Param("publicationDate") java.util.Date publicationDate);
}
