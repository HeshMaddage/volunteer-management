package com.volunteer.management.registration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    List<Registration> findByShiftId(UUID shiftId);

    List<Registration> findByVolunteerId(UUID volunteerId);

    Optional<Registration> findByVolunteerIdAndShiftId(UUID volunteerId, UUID shiftId);

    List<Registration> findByShift_Event_Id(UUID eventId);

    /**
     * Counts only "active" registrations (REGISTERED or ATTENDED) against a
     * shift's capacity — CANCELLED/NO_SHOW rows must not count towards the
     * limit, otherwise a cancelled slot would never free up.
     */
    long countByShiftIdAndStatusIn(UUID shiftId, List<RegistrationStatus> statuses);
}
