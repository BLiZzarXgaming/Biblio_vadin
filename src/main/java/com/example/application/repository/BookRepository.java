package com.example.application.repository;

import com.example.application.entity.Book;
import com.example.application.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT COUNT(b) FROM Book b JOIN b.item i WHERE "
            + "(:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
            + "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND "
            + "(:isbn IS NULL OR b.isbn LIKE CONCAT('%', :isbn, '%')) AND "
            + "(:publicationDate IS NULL OR b.publicationDate = :publicationDate) AND "
            + "(:category IS NULL OR i.category = :category) AND "
            + "(:publisher IS NULL OR i.publisher = :publisher)")
    int countByCriteria(@Param("title") String title,
                        @Param("author") String author,
                        @Param("isbn") String isbn,
                        @Param("publicationDate") LocalDate publicationDate,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher);

    // Méthode pour trouver un Book par son itemId
    Optional<Book> findByItemId(Long itemId);
}
