package com.example.application.repository;

import com.example.application.entity.Item;
import com.example.application.entity.Magazine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MagazineRepository extends JpaRepository<Magazine, Long> {

    @Query("SELECT COUNT(m) FROM Magazine m JOIN m.item i WHERE "
            + "(:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
            + "(:isni IS NULL OR m.isni LIKE CONCAT('%', :isni, '%')) AND "
            + "(:month IS NULL OR m.month = :month) AND "
            + "(:publicationDate IS NULL OR m.publicationDate = :publicationDate) AND "
            + "(:category IS NULL OR i.category = :category) AND "
            + "(:publisher IS NULL OR i.publisher = :publisher)")
    int countByCriteria(@Param("title") String title,
                        @Param("isni") String isni,
                        @Param("month") String month,
                        @Param("publicationDate") LocalDate publicationDate,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher);

    // Méthode pour trouver un Magazine par son itemId
    Optional<Magazine> findByItemId(Long itemId);

    @Query(value = "SELECT * FROM magazines WHERE isni = :isni AND month = :month AND year = :year", nativeQuery = true)
    Magazine findByIsniAndMonthAndYear(@Param("isni") String isni, @Param("month") String month, @Param("year") String year);
}
