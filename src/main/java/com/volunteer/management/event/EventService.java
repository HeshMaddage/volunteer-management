package com.volunteer.management.event;

import com.volunteer.management.event.dto.EventRequest;
import com.volunteer.management.event.dto.EventResponse;
import com.volunteer.management.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse create(EventRequest request) {
        Event event = Event.builder()
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .build();
        return toResponse(eventRepository.save(event));
    }

    public Page<EventResponse> browse(EventStatus status, String skill, Instant from, Instant to, Pageable pageable) {
        var spec = EventSpecification.buildFilter(status, skill, from, to);
        return eventRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public EventResponse getById(UUID id) {
        return toResponse(findEventOrThrow(id));
    }

    @org.springframework.transaction.annotation.Transactional
    public EventResponse cancel(UUID id) {
        Event event = findEventOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        return toResponse(event);
    }

    public Event findEventOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(), event.getTitle(), event.getDescription(),
                event.getLocation(), event.getStatus(), event.getCreatedAt());
    }
}