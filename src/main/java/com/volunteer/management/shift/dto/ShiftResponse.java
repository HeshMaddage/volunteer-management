package com.volunteer.management.shift.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ShiftResponse(
        UUID id,
        UUID eventId,
        Instant startTime,
        Instant endTime,
        Integer capacity,
        int registeredCount,
        Set<String> requiredSkills) {
}