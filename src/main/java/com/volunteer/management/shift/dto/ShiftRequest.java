package com.volunteer.management.shift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;

public record ShiftRequest(
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @Min(1) Integer capacity,
        Set<String> requiredSkills // skill names
) {
}