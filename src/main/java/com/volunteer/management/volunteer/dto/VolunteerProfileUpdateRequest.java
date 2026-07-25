package com.volunteer.management.volunteer.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record VolunteerProfileUpdateRequest(
        @NotBlank String fullName,
        String phone,
        String address,
        String bio,
        Set<String> skills // skill names; unknown names are created on the fly
) {
}