package com.example.application.repository;

import com.example.application.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    @Query(value = "SELECT * FROM availabilities WHERE status = :status AND date BETWEEN :dateStart AND :dateEnd", nativeQuery = true)
    List<Availability> findAvailabilitiesByStatusAndDateBetween(
            @Param("status") String status,
            @Param("dateStart") LocalDate dateStart,
            @Param("dateEnd") LocalDate dateEnd);
}
