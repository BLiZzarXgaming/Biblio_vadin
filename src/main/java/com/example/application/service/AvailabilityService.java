package com.example.application.service;

import com.example.application.entity.Availability;

import java.time.LocalDate;
import java.util.List;


public interface AvailabilityService {

    public List<Availability> findAvailabilitiesByStatusAndDateBetween(String status, LocalDate dateStart, LocalDate dateEnd);
}
