package com.volunteer.management.volunteer;

import com.volunteer.management.auth.UserRepository;
import com.volunteer.management.exception.ResourceNotFoundException;
import com.volunteer.management.skill.Skill;
import com.volunteer.management.skill.SkillRepository;
import com.volunteer.management.volunteer.dto.VolunteerProfileResponse;
import com.volunteer.management.volunteer.dto.VolunteerProfileUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VolunteerProfileService {

    private final VolunteerProfileRepository volunteerProfileRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public VolunteerProfileService(
            VolunteerProfileRepository volunteerProfileRepository,
            UserRepository userRepository,
            SkillRepository skillRepository) {
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    public VolunteerProfileResponse getMyProfile(String email) {
        VolunteerProfile profile = findProfileByEmail(email);
        return toResponse(profile);
    }

    @Transactional
    public VolunteerProfileResponse updateMyProfile(String email, VolunteerProfileUpdateRequest request) {
        VolunteerProfile profile = findProfileByEmail(email);

        profile.setFullName(request.fullName());
        profile.setPhone(request.phone());
        profile.setAddress(request.address());
        profile.setBio(request.bio());

        if (request.skills() != null) {
            Set<Skill> resolvedSkills = request.skills().stream()
                    .map(this::findOrCreateSkill)
                    .collect(Collectors.toSet());
            profile.setSkills(resolvedSkills);
        }

        return toResponse(profile);
    }

    private Skill findOrCreateSkill(String name) {
        return skillRepository.findByName(name)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(name).build()));
    }

    private VolunteerProfile findProfileByEmail(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return volunteerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));
    }

    private VolunteerProfileResponse toResponse(VolunteerProfile profile) {
        Set<String> skillNames = new HashSet<>();
        profile.getSkills().forEach(s -> skillNames.add(s.getName()));

        return new VolunteerProfileResponse(
                profile.getFullName(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getBio(),
                profile.getJoinDate(),
                skillNames);
    }
}
