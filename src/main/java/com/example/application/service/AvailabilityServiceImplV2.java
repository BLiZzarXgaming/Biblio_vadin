package com.example.application.service;

import com.example.application.entity.Availability;
import com.example.application.entity.DTO.AvailabilityDto;
import com.example.application.entity.Mapper.AvailabilityMapper;
import com.example.application.repository.AvailabilityRepositoryV2;
import com.example.application.service.implementation.AvailabilityServiceV2;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AvailabilityServiceImplV2 implements AvailabilityServiceV2 {
    private final AvailabilityRepositoryV2 availabilityRepository;
    private final AvailabilityMapper availabilityMapper;
    private static final Logger log = LoggerFactory.getLogger(AvailabilityServiceImplV2.class);

    public AvailabilityServiceImplV2(AvailabilityRepositoryV2 availabilityRepository,
            AvailabilityMapper availabilityMapper) {
        this.availabilityRepository = availabilityRepository;
        this.availabilityMapper = availabilityMapper;
    }

    @Override
    public List<AvailabilityDto> findAll() {

        return availabilityRepository.findAll().stream().map(availabilityMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<AvailabilityDto> findById(Long id) {
        return Optional.ofNullable(availabilityRepository.findById(id).map(availabilityMapper::toDto).orElse(null));
    }

    @Override
    public List<AvailabilityDto> searchByTitle(String keyword) {
        return availabilityRepository.findByTitleContaining(keyword).stream().map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDto> findByDate(LocalDate date) {
        return availabilityRepository.findByDate(date).stream().map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDto> findByStatus(String status) {
        return availabilityRepository.findByStatus(status).stream().map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDto> findByType(String type) {
        return availabilityRepository.findByType(type).stream().map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDto> findByTypeWithPagination(String type, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("date").descending());
        return availabilityRepository.findByType(type, pageable).stream()
                .map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countByType(String type) {
        return availabilityRepository.countByType(type);
    }

    @Override
    public List<AvailabilityDto> findByUser(Long userId) {
        return availabilityRepository.findByUserId(userId).stream().map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AvailabilityDto save(AvailabilityDto availability) {

        Availability entity = availabilityMapper.toEntity(availability);

        return availabilityMapper.toDto(availabilityRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        try {
            // Vérifier si l'ID est null
            if (id == null) {
                String errorMsg = "Impossible de supprimer: l'ID fourni est null";
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            // Vérifier si l'élément existe avant de le supprimer
            if (!availabilityRepository.existsById(id)) {
                String errorMsg = "Impossible de supprimer: aucun élément trouvé avec l'ID " + id;
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            // Procéder à la suppression
            availabilityRepository.deleteById(id);
            log.info("Élément avec ID {} supprimé avec succès", id);
        } catch (IllegalArgumentException e) {
            // Ces exceptions sont déjà loggées et peuvent être propagées
            throw e;
        } catch (Exception e) {
            String errorMsg = "Erreur lors de la suppression de l'élément avec ID " + id;
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    @AnonymousAllowed
    public List<AvailabilityDto> findByStatusAndDateBetween(String status, LocalDate dateStart, LocalDate dateEnd) {
        return availabilityRepository.findByStatusAndDateBetween(status, dateStart, dateEnd).stream()
                .map(availabilityMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDto> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return availabilityRepository.findByDateBetween(startDate, endDate).stream()
                .map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDto> findByTypeAndDateBetween(String type, LocalDate startDate, LocalDate endDate) {
        return availabilityRepository.findByTypeAndDateBetween(type, startDate, endDate).stream()
                .map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDto> findByStatusAndType(String status, String type) {
        return availabilityRepository.findByStatusAndType(status, type).stream()
                .map(availabilityMapper::toDto)
                .collect(Collectors.toList());
    }
}
