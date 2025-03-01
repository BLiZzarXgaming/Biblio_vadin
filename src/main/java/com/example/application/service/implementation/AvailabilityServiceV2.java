package com.example.application.service.implementation;

import com.example.application.entity.DTO.AvailabilityDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailabilityServiceV2 {
    List<AvailabilityDto> findAll();

    Optional<AvailabilityDto> findById(Long id);

    List<AvailabilityDto> searchByTitle(String keyword);

    List<AvailabilityDto> findByDate(LocalDate date);

    List<AvailabilityDto> findByStatus(String status);

    List<AvailabilityDto> findByType(String type);

    List<AvailabilityDto> findByUser(Long userId);

    AvailabilityDto save(AvailabilityDto availability);

    void deleteById(Long id);

    List<AvailabilityDto> findByStatusAndDateBetween(String status, LocalDate dateStart, LocalDate dateEnd);

    // New methods needed for calendar management
    /**
     * Find all availabilities between two dates regardless of status
     * 
     * @param startDate The start date (inclusive)
     * @param endDate   The end date (inclusive)
     * @return List of availabilities in the date range
     */
    List<AvailabilityDto> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find all availabilities for a specific type and date range
     * 
     * @param type      The type of availability (e.g., "event" or "heureOuverture")
     * @param startDate The start date (inclusive)
     * @param endDate   The end date (inclusive)
     * @return List of availabilities matching the criteria
     */
    List<AvailabilityDto> findByTypeAndDateBetween(String type, LocalDate startDate, LocalDate endDate);

    /**
     * Find all availabilities by status and type
     * 
     * @param status The status (e.g., "Confirmed", "Draft", "Cancelled")
     * @param type   The type of availability
     * @return List of availabilities matching the criteria
     */
    List<AvailabilityDto> findByStatusAndType(String status, String type);

    /**
     * Find availabilities by type with pagination
     * 
     * @param type   The type of availability (e.g., "event" or "heureOuverture")
     * @param offset The offset for pagination
     * @param limit  The maximum number of results to return
     * @return List of availabilities matching the criteria with pagination
     */
    List<AvailabilityDto> findByTypeWithPagination(String type, int offset, int limit);

    /**
     * Count the total number of availabilities by type
     * 
     * @param type The type of availability (e.g., "event" or "heureOuverture")
     * @return The total count of availabilities of the specified type
     */
    long countByType(String type);
}
