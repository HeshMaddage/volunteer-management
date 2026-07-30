package com.volunteer.management.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<com.volunteer.management.shift.Shift> shifts = new java.util.ArrayList<>();
}
