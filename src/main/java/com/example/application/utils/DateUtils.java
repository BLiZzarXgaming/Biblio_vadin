package com.example.application.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    private static final ZoneId zoneId = ZoneId.of("America/Montreal");

    static public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        if (dateToConvert == null) {
            throw new IllegalArgumentException("The dateToConvert parameter cannot be null");
        }
        Instant instant = new java.util.Date(dateToConvert.getTime()).toInstant();

        LocalDate localDate = instant.atZone(zoneId).toLocalDate();


        return localDate;
    }

    static public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(zoneId)
                .toInstant());
    }
}
