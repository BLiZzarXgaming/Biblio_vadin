package com.example.application.service.implementation;

import com.example.application.entity.Copy;
import com.example.application.entity.DTO.CopyDto;

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

    int insertCopy(CopyDto copy);
}
