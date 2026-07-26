package com.volunteer.management.event.dto;

import com.volunteer.management.event.EventStatus;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        String location,
        EventStatus status,
        Instant createdAt) {
}
