package com.example.application.repository;

import com.example.application.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Nouvelles méthodes pour les statistiques



    /**
     * Compte le nombre de prêts par mois pour une période donnée
     * 
     * @param startDate Date de début de la période
     * @param endDate   Date de fin de la période
     * @return Liste d'objets [yearMonth, count] où yearMonth est au format YYYY-MM
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', l.loanDate, '%Y-%m') as yearMonth, COUNT(l.id) " +
            "FROM Loan l " +
            "WHERE l.loanDate BETWEEN :startDate AND :endDate " +
            "GROUP BY yearMonth " +
            "ORDER BY yearMonth")
    List<Object[]> countLoansByMonth(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Compte le nombre de prêts par statut pour une période donnée
     * 
     * @param startDate Date de début de la période
     * @param endDate   Date de fin de la période
     * @return Liste d'objets [status, count]
     */
    @Query("SELECT l.status, COUNT(l.id) " +
            "FROM Loan l " +
            "WHERE l.loanDate BETWEEN :startDate AND :endDate " +
            "GROUP BY l.status")
    List<Object[]> countLoansByStatus(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
