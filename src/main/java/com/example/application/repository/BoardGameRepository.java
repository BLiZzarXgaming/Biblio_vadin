package com.example.application.repository;

import com.example.application.entity.BoardGame;
import com.example.application.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Long> {

    @Query("SELECT COUNT(g) FROM BoardGame g JOIN g.item i WHERE "
            + "(:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND "
            + "(:numberOfPieces IS NULL OR g.numberOfPieces = :numberOfPieces) AND "
            + "(:recommendedAge IS NULL OR g.recommendedAge = :recommendedAge) AND "
            + "(:category IS NULL OR i.category = :category) AND "
            + "(:publisher IS NULL OR i.publisher = :publisher)")
    int countByCriteria(@Param("title") String title,
                        @Param("numberOfPieces") Integer numberOfPieces,
                        @Param("recommendedAge") Integer recommendedAge,
                        @Param("category") Long category,
                        @Param("publisher") Long publisher);

    // Méthode pour trouver un BoardGame par son itemId
    Optional<BoardGame> findByItemId(Long itemId);
}
