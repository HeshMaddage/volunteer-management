package com.volunteer.management.hourslog.dto;

import java.time.Instant;
import java.util.UUID;

public record HoursLogResponse(
        UUID id,
        UUID shiftId,
        String eventTitle,
        Double hours,
        Instant loggedAt) {
}
