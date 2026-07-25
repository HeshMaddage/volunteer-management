package com.volunteer.management.auth.dto;

public record AuthResponse(String token, String email, String role) {
}