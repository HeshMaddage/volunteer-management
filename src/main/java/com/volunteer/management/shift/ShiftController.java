package com.volunteer.management.shift;

import com.volunteer.management.shift.dto.ShiftRequest;
import com.volunteer.management.shift.dto.ShiftResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShiftResponse> create(@PathVariable UUID eventId, @Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(shiftService.create(eventId, request));
    }

    @GetMapping
    public List<ShiftResponse> getByEvent(@PathVariable UUID eventId) {
        return shiftService.getByEvent(eventId);
    }
}