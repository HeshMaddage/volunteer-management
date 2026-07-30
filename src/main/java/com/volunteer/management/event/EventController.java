package com.volunteer.management.event;

import com.volunteer.management.event.dto.EventRequest;
import com.volunteer.management.event.dto.EventResponse;
import com.volunteer.management.registration.RegistrationService;

import java.nio.file.Path;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.volunteer.management.event.EventImageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final com.volunteer.management.registration.RegistrationService registrationService;
    private final EventImageService eventImageService;

    public EventController(EventService eventService, RegistrationService registrationService,
            EventImageService eventImageService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.eventImageService = eventImageService;
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

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.Map<String, String> uploadImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        String imageUrl = eventImageService.uploadImage(id, file);
        return java.util.Map.of("imageUrl", imageUrl);
    }

    @GetMapping("/{id}/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable UUID id, @PathVariable String filename)
            throws java.net.MalformedURLException {
        Path path = eventImageService.resolveImagePath(filename);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(resource);
    }
}