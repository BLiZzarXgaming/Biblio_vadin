package com.example.application.service.implementation;

import com.example.application.entity.BoardGame;
import com.example.application.entity.DTO.BoardGameDto;

import java.util.List;
import java.util.Optional;

public interface BoardGameServiceV2 {
    List<BoardGameDto> findAll();
    Optional<BoardGameDto> findById(Long id);
    List<BoardGameDto> findByRecommendedAge(int age);
    List<BoardGameDto> findByNumberOfPiecesRange(int minPieces, int maxPieces);
    Optional<BoardGameDto> findByGtin(String gtin);
    BoardGameDto save(BoardGameDto boardGame);
    BoardGameDto save(BoardGameDto boardGame, boolean isUpdate);
    void deleteById(Long id);

    int insertBoardGame(BoardGameDto boardGame);
}
