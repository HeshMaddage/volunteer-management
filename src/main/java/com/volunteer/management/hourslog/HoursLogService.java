package com.volunteer.management.hourslog;

import com.volunteer.management.auth.UserRepository;
import com.volunteer.management.exception.ResourceNotFoundException;
import com.volunteer.management.hourslog.dto.HoursLogResponse;
import com.volunteer.management.hourslog.dto.HoursSummaryResponse;
import com.volunteer.management.volunteer.VolunteerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HoursLogService {

    private final HoursLogRepository hoursLogRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final UserRepository userRepository;

    public HoursLogService(
            HoursLogRepository hoursLogRepository,
            VolunteerProfileRepository volunteerProfileRepository,
            UserRepository userRepository) {
        this.hoursLogRepository = hoursLogRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.userRepository = userRepository;
    }

    public HoursSummaryResponse getMyHours(String email) {
        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();

        UUID volunteerId = volunteerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"))
                .getId();

        var history = hoursLogRepository.findByVolunteerId(volunteerId).stream()
                .map(h -> new HoursLogResponse(
                        h.getId(),
                        h.getRegistration().getShift().getId(),
                        h.getRegistration().getShift().getEvent().getTitle(),
                        h.getHours(),
                        h.getLoggedAt()))
                .toList();

        Double total = hoursLogRepository.sumHoursByVolunteerId(volunteerId);

        return new HoursSummaryResponse(total, history);
    }
}