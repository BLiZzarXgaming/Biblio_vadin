package com.example.application.service;

import com.example.application.entity.Availability;
import com.example.application.entity.DTO.AvailabilityDto;
import com.example.application.service.implementation.AvailabilityServiceV2;
import com.example.application.setupBD.DataInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("junittest")
@SpringBootTest
class AvailabilityServiceV2Test {

    @Autowired
    private AvailabilityServiceV2 availabilityServiceV2;

    @Autowired
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        try {
            dataInitializer.run(new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAll() {

    }

    @Test
    void findById() {
        Optional<AvailabilityDto> availability = availabilityServiceV2.findById(1L);

        assertNotNull(availability);
        assertTrue(availability.isPresent());
        assertEquals(1L, availability.get().getId());
    }

    @Test
    void searchByTitle() {

    }

    @Test
    void findByDate() {
    }

    @Test
    void findByStatus() {
    }

    @Test
    void findByType() {
    }

    @Test
    void findByUser() {
    }

    @Test
    void save() {
    }

    @Test
    void deleteById() {
    }

    @Test
    void findByStatusAndDateBetween() {
        LocalDate currentDate = LocalDate.now(); // ZoneId.of("America/Montreal")
        LocalDate endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        List<AvailabilityDto> availabilities = availabilityServiceV2.findByStatusAndDateBetween("Confirmed", currentDate, endOfMonth );

        assertNotNull(availabilities);
        assertEquals(6, availabilities.size());
    }
}