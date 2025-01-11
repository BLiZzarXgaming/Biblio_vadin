package com.example.application.repository;

import com.example.application.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LoanRepositoryV2 extends JpaRepository<Loan, Long> {
    // Méthodes personnalisées
    List<Loan> findByMemberId(Long memberId);
    List<Loan> findByCopyId(Long copyId);
    List<Loan> findByStatus(String status);
    List<Loan> findByLoanDateBetween(Date startDate, Date endDate);
}
