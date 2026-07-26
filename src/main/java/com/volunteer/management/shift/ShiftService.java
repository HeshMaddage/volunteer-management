package com.volunteer.management.shift;

import com.volunteer.management.event.Event;
import com.volunteer.management.event.EventService;
import com.volunteer.management.exception.ResourceNotFoundException;
import com.volunteer.management.registration.RegistrationRepository;
import com.volunteer.management.registration.RegistrationStatus;
import com.volunteer.management.shift.dto.ShiftRequest;
import com.volunteer.management.shift.dto.ShiftResponse;
import com.volunteer.management.skill.Skill;
import com.volunteer.management.skill.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EventService eventService;
    private final SkillRepository skillRepository;
    private final RegistrationRepository registrationRepository;

    public ShiftService(
            ShiftRepository shiftRepository,
            EventService eventService,
            SkillRepository skillRepository,
            RegistrationRepository registrationRepository) {
        this.shiftRepository = shiftRepository;
        this.eventService = eventService;
        this.skillRepository = skillRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public ShiftResponse create(UUID eventId, ShiftRequest request) {
        Event event = eventService.findEventOrThrow(eventId);

        Set<Skill> skills = new HashSet<>();
        if (request.requiredSkills() != null) {
            for (String name : request.requiredSkills()) {
                skills.add(skillRepository.findByName(name)
                        .orElseGet(() -> skillRepository.save(Skill.builder().name(name).build())));
            }
        }

        Shift shift = Shift.builder()
                .event(event)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .capacity(request.capacity())
                .requiredSkills(skills)
                .build();

        return toResponse(shiftRepository.save(shift));
    }

    public List<ShiftResponse> getByEvent(UUID eventId) {
        return shiftRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    Shift findShiftOrThrow(UUID id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
    }

    private ShiftResponse toResponse(Shift shift) {
        long activeCount = registrationRepository.countByShiftIdAndStatusIn(
                shift.getId(),
                List.of(RegistrationStatus.REGISTERED, RegistrationStatus.ATTENDED));

        Set<String> skillNames = shift.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        return new ShiftResponse(
                shift.getId(), shift.getEvent().getId(), shift.getStartTime(),
                shift.getEndTime(), shift.getCapacity(), (int) activeCount, skillNames);
    }
}