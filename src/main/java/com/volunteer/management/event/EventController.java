package com.volunteer.management.event;

import com.volunteer.management.event.dto.EventRequest;
import com.volunteer.management.event.dto.EventResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final com.volunteer.management.registration.RegistrationService registrationService;

    public EventController(EventService eventService,
            com.volunteer.management.registration.RegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.create(request));
    }

    @GetMapping
    public Page<EventResponse> browse(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return eventService.browse(status, skill, from, to, pageable);
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable UUID id) {
        return eventService.getById(id);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse cancel(@PathVariable UUID id) {
        return eventService.cancel(id);
    }

    @GetMapping("/{id}/roster")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<com.volunteer.management.registration.dto.RosterEntryResponse> getRoster(
            @PathVariable UUID id) {
        return registrationService.getRoster(id);
    }
}