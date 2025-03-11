package com.example.application.service;

import com.example.application.entity.DTO.ReservationDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.Mapper.ReservationMapper;
import com.example.application.entity.Mapper.UserMapper;
import com.example.application.repository.ReservationRepositoryV2;
import com.example.application.service.implementation.ReservationServiceV2;
import com.example.application.utils.StatusUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImplV2 implements ReservationServiceV2 {

    private static final Logger LOGGER = Logger.getLogger(LoanServiceImplV2.class.getName());

    private final ReservationRepositoryV2 reservationRepository;
    private final ReservationMapper reservationMapper;
    private final UserMapper userMapper;

    public ReservationServiceImplV2(ReservationRepositoryV2 reservationRepository,
            ReservationMapper reservationMapper,
            UserMapper userMapper) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<ReservationDto> findAll() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReservationDto> findById(Long id) {
        return reservationRepository.findById(id).map(reservationMapper::toDto);
    }

    @Override
    public List<ReservationDto> findByMember(Long memberId) {
        return reservationRepository.findByMemberId(memberId).stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> findByCopy(Long copyId) {
        return reservationRepository.findByCopyId(copyId).stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> findByStatus(String status) {
        return reservationRepository.findByStatus(status).stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDto save(ReservationDto reservation) {
        return reservationMapper.toDto(
                reservationRepository.save(
                        reservationMapper.toEntity(reservation)));
    }

    @Override
    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public List<UserDto> findDistinctMembersByStatus(String status) {
        return reservationRepository.findDistinctMembersByStatus(status).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> findReadyForPreparationReservations() {
        return reservationRepository.findReadyForPreparationReservations().stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> findReadyForPreparationReservationsByMember(Long memberId) {
        return reservationRepository.findReadyForPreparationReservationsByMember(memberId).stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDto markAsReady(Long reservationId) {
        Optional<ReservationDto> reservationOpt = findById(reservationId);
        if (reservationOpt.isPresent()) {
            ReservationDto reservation = reservationOpt.get();
            reservation.setStatus(StatusUtils.ReservationStatus.READY);
            return save(reservation);
        }
        throw new IllegalArgumentException("Réservation non trouvée avec l'ID: " + reservationId);
    }

    @Override
    public ReservationDto markAsNotReady(Long reservationId) {
        Optional<ReservationDto> reservationOpt = findById(reservationId);
        if (reservationOpt.isPresent()) {
            ReservationDto reservation = reservationOpt.get();
            reservation.setStatus(StatusUtils.ReservationStatus.PENDING);
            return save(reservation);
        }
        throw new IllegalArgumentException("Réservation non trouvée avec l'ID: " + reservationId);
    }

    @Override
    public int countReservations() {
        try {
            int count = (int) reservationRepository.countReservations();
            // Si aucune réservation, ne pas retourner de valeur par défaut
            return count;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du comptage des réservations", e);
            return 0;
        }
    }
}