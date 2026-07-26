package com.volunteer.management.registration;

import com.volunteer.management.auth.UserRepository;
import com.volunteer.management.registration.dto.RegistrationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;

    public RegistrationController(RegistrationService registrationService, UserRepository userRepository) {
        this.registrationService = registrationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/v1/shifts/{shiftId}/register")
    public ResponseEntity<RegistrationResponse> register(@PathVariable UUID shiftId, Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(registrationService.register(shiftId, userId));
    }

    @DeleteMapping("/api/v1/registrations/{registrationId}")
    public ResponseEntity<Void> cancel(@PathVariable UUID registrationId, Authentication authentication) {
        UUID userId = currentUserId(authentication);
        registrationService.cancel(registrationId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow()
                .getId();
    }

    @PatchMapping("/api/v1/registrations/{registrationId}/attendance")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public com.volunteer.management.registration.dto.RegistrationResponse markAttendance(
            @PathVariable UUID registrationId,
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid com.volunteer.management.registration.dto.AttendanceUpdateRequest request) {
        return registrationService.markAttendance(registrationId, request);
    }
}