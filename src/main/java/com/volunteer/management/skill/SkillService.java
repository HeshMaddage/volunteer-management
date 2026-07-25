package com.volunteer.management.skill;

import com.volunteer.management.exception.ResourceNotFoundException;
import com.volunteer.management.skill.dto.SkillRequest;
import com.volunteer.management.skill.dto.SkillResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<SkillResponse> getAll() {
        return skillRepository.findAll().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName()))
                .toList();
    }

    public SkillResponse create(SkillRequest request) {
        if (skillRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Skill already exists: " + request.name());
        }
        Skill saved = skillRepository.save(Skill.builder().name(request.name()).build());
        return new SkillResponse(saved.getId(), saved.getName());
    }

    public void delete(UUID id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found: " + id);
        }
        skillRepository.deleteById(id);
    }
}
