package com.volunteer.management.event.dto;

import jakarta.validation.constraints.NotBlank;

public record EventRequest(
        @NotBlank String title,
        String description,
        String location) {
}
