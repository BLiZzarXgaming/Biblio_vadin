package com.example.application.service.implementation;

import com.example.application.entity.DTO.ReservationDto;
import com.example.application.entity.DTO.UserDto;

import java.util.List;
import java.util.Optional;

public interface ReservationServiceV2 {
    List<ReservationDto> findAll();

    Optional<ReservationDto> findById(Long id);

    List<ReservationDto> findByMember(Long memberId);

    List<ReservationDto> findByCopy(Long copyId);

    List<ReservationDto> findByStatus(String status);

    ReservationDto save(ReservationDto reservation);

    void deleteById(Long id);

    /**
     * Trouve tous les membres distincts qui ont des réservations avec un statut
     * spécifique
     * 
     * @param status Le statut des réservations à rechercher
     * @return Liste des membres
     */
    List<UserDto> findDistinctMembersByStatus(String status);

    /**
     * Trouve toutes les réservations qui peuvent être préparées
     * (exclut les réservations pour des documents actuellement empruntés)
     * 
     * @return Liste des réservations prêtes à être préparées
     */
    List<ReservationDto> findReadyForPreparationReservations();

    /**
     * Trouve les réservations prêtes à être préparées pour un membre spécifique
     * 
     * @param memberId ID du membre
     * @return Liste des réservations prêtes pour ce membre
     */
    List<ReservationDto> findReadyForPreparationReservationsByMember(Long memberId);

    /**
     * Marque une réservation comme prête pour être récupérée
     * 
     * @param reservationId ID de la réservation
     * @return La réservation mise à jour
     */
    ReservationDto markAsReady(Long reservationId);

    /**
     * Marque une réservation comme non-prête
     * 
     * @param reservationId ID de la réservation
     * @return La réservation mise à jour
     */
    ReservationDto markAsNotReady(Long reservationId);

    int countReservations();
}