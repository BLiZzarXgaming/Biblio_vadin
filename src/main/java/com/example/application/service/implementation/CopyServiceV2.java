package com.example.application.service.implementation;

import com.example.application.entity.Copy;
import com.example.application.entity.DTO.CopyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CopyServiceV2 {
    List<CopyDto> findAll();

    Optional<CopyDto> findById(Long id);

    List<CopyDto> findByStatus(String status);

    List<CopyDto> findByItem(Long itemId);

    List<CopyDto> findByPriceRange(double minPrice, double maxPrice);

    CopyDto save(CopyDto copy);

    void deleteById(Long id);

    // Méthodes de pagination
    Page<CopyDto> findAllPaginated(Pageable pageable);

    Page<CopyDto> findBySearchTerm(String searchTerm, Pageable pageable);

    Page<CopyDto> findByStatusPaginated(String status, Pageable pageable);

    Page<CopyDto> findBySearchTermAndStatus(String searchTerm, String status, Pageable pageable);

    Page<CopyDto> findByAcquisitionDate(LocalDate date, Pageable pageable);

    Page<CopyDto> findBySearchTermAndAcquisitionDate(String searchTerm, LocalDate date, Pageable pageable);

    Page<CopyDto> findByStatusAndAcquisitionDate(String status, LocalDate date, Pageable pageable);

    Page<CopyDto> findBySearchTermAndStatusAndAcquisitionDate(String searchTerm, String status, LocalDate date,
            Pageable pageable);

    // Obtenir le nombre total de copies (pour la pagination)
    long count();

    int insertCopy(CopyDto copy);
}
