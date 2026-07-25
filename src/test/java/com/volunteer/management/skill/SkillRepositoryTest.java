package com.volunteer.management.skill;

import com.volunteer.management.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SkillRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SkillRepository skillRepository;

    @Test
    void savesAndFindsSkillByName() {
        skillRepository.save(Skill.builder().name("First Aid").build());

        assertThat(skillRepository.findByName("First Aid")).isPresent();
        assertThat(skillRepository.existsByName("Cooking")).isFalse();
    }

    @Test
    void skillNameUniquenessEnforcedAtDbLevel() {
        skillRepository.saveAndFlush(Skill.builder().name("Driving").build());

        assertThrows(DataIntegrityViolationException.class,
                () -> skillRepository.saveAndFlush(Skill.builder().name("Driving").build()));
    }
}
