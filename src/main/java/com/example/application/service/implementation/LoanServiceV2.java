package com.example.application.service.implementation;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.Loan;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LoanServiceV2 {
    List<LoanDto> findAll();
    Optional<LoanDto> findById(Long id);
    List<LoanDto> findByMember(Long memberId);
    List<LoanDto> findByCopy(Long copyId);
    List<LoanDto> findByStatus(String status);
    List<LoanDto> findByLoanDateRange(Date startDate, Date endDate);
    LoanDto save(LoanDto loan);
    void deleteById(Long id);
}
