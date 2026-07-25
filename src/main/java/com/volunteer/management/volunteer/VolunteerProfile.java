package com.volunteer.management.volunteer;

import com.volunteer.management.auth.User;
import com.volunteer.management.skill.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "volunteer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "join_date", nullable = false, updatable = false)
    private Instant joinDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "volunteer_skills",
            joinColumns = @JoinColumn(name = "volunteer_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @PrePersist
    void onCreate() {
        this.joinDate = Instant.now();
    }
}
