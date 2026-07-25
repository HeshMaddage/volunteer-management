package com.volunteer.management.volunteer.dto;

import java.time.Instant;
import java.util.Set;

public record VolunteerProfileResponse(
        String fullName,
        String phone,
        String address,
        String bio,
        Instant joinDate,
        Set<String> skills) {
}
