package com.volunteer.management.volunteer;

import com.volunteer.management.volunteer.dto.VolunteerProfileResponse;
import com.volunteer.management.volunteer.dto.VolunteerProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/volunteers/me")
public class VolunteerProfileController {

    private final VolunteerProfileService volunteerProfileService;

    public VolunteerProfileController(VolunteerProfileService volunteerProfileService) {
        this.volunteerProfileService = volunteerProfileService;
    }

    @GetMapping
    public VolunteerProfileResponse getMyProfile(Authentication authentication) {
        return volunteerProfileService.getMyProfile(authentication.getName());
    }

    @PutMapping
    public VolunteerProfileResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody VolunteerProfileUpdateRequest request) {
        return volunteerProfileService.updateMyProfile(authentication.getName(), request);
    }
}
