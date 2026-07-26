package com.volunteer.management.registration.dto;

import com.volunteer.management.registration.RegistrationStatus;

import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        UUID shiftId,
        UUID volunteerId,
        String volunteerName,
        RegistrationStatus status,
        Instant registeredAt) {
}