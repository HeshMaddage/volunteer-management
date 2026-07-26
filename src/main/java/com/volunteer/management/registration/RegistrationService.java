package com.volunteer.management.registration;

import com.volunteer.management.exception.AlreadyRegisteredException;
import com.volunteer.management.exception.ResourceNotFoundException;
import com.volunteer.management.exception.ShiftFullException;
import com.volunteer.management.registration.dto.RegistrationResponse;
import com.volunteer.management.shift.Shift;
import com.volunteer.management.shift.ShiftRepository;
import com.volunteer.management.volunteer.VolunteerProfile;
import com.volunteer.management.volunteer.VolunteerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.volunteer.management.exception.InvalidStatusTransitionException;
import com.volunteer.management.hourslog.HoursLog;
import com.volunteer.management.hourslog.HoursLogRepository;
import com.volunteer.management.registration.dto.AttendanceUpdateRequest;
import com.volunteer.management.registration.dto.RosterEntryResponse;
import java.time.Duration;

import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ShiftRepository shiftRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final HoursLogRepository hoursLogRepository;

    private static final List<RegistrationStatus> ACTIVE_STATUSES = List.of(RegistrationStatus.REGISTERED,
            RegistrationStatus.ATTENDED);

    public RegistrationService(
            RegistrationRepository registrationRepository,
            ShiftRepository shiftRepository,
            VolunteerProfileRepository volunteerProfileRepository,
            HoursLogRepository hoursLogRepository) {
        this.registrationRepository = registrationRepository;
        this.shiftRepository = shiftRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.hoursLogRepository = hoursLogRepository;
    }

    @Transactional
    public RegistrationResponse register(UUID shiftId, UUID volunteerUserId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + shiftId));

        VolunteerProfile volunteer = volunteerProfileRepository.findByUserId(volunteerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));

        checkNotAlreadyRegistered(volunteer.getId(), shiftId);
        checkCapacityAvailable(shift);

        Registration registration = Registration.builder()
                .volunteer(volunteer)
                .shift(shift)
                .status(RegistrationStatus.REGISTERED)
                .build();

        return toResponse(registrationRepository.save(registration));
    }

    @Transactional
    public void cancel(UUID registrationId, UUID volunteerUserId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        VolunteerProfile volunteer = volunteerProfileRepository.findByUserId(volunteerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));

        if (!registration.getVolunteer().getId().equals(volunteer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cannot cancel another volunteer's registration");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
    }

    @Transactional
    public RegistrationResponse markAttendance(UUID registrationId, AttendanceUpdateRequest request) {
        RegistrationStatus newStatus = request.status();
        if (newStatus != RegistrationStatus.ATTENDED && newStatus != RegistrationStatus.NO_SHOW) {
            throw new InvalidStatusTransitionException(
                    "Attendance can only be set to ATTENDED or NO_SHOW, got: " + newStatus);
        }

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + registrationId));

        if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            throw new InvalidStatusTransitionException(
                    "Can only mark attendance on a REGISTERED registration, current status: "
                            + registration.getStatus());
        }

        registration.setStatus(newStatus);

        if (newStatus == RegistrationStatus.ATTENDED) {
            createHoursLog(registration);
        }

        return toResponse(registration);
    }

    private void createHoursLog(Registration registration) {
        var shift = registration.getShift();
        double hours = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes() / 60.0;

        HoursLog hoursLog = HoursLog.builder()
                .registration(registration)
                .hours(hours)
                .build();

        hoursLogRepository.save(hoursLog);
    }

    public List<RosterEntryResponse> getRoster(UUID eventId) {
        return registrationRepository.findByShift_Event_Id(eventId).stream()
                .map(r -> new RosterEntryResponse(
                        r.getId(), r.getShift().getId(), r.getVolunteer().getId(),
                        r.getVolunteer().getFullName(), r.getStatus(), r.getRegisteredAt()))
                .toList();
    }
    // --- The two business rules, kept separate and independently testable ---

    private void checkNotAlreadyRegistered(UUID volunteerId, UUID shiftId) {
        boolean alreadyActive = registrationRepository.findByVolunteerIdAndShiftId(volunteerId, shiftId)
                .filter(r -> ACTIVE_STATUSES.contains(r.getStatus()))
                .isPresent();

        if (alreadyActive) {
            throw new AlreadyRegisteredException("You are already registered for this shift");
        }
    }

    private void checkCapacityAvailable(Shift shift) {
        long activeCount = registrationRepository.countByShiftIdAndStatusIn(shift.getId(), ACTIVE_STATUSES);
        if (activeCount >= shift.getCapacity()) {
            throw new ShiftFullException("This shift is at full capacity");
        }
    }

    private RegistrationResponse toResponse(Registration r) {
        return new RegistrationResponse(
                r.getId(), r.getShift().getId(), r.getVolunteer().getId(),
                r.getVolunteer().getFullName(), r.getStatus(), r.getRegisteredAt());
    }
}