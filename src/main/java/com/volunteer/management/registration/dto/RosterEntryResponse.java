package com.volunteer.management.registration.dto;

import com.volunteer.management.registration.RegistrationStatus;

import java.time.Instant;
import java.util.UUID;

public record RosterEntryResponse(
        UUID registrationId,
        UUID shiftId,
        UUID volunteerId,
        String volunteerName,
        RegistrationStatus status,
        Instant registeredAt) {
}
