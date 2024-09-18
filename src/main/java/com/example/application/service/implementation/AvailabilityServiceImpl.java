package com.example.application.service.implementation;

import com.example.application.entity.Availability;
import com.example.application.repository.AvailabilityRepository;
import com.example.application.service.AvailabilityService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    public AvailabilityServiceImpl(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @AnonymousAllowed
    @Override
    public List<Availability> findAvailabilitiesByStatusAndDateBetween(String status, LocalDate dateStart, LocalDate dateEnd) {
        return availabilityRepository.findAvailabilitiesByStatusAndDateBetween(status, dateStart, dateEnd);
    }
}
