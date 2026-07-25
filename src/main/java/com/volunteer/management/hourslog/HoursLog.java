package com.volunteer.management.hourslog;

import com.volunteer.management.registration.Registration;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hours_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoursLog {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private Registration registration;

    /**
     * Hours credited for this shift. Computed from the shift's start/end
     * time at the moment attendance is marked, not entered by hand — so a
     * volunteer or admin can't fabricate hours.
     */
    @Column(nullable = false)
    private Double hours;

    @Column(name = "logged_at", nullable = false, updatable = false)
    private Instant loggedAt;

    @PrePersist
    void onCreate() {
        this.loggedAt = Instant.now();
    }
}
