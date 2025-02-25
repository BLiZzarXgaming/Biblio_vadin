package com.example.application.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.application.entity.DTO.BoardGameDto;
import com.example.application.entity.DTO.BookDto;
import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.MagazineDto;

import jakarta.transaction.Transactional;

@Service
public class DocumentAdditionService {
    private final ItemServiceV2 itemService;
    private final BookServiceV2 bookService;
    private final MagazineServiceV2 magazineService;
    private final BoardGameServiceV2 boardGameService;
    private final CopyServiceV2 copyService;

    public DocumentAdditionService(ItemServiceV2 itemService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            CopyServiceV2 copyService) {
        this.itemService = itemService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.copyService = copyService;
    }

    @Transactional
    public void addCopiesToExistingDocument(ItemDto existingItem, List<CopyDto> copies) {
        saveAllCopies(copies, existingItem);
    }

    @Transactional
    public ItemDto addBook(ItemDto item, BookDto book, List<CopyDto> copies) {
        ItemDto savedItem = itemService.save(item);
        book.setItem(savedItem);
        bookService.insertBook(book);
        saveAllCopies(copies, savedItem);
        return savedItem;
    }

    @Transactional
    public ItemDto addMagazine(ItemDto item, MagazineDto magazine, List<CopyDto> copies) {
        ItemDto savedItem = itemService.save(item);
        magazine.setItem(savedItem);
        magazineService.insertMagazine(magazine);
        saveAllCopies(copies, savedItem);
        return savedItem;
    }

    @Transactional
    public ItemDto addBoardGame(ItemDto item, BoardGameDto boardGame, List<CopyDto> copies) {
        ItemDto savedItem = itemService.save(item);
        boardGame.setItem(savedItem);
        boardGameService.insertBoardGame(boardGame);
        saveAllCopies(copies, savedItem);
        return savedItem;
    }

    private void saveAllCopies(List<CopyDto> copies, ItemDto item) {
        for (CopyDto copy : copies) {
            copy.setItem(item);
            copyService.insertCopy(copy);
        }
    }
}