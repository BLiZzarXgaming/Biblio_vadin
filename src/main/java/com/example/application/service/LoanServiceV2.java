package com.example.application.service;

import com.example.application.entity.DTO.LoanDto;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoanServiceV2 {
    // Méthodes existantes
    List<LoanDto> findAll();

    Optional<LoanDto> findById(Long id);

    LoanDto save(LoanDto loan);

    void deleteById(Long id);

    List<LoanDto> findByMember(Long memberId);

    List<LoanDto> findByCopy(Long copyId);

    List<LoanDto> findByStatus(String status);

    List<LoanDto> findByLoanDateRange(Date startDate, Date endDate);

    // Méthodes pour les statistiques
    int countReservations();

    Map<String, Integer> getLoansByMonth();

    Map<String, Integer> getLoansByStatus();
}