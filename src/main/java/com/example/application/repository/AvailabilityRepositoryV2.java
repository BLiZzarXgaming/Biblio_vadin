package com.example.application.repository;

import com.example.application.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface AvailabilityRepositoryV2 extends JpaRepository<Availability, Long> {
    List<Availability> findByTitleContaining(String keyword);
    List<Availability> findByDate(LocalDate date);
    List<Availability> findByStatus(String status);
    List<Availability> findByType(String type);
    List<Availability> findByUserId(Long userId);
    List<Availability> findByStatusAndDateBetween(String status, LocalDate dateStart, LocalDate dateEnd);
}
