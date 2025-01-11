package com.example.application.repository;

import com.example.application.entity.BoardGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardGameRepositoryV2 extends JpaRepository<BoardGame,Long> {
    // Méthodes personnalisées
    List<BoardGame> findByRecommendedAge(int age);
    List<BoardGame> findByNumberOfPiecesBetween(int minPieces, int maxPieces);
    Optional<BoardGame> findByGtin(String gtin);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO board_games (number_of_pieces, recommended_age, game_rules, gtin, item_id) VALUES (:numberOfPieces, :recommendedAge, :gameRules, :gtin, :itemId)", nativeQuery = true)
    int insertBoardGame(@Param("numberOfPieces") int numberOfPieces, @Param("recommendedAge") int recommendedAge,@Param("gameRules") String gameRules,@Param("gtin") String gtin,@Param("itemId") Long itemId);
}
