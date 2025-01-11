package com.example.application.service.implementation;

import com.example.application.entity.DTO.AvailabilityDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailabilityServiceV2 {
    List<AvailabilityDto> findAll();
    Optional<AvailabilityDto> findById(Long id);
    List<AvailabilityDto> searchByTitle(String keyword);
    List<AvailabilityDto> findByDate(LocalDate date);
    List<AvailabilityDto> findByStatus(String status);
    List<AvailabilityDto> findByType(String type);
    List<AvailabilityDto> findByUser(Long userId);
    AvailabilityDto save(AvailabilityDto availability);
    void deleteById(Long id);
    List<AvailabilityDto> findByStatusAndDateBetween(String status, LocalDate dateStart, LocalDate dateEnd);
}
