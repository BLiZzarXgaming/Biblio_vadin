package com.example.application.service;

import com.example.application.entity.BoardGame;
import com.example.application.entity.DTO.BoardGameDto;
import com.example.application.entity.Mapper.BoardGameMapper;
import com.example.application.repository.BoardGameRepositoryV2;
import com.example.application.service.implementation.BoardGameServiceV2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardGameServiceImplV2 implements BoardGameServiceV2 {
    private final BoardGameRepositoryV2 boardGameRepository;
    private final BoardGameMapper boardGameMapper;

    public BoardGameServiceImplV2(BoardGameRepositoryV2 boardGameRepository, BoardGameMapper boardGameMapper) {
        this.boardGameRepository = boardGameRepository;
        this.boardGameMapper = boardGameMapper;
    }

    @Override
    public List<BoardGameDto> findAll() {
        return boardGameRepository.findAll().stream().map(boardGameMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<BoardGameDto> findById(Long id) {
        return boardGameRepository.findById(id).map(boardGameMapper::toDto);
    }

    @Override
    public List<BoardGameDto> findByRecommendedAge(int age) {
        return boardGameRepository.findByRecommendedAge(age).stream().map(boardGameMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BoardGameDto> findByNumberOfPiecesRange(int minPieces, int maxPieces) {
        return boardGameRepository.findByNumberOfPiecesBetween(minPieces, maxPieces).stream().map(boardGameMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<BoardGameDto> findByGtin(String gtin) {
        return boardGameRepository.findByGtin(gtin).stream().findFirst().map(boardGameMapper::toDto);
    }

    @Override
    public BoardGameDto save(BoardGameDto boardGame) {
        // Vérification d'unicité pour le GTIN
        Optional<BoardGame> existingBoardGame = boardGameRepository.findByGtin(boardGame.getGtin());
        if (existingBoardGame.isPresent()) {
            throw new IllegalArgumentException("A board game with the same GTIN already exists.");
        }
        return boardGameMapper.toDto(boardGameRepository.save(boardGameMapper.toEntity( boardGame)));
    }

    @Override
    public BoardGameDto save(BoardGameDto boardGame, boolean isUpdate) {
        // Vérification d'unicité pour le GTIN
        Optional<BoardGame> existingBoardGame = boardGameRepository.findByGtin(boardGame.getGtin());
        if (existingBoardGame.isPresent() && !isUpdate) {
            throw new IllegalArgumentException("A board game with the same GTIN already exists.");
        }
        return boardGameMapper.toDto(boardGameRepository.save(boardGameMapper.toEntity( boardGame)));
    }

    @Override
    public void deleteById(Long id) {
        boardGameRepository.deleteById(id);
    }

    @Override
    public int insertBoardGame(BoardGameDto boardGame) {

        Optional<BoardGame> existingBoardGame = boardGameRepository.findByGtin(boardGame.getGtin());
        if (existingBoardGame.isPresent()) {
            throw new IllegalArgumentException("A board game with the same GTIN already exists.");
        }

        return boardGameRepository.insertBoardGame(boardGame.getNumberOfPieces(), boardGame.getRecommendedAge(), boardGame.getGameRules(), boardGame.getGtin(), boardGame.getItem().getId());
    }
}
