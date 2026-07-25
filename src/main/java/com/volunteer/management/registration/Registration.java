package com.volunteer.management.registration;

import com.volunteer.management.shift.Shift;
import com.volunteer.management.volunteer.VolunteerProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Note: uniqueness of (volunteer, shift) is enforced at the DB level via a
 * PARTIAL unique index (see V1__init_schema.sql: uq_active_registration),
 * not a plain @UniqueConstraint here — a plain constraint would block a
 * volunteer from ever re-registering for a shift after one cancellation.
 * ddl-auto is "validate" (Flyway owns the schema), so this entity does not
 * declare the constraint itself.
 */
@Entity
@Table(name = "registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private VolunteerProfile volunteer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @PrePersist
    void onCreate() {
        this.registeredAt = Instant.now();
    }
}
