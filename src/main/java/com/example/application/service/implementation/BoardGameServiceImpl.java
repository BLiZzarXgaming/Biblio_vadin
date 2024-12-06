package com.example.application.service.implementation;

import com.example.application.entity.BoardGame;
import com.example.application.repository.BoardGameRepository;
import org.springframework.stereotype.Service;

@Service
public class BoardGameServiceImpl {

    private BoardGameRepository boardGameRepository;

    public BoardGameServiceImpl(BoardGameRepository boardGameRepository) {
        this.boardGameRepository = boardGameRepository;
    }

    public int save(BoardGame boardGame) {
        BoardGame existingBoardGame = boardGameRepository.findByGtin(boardGame.getGtin()).orElse(null);

        if (existingBoardGame != null) {
            return 0;
        }

        boardGameRepository.save(boardGame);

        return 1;
    }

    public BoardGame findByGtin(String gtin) {
        return boardGameRepository.findByGtin(gtin).orElse(null);
    }

    public BoardGame findByItemId(long itemId) {
        return boardGameRepository.findByItemId(itemId).orElse(null);
    }
}
