package com.example.application.repository;

import com.example.application.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepositoryV2 extends JpaRepository<Reservation, Long> {

    // Trouver les réservations d'un membre
    List<Reservation> findByMemberId(Long memberId);

    // Trouver les réservations par copie
    List<Reservation> findByCopyId(Long copyId);

    // Trouver les réservations par statut
    List<Reservation> findByStatus(String status);

    // Requête pour trouver tous les membres distincts qui ont des réservations avec
    // un statut spécifique
    @Query("SELECT DISTINCT r.member FROM Reservation r WHERE r.status = :status")
    List<com.example.application.entity.User> findDistinctMembersByStatus(@Param("status") String status);

    // Trouver toutes les réservations qui peuvent être préparées
    // (ne pas inclure celles en attente d'autres retours)
    @Query("SELECT r FROM Reservation r WHERE r.status = 'reserved' " +
            "AND r.copy.id NOT IN (SELECT l.copy.id FROM Loan l WHERE l.status = 'borrowed')")
    List<Reservation> findReadyForPreparationReservations();

    // Trouver les réservations prêtes pour un membre spécifique
    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId " +
            "AND r.status = 'reserved' " +
            "AND r.copy.id NOT IN (SELECT l.copy.id FROM Loan l WHERE l.status = 'borrowed')")
    List<Reservation> findReadyForPreparationReservationsByMember(@Param("memberId") Long memberId);

    /**
     * Compte le nombre de réservations (prêts avec statut "RESERVED" mais pas
     * "ANNULE")
     */
    @Query("SELECT COUNT(l) FROM Reservation l WHERE l.status != 'cancelled'")
    long countReservations();
}