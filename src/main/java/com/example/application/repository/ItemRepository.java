package com.example.application.repository;

import com.example.application.entity.Category;
import com.example.application.entity.Item;
import com.example.application.entity.Publisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(value = "SELECT * FROM items WHERE "
            + "(:keyword IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:category IS NULL OR category = :category) AND "
            + "(:publisher IS NULL OR publisher_id = :publisher) ORDER BY title", nativeQuery = true)
    List<Item> findByCriteriaWithPagination(@Param("keyword") String keyword,
                                            @Param("category") Long category,
                                            @Param("publisher") Long publisher,
                                            Pageable pageable);

    @Query(value = "SELECT i.* FROM items i " +
            "    inner join board_games g on i.id = g.item_id " +
            "WHERE " +
            "    (:numberOfPieces IS NULL OR g.number_of_pieces = :numberOfPieces) AND " +
            "    (:recommendedAge IS NULL OR g.recommended_age = :recommendedAge) AND " +
            "    (:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "    (:category IS NULL OR i.category = :category) AND " +
            "    (:publisher IS NULL OR i.publisher_id = :publisher) ", nativeQuery = true)
    List<Item> findBoardGameByCriteriaWithPagination(@Param("title") String title,
                                                     @Param("numberOfPieces") Integer numberOfPieces,
                                                     @Param("recommendedAge") Integer recommendedAge,
                                                     @Param("category") Long category,
                                                     @Param("publisher") Long publisher,
                                                     Pageable pageable);

    @Query(value = "SELECT i.* FROM items i " +
            "    inner join magazines m on i.id = m.item_id " +
            "WHERE " +
            "    (:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "    (:isni IS NULL OR m.isni = :isni OR :isni = '') AND " +
            "    (:month IS NULL OR m.month = :month OR :month = '') AND " +
            "    (:publicationDate IS NULL OR m.publication_date = :publicationDate) AND " +
            "    (:category IS NULL OR i.category = :category) AND " +
            "    (:publisher IS NULL OR i.publisher_id = :publisher) ", nativeQuery = true)
    List<Item> findMagazineByCriteriaWithPagination(@Param("title") String title,
                                                    @Param("isni") String isni,
                                                    @Param("month") String month,
                                                    @Param("publicationDate") LocalDate publicationDate,
                                                    @Param("category") Long category,
                                                    @Param("publisher") Long publisher,
                                                    Pageable pageable);

    @Query(value = "SELECT i.* FROM items i " +
            "    inner join books b on i.id = b.item_id " +
            "WHERE  " +
            "    (:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "    (:isbn IS NULL OR b.isbn = :isbn OR :isbn = '') AND " +
            "    (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "    (:publicationDate IS NULL OR b.publication_date = :publicationDate) AND " +
            "    (:category IS NULL OR i.category = :category) AND " +
            "    (:publisher IS NULL OR i.publisher_id = :publisher) ", nativeQuery = true)
    List<Item> findBookByCriteriaWithPagination(@Param("title") String title,
                                                @Param("author") String author,
                                                @Param("isbn") String isbn,
                                                @Param("publicationDate") LocalDate publicationDate,
                                                @Param("category") Long category,
                                                @Param("publisher") Long publisher,
                                                Pageable pageable);

    @Query("SELECT COUNT(i) FROM Item i WHERE "
            + "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:category IS NULL OR i.category = :category) AND "
            + "(:publisher IS NULL OR i.publisher = :publisher)")
    int countByCriteria(@Param("keyword") String keyword,
                        @Param("category") Category category,
                        @Param("publisher") Publisher publisher);

    // Méthode pour trouver un Item par son ID
    @Override
    @Query(value = "SELECT * FROM items WHERE id = :id", nativeQuery = true)
    Optional<Item> findById(@Param("id") Long id);
}
