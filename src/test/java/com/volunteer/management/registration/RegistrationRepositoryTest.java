package com.volunteer.management.registration;

import com.volunteer.management.auth.Role;
import com.volunteer.management.auth.User;
import com.volunteer.management.auth.UserRepository;
import com.volunteer.management.common.AbstractIntegrationTest;
import com.volunteer.management.event.Event;
import com.volunteer.management.event.EventRepository;
import com.volunteer.management.shift.Shift;
import com.volunteer.management.shift.ShiftRepository;
import com.volunteer.management.volunteer.VolunteerProfile;
import com.volunteer.management.volunteer.VolunteerProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class RegistrationRepositoryTest extends AbstractIntegrationTest {

    @Autowired private RegistrationRepository registrationRepository;
    @Autowired private VolunteerProfileRepository volunteerProfileRepository;
    @Autowired private ShiftRepository shiftRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;

    private VolunteerProfile createVolunteer(String emailPrefix) {
        User user = userRepository.save(User.builder()
                .email(emailPrefix + "-" + System.nanoTime() + "@example.com")
                .passwordHash("hash")
                .role(Role.VOLUNTEER)
                .build());
        return volunteerProfileRepository.save(VolunteerProfile.builder()
                .user(user)
                .fullName("Test Volunteer")
                .build());
    }

    private Shift createShift(int capacity) {
        Event event = eventRepository.save(Event.builder()
                .title("Beach Cleanup")
                .build());
        return shiftRepository.save(Shift.builder()
                .event(event)
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS))
                .capacity(capacity)
                .build());
    }

    @Test
    void volunteerCannotDoubleRegisterForSameActiveShift() {
        VolunteerProfile volunteer = createVolunteer("dup");
        Shift shift = createShift(5);

        registrationRepository.saveAndFlush(Registration.builder()
                .volunteer(volunteer)
                .shift(shift)
                .status(RegistrationStatus.REGISTERED)
                .build());

        Registration second = Registration.builder()
                .volunteer(volunteer)
                .shift(shift)
                .status(RegistrationStatus.REGISTERED)
                .build();

        // The partial unique index (uq_active_registration) rejects this at
        // the DB level, not just via application-level checks.
        assertThrows(DataIntegrityViolationException.class,
                () -> registrationRepository.saveAndFlush(second));
    }

    @Test
    void volunteerCanReRegisterAfterCancellingPreviousRegistration() {
        VolunteerProfile volunteer = createVolunteer("recancel");
        Shift shift = createShift(5);

        Registration first = registrationRepository.saveAndFlush(Registration.builder()
                .volunteer(volunteer)
                .shift(shift)
                .status(RegistrationStatus.REGISTERED)
                .build());

        first.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.saveAndFlush(first);

        // Because the unique index only covers REGISTERED/ATTENDED rows,
        // a new active registration for the same (volunteer, shift) is allowed.
        Registration reRegistered = registrationRepository.saveAndFlush(Registration.builder()
                .volunteer(volunteer)
                .shift(shift)
                .status(RegistrationStatus.REGISTERED)
                .build());

        assertThat(reRegistered.getId()).isNotNull();
    }

    @Test
    void countsOnlyActiveRegistrationsTowardsCapacity() {
        Shift shift = createShift(10);

        Registration registered = registrationRepository.saveAndFlush(Registration.builder()
                .volunteer(createVolunteer("v1"))
                .shift(shift)
                .status(RegistrationStatus.REGISTERED)
                .build());

        registrationRepository.saveAndFlush(Registration.builder()
                .volunteer(createVolunteer("v2"))
                .shift(shift)
                .status(RegistrationStatus.CANCELLED)
                .build());

        long activeCount = registrationRepository.countByShiftIdAndStatusIn(
                shift.getId(),
                List.of(RegistrationStatus.REGISTERED, RegistrationStatus.ATTENDED)
        );

        assertThat(activeCount).isEqualTo(1);
        assertThat(registered.getStatus()).isEqualTo(RegistrationStatus.REGISTERED);
    }
}
