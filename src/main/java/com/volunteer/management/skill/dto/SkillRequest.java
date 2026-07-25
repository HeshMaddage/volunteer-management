package com.volunteer.management.skill.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(@NotBlank String name) {
}
