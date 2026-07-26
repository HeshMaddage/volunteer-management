package com.volunteer.management.registration.dto;

import com.volunteer.management.registration.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceUpdateRequest(@NotNull RegistrationStatus status) {
}
