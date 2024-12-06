package com.example.application.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    private static final ZoneId zoneId = ZoneId.of("America/Montreal");

    static public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(zoneId)
                .toLocalDate();
    }

    static public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(zoneId)
                .toInstant());
    }
}
